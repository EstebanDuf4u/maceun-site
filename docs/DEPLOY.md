# Déploiement

Production : **https://maceunofficiel.fr**, même serveur que rutrack et noryx.fr : `164.132.192.219`
(VPS OVH, Ubuntu), dossier `/home/ubuntu/maceun-site`.

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
  Il sert aussi rutrack et noryx.fr — ne pas y toucher.
  ⚠️ Chaque domaine doit avoir **son propre bloc 443** : sinon nginx répond avec le premier site
  HTTPS configuré (rutrack). C'est ce qui arrive quand Cloudflare (proxy orange) contacte le serveur.
- **HTTPS** : Let's Encrypt obtenu par `certbot certonly --webroot` (marche derrière Cloudflare),
  renouvellement automatique + rechargement de nginx (`--deploy-hook`).
- **`docker-compose.prod.yml`** : `web` (n'écoute que sur `127.0.0.1:8081`, rutrack a 8080),
  `backend` (cookies `Secure`), `db` (volume `maceun_db_data`, aucun port publié),
  images envoyées dans le volume `maceun_uploads`.
- **Secrets** : `/home/ubuntu/maceun-site/.env` (droits 600), uniquement sur le serveur ;
  modèle `deploy/.env.prod.example`. Jamais écrasé par le déploiement.

## Mise en place (une seule fois)

1. **Serveur** (crée le `.env` avec des secrets générés, le site nginx, et le HTTPS si le DNS est prêt) :
   ```bash
   rsync -a deploy/ ubuntu@164.132.192.219:maceun-site/deploy/
   ssh -t ubuntu@164.132.192.219 'cd ~/maceun-site/deploy && bash setup-server.sh'
   ```
   Le mot de passe admin du site est affiché **une seule fois** : le noter.

2. **DNS chez Cloudflare** : enregistrements `A` de `maceunofficiel.fr` et `www` → `164.132.192.219`
   (proxy orange ou gris, les deux marchent ; SSL/TLS en mode **Full (strict)**), pas d'`AAAA`
   (le serveur n'a pas d'IPv6 publique). Relancer ensuite le script de l'étape 1 s'il n'avait pas pu
   obtenir le certificat.

## Secrets GitHub (Settings → Secrets and variables → Actions)

| secret | valeur |
|---|---|
| `SSH_HOST` | `164.132.192.219` |
| `SSH_USER` | `ubuntu` |
| `SSH_PRIVATE_KEY` | clé privée de déploiement (sa clé publique est dans `~/.ssh/authorized_keys` du serveur, commentaire `noryx-site deploy (GitHub Actions)` ; la même sert à noryx-site) |
| `SSH_KNOWN_HOSTS` | `ssh-keyscan 164.132.192.219` |
| `DEPLOY_PATH` | `/home/ubuntu/maceun-site` |

Pour révoquer l'accès : supprimer la ligne de la clé dans `authorized_keys` sur le serveur.

## Opérations courantes (sur le serveur, dans `/home/ubuntu/maceun-site`)

- Logs : `docker compose -f docker-compose.prod.yml logs -f backend`
- Sauvegarde de la base :
  `docker compose -f docker-compose.prod.yml exec -T db pg_dump -U maceun maceun > backup.sql`
- Changer le mot de passe admin : modifier `ADMIN_PASSWORD` dans `.env` puis
  `docker compose -f docker-compose.prod.yml up -d backend`
