#!/usr/bin/env bash
# Installation initiale sur le serveur (une seule fois), à lancer en tant qu'esteban :
#   scp deploy/setup-server.sh deploy/nginx-maceun.conf esteban@79.175.70.167:/tmp/
#   ssh -t esteban@79.175.70.167 'bash /tmp/setup-server.sh'
# Idempotent : relançable sans risque (ne remplace jamais un .env existant).
set -euo pipefail

DEPLOY_PATH="${DEPLOY_PATH:-/home/esteban/maceun-site}"
DOMAIN="maceunofficiel.fr"
SITE="/etc/nginx/sites-available/$DOMAIN"
HERE="$(cd "$(dirname "$0")" && pwd)"

mkdir -p "$DEPLOY_PATH"

# 1. Port local libre pour le conteneur web (rutrack utilise déjà 8080)
if [ -f "$DEPLOY_PATH/.env" ] && grep -q '^WEB_PORT=' "$DEPLOY_PATH/.env"; then
  PORT=$(grep '^WEB_PORT=' "$DEPLOY_PATH/.env" | cut -d= -f2)
else
  PORT=8081
  while ss -ltn "( sport = :$PORT )" | grep -q ":$PORT"; do PORT=$((PORT + 1)); done
fi
echo "→ Port local du site : 127.0.0.1:$PORT"

# 2. Secrets de production (générés, jamais dans git)
if [ ! -f "$DEPLOY_PATH/.env" ]; then
  ADMIN_PASSWORD=$(openssl rand -base64 24 | tr -d '/+=' | cut -c1-20)
  umask 077
  cat > "$DEPLOY_PATH/.env" <<ENV
DOMAIN=$DOMAIN
WEB_PORT=$PORT

DB_PASSWORD=$(openssl rand -hex 24)
ADMIN_USERNAME=maceun
ADMIN_PASSWORD=$ADMIN_PASSWORD

BANDSINTOWN_ARTIST=DJ Maceun
BANDSINTOWN_APP_ID=0ee6368fd3652519a6a69a1065f0283d
FORMSPREE_URL=https://formspree.io/f/xanwkaap
ENV
  echo "→ $DEPLOY_PATH/.env créé (droits 600)."
  echo "   Identifiant admin : maceun"
  echo "   Mot de passe admin : $ADMIN_PASSWORD   ← à noter maintenant, il n'est affiché qu'une fois"
else
  echo "→ $DEPLOY_PATH/.env existe déjà : conservé."
fi

# 3. Certificat : auto-signé d'abord (nginx a besoin d'un bloc 443 pour ce domaine, sinon il
#    répond avec le premier site HTTPS venu, c'est-à-dire rutrack), puis Let's Encrypt.
LE="/etc/letsencrypt/live/$DOMAIN"
if sudo test -f "$LE/fullchain.pem"; then
  CERT="$LE/fullchain.pem"; KEY="$LE/privkey.pem"
else
  sudo mkdir -p /etc/ssl/maceun
  sudo test -f /etc/ssl/maceun/fullchain.pem || sudo openssl req -x509 -nodes -newkey rsa:2048 -days 30 \
    -subj "/CN=$DOMAIN" -addext "subjectAltName=DNS:$DOMAIN,DNS:www.$DOMAIN" \
    -keyout /etc/ssl/maceun/privkey.pem -out /etc/ssl/maceun/fullchain.pem 2>/dev/null
  CERT=/etc/ssl/maceun/fullchain.pem; KEY=/etc/ssl/maceun/privkey.pem
fi

# 4. Site nginx de l'hôte (ne touche pas aux autres sites : rutrack, deb1, deb2)
sudo mkdir -p /var/www/certbot
sed -e "s/__WEB_PORT__/$PORT/" -e "s#__CERT__#$CERT#" -e "s#__KEY__#$KEY#" "$HERE/nginx-maceun.conf" | sudo tee "$SITE" > /dev/null
sudo ln -sf "$SITE" "/etc/nginx/sites-enabled/$DOMAIN"
sudo nginx -t
sudo systemctl reload nginx
echo "→ nginx : $SITE activé (certificat : $CERT)"

# 5. Let's Encrypt par webroot : fonctionne aussi derrière le proxy Cloudflare
if [ "$CERT" != "$LE/fullchain.pem" ]; then
  if sudo certbot certonly --webroot -w /var/www/certbot -d "$DOMAIN" -d "www.$DOMAIN" \
       --non-interactive --agree-tos --register-unsafely-without-email --keep-until-expiring \
       --deploy-hook "systemctl reload nginx"; then
    sudo sed -i -e "s#/etc/ssl/maceun/fullchain.pem#$LE/fullchain.pem#" -e "s#/etc/ssl/maceun/privkey.pem#$LE/privkey.pem#" "$SITE"
    sudo nginx -t && sudo systemctl reload nginx && sudo rm -rf /etc/ssl/maceun
    echo "→ HTTPS Let's Encrypt actif : https://$DOMAIN (renouvellement automatique)"
  else
    echo "⚠ Certificat Let's Encrypt impossible pour l'instant (DNS pas encore basculé ?)."
    echo "  Le site tourne avec un certificat auto-signé (OK derrière Cloudflare en mode Full)."
    echo "  Relancer ce script après la bascule DNS."
  fi
fi
