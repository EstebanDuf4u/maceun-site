# Déploiement

Production : **https://maceunofficiel.fr**, même serveur que rutrack : `79.175.70.167` (Ubuntu 24.04),
dossier `/home/esteban/maceun-site`.

Chaque push sur `main` lance les tests (GitHub Actions, `.github/workflows/deploy.yml`) ;
s'ils passent, le code est synchronisé (rsync) sur le serveur puis
`docker compose -f docker-compose.prod.yml up -d --build --wait` est relancé, suivi d'un test
que le site et l'API répondent. Les migrations Flyway s'appliquent au démarrage du backend.
Les autres branches et les PR ne font que les tests.

## Architecture

```
Internet ─▶ Cloudflare (DNS) ─▶ nginx de l'hôte (HTTPS, certbot) ─▶ 127.0.0.1:8081  web (nginx : front + /api)
                                                                                     └─▶ backend (Spring) ─▶ db (Postgres)
```

- **nginx de l'hôte** = le reverse proxy : il choisit le site selon le domaine demandé.
  `/etc/nginx/sites-available/maceunofficiel.fr` (modèle : `deploy/nginx-maceun.conf`).
  Il sert aussi rutrack, deb1, deb2 — ne pas y toucher.
  ⚠️ Chaque domaine doit avoir **son propre bloc 443** : sinon nginx répond avec le premier site
  HTTPS configuré (rutrack). C'est ce qui arrive quand Cloudflare (proxy orange) contacte le serveur.
- **HTTPS** : Let's Encrypt obtenu par `certbot certonly --webroot` (marche derrière Cloudflare),
  renouvellement automatique + rechargement de nginx (`--deploy-hook`).
- **`docker-compose.prod.yml`** : `web` (n'écoute que sur `127.0.0.1:8081`, rutrack a 8080),
  `backend` (cookies `Secure`), `db` (volume `maceun_db_data`, aucun port publié),
  images envoyées dans le volume `maceun_uploads`.
- **Secrets** : `/home/esteban/maceun-site/.env` (droits 600), uniquement sur le serveur ;
  modèle `deploy/.env.prod.example`. Jamais écrasé par le déploiement.

## Mise en place (une seule fois)

1. **Serveur** (crée le `.env` avec des secrets générés, le site nginx, et le HTTPS si le DNS est prêt) :
   ```bash
   scp deploy/setup-server.sh deploy/nginx-maceun.conf esteban@79.175.70.167:/tmp/
   ssh -t esteban@79.175.70.167 'bash /tmp/setup-server.sh'
   ```
   Le mot de passe admin du site est affiché **une seule fois** : le noter.

2. **Secret GitHub manquant** (les autres sont déjà configurés) :
   ```bash
   gh secret set SSH_PASSWORD -R EstebanDuf4u/maceun-site
   ```

3. **DNS chez Cloudflare** : enregistrements `A` de `maceunofficiel.fr` et `www` → `79.175.70.167`
   (proxy orange ou gris, les deux marchent ; SSL/TLS en mode **Full (strict)**).
   Relancer ensuite le script de l'étape 1 s'il n'avait pas pu obtenir le certificat.

## Secrets GitHub (Settings → Secrets and variables → Actions)

| secret | valeur |
|---|---|
| `SSH_HOST` | `79.175.70.167` |
| `SSH_USER` | `esteban` |
| `SSH_PASSWORD` | mot de passe SSH (connexion par mot de passe, pas de clé) |
| `SSH_KNOWN_HOSTS` | `ssh-keyscan -t ed25519 79.175.70.167` |
| `DEPLOY_PATH` | `/home/esteban/maceun-site` |

⚠️ Si le mot de passe SSH change, mettre à jour `SSH_PASSWORD` ici **et** dans rutrack.

## Opérations courantes (sur le serveur, dans `/home/esteban/maceun-site`)

- Logs : `docker compose -f docker-compose.prod.yml logs -f backend`
- Sauvegarde de la base :
  `docker compose -f docker-compose.prod.yml exec -T db pg_dump -U maceun maceun > backup.sql`
- Changer le mot de passe admin : modifier `ADMIN_PASSWORD` dans `.env` puis
  `docker compose -f docker-compose.prod.yml up -d backend`
