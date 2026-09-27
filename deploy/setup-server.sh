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

# 3. Site nginx de l'hôte (ne touche pas aux autres sites : rutrack, deb1, deb2)
sed "s/__WEB_PORT__/$PORT/" "$HERE/nginx-maceun.conf" | sudo tee "$SITE" > /dev/null
sudo ln -sf "$SITE" "/etc/nginx/sites-enabled/$DOMAIN"
sudo nginx -t
sudo systemctl reload nginx
echo "→ nginx : $SITE activé"

# 4. Certificat HTTPS : seulement si le domaine pointe déjà vers ce serveur
SERVER_IP=$(curl -4 -s https://ifconfig.me || true)
if dig +short A "$DOMAIN" | grep -qx "$SERVER_IP"; then
  sudo certbot --nginx -d "$DOMAIN" -d "www.$DOMAIN" --redirect --non-interactive --agree-tos \
    --register-unsafely-without-email --keep-until-expiring
  echo "→ HTTPS actif : https://$DOMAIN"
else
  echo "⚠ $DOMAIN ne pointe pas encore vers ce serveur ($SERVER_IP) : HTTPS non configuré."
  echo "  Après la bascule DNS, relancer : sudo certbot --nginx -d $DOMAIN -d www.$DOMAIN --redirect"
fi
