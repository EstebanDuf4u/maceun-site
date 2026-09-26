# DJ Maceun : site officiel

- `frontend/` : React (Vite) + CSS, servi par Nginx. Site public sur `/`, espace admin sur `/admin`
- `backend/` : API Java 21 / Spring Boot 4 + PostgreSQL

## Lancer avec Docker

```bash
cp .env.example .env      # puis remplir DB_PASSWORD et ADMIN_PASSWORD
docker compose up -d --build
```

- Site : http://localhost:8088
- Admin : http://localhost:8088/admin (identifiant `ADMIN_USERNAME`, mot de passe `ADMIN_PASSWORD` du `.env`)

Les données (base + images envoyées) sont dans les volumes Docker `db-data` et `uploads` :
elles survivent aux redémarrages et aux mises à jour.

## Espace admin

Les **sons** et les **dates** sont récupérés automatiquement :

- **Sorties** : Maceun colle le lien **Fanlink** d'un son. Le back récupère le titre, la pochette et les
  plateformes (page Fanlink), puis la date de sortie, le genre et la pochette HD (**API Deezer**, par ISRC
  ou titre). Tout est remis à jour toutes les 6 h ; si un lien ne répond plus, les dernières infos sont
  gardées et l'erreur s'affiche dans l'admin. Pour un son pas encore sorti, une date peut être saisie
  (compte à rebours sur le site) ; sinon, sans Deezer, c'est la date de création du lien Fanlink.
- **Dates** : uniquement **Bandsintown** (lecture seule dans l'admin, cache 10 min).
- **Photos** : ajouter (glisser-déposer), réordonner, choisir paysage/portrait, supprimer.

Sécurité : compte unique défini dans `.env`, session HttpOnly de 8 h, protection CSRF,
limite de 10 tentatives de connexion par IP toutes les 15 min, images vérifiées (vrai JPG/PNG/WebP).
En production (HTTPS), mettre `COOKIE_SECURE=true`.

## API

| Route | Rôle |
|---|---|
| `GET /api/releases` | Dernière sortie + discographie (Fanlink + Deezer, synchro toutes les 6 h) |
| `GET /api/events` | Prochaines dates Bandsintown, cache 10 min |
| `GET /api/photos` | Galerie |
| `POST /api/booking` | Formulaire de booking → validé, anti-spam, puis transmis à Formspree |
| `POST /api/auth/login` · `/logout` · `GET /me` | Connexion admin |
| `/api/admin/**` | Ajout de sons par lien Fanlink, photos (connexion requise) |

## Développer sans Docker

```bash
docker compose up -d db                                  # juste la base
cd backend && DB_PASSWORD=... ADMIN_PASSWORD=... mvn spring-boot:run
cd frontend && npm install && npm run dev                # proxy /api → :8080
```

Tests du back (sans Maven installé) :

```bash
cd backend && docker run --rm -v "$PWD":/app -v maceun-m2:/root/.m2 -w /app maven:3.9-eclipse-temurin-21 mvn -q test
```
