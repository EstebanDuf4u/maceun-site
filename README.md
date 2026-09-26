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

Maceun peut, sans toucher au code :

- **Sorties** : ajouter / modifier / masquer / supprimer un son, avec sa pochette.
  La sortie visible la plus récente est mise en avant (compte à rebours si elle n'est pas encore sortie).
- **Dates** : ajouter des dates à la main, en plus de celles de Bandsintown (affichées en lecture seule).
- **Photos** : ajouter (glisser-déposer), réordonner, choisir paysage/portrait, supprimer.
- **Infos** : tous les textes, liens et images du site (accueil, bio, chiffres, contact, réseaux, FAQ).
  Dans les textes, `*mot*` s'affiche en italique ; liens et numéros deviennent cliquables.

Le front ne contient aucun contenu en dur : tout passe par l'API.

Sécurité : compte unique défini dans `.env`, session HttpOnly de 8 h, protection CSRF,
limite de 10 tentatives de connexion par IP toutes les 15 min, images vérifiées (vrai JPG/PNG/WebP).
En production (HTTPS), mettre `COOKIE_SECURE=true`.

## API

| Route | Rôle |
|---|---|
| `GET /api/content` | Textes, liens et images du site (accueil, bio, contact, réseaux, FAQ) |
| `GET /api/releases` | Dernière sortie + discographie |
| `GET /api/events` | Prochaines dates (Bandsintown + dates ajoutées), cache Bandsintown 10 min |
| `GET /api/photos` | Galerie |
| `POST /api/booking` | Formulaire de booking → validé, anti-spam, puis transmis à Formspree |
| `POST /api/auth/login` · `/logout` · `GET /me` | Connexion admin |
| `/api/admin/**` | Gestion des sorties, dates, photos, envoi d'images (connexion requise) |

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
