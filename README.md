# DJ Maceun : site officiel

- `frontend/` : React (Vite) + CSS, servi par Nginx
- `backend/` : API Java 21 / Spring Boot 4

## Lancer avec Docker

```bash
cp .env.example .env      # optionnel, pour changer le port ou les clés
docker compose up -d --build
```

Site : http://localhost:8088

## API

| Route | Rôle |
|---|---|
| `GET /api/releases` | Dernière sortie + discographie (`ReleaseService.java`) |
| `GET /api/events` | Prochaines dates Bandsintown, cache 10 min |
| `POST /api/booking` | Formulaire de booking → validé, anti-spam, puis transmis à Formspree |

**Ajouter un son** : une ligne dans `backend/src/main/java/fr/maceun/api/release/ReleaseService.java`
+ la pochette dans `frontend/public/img/`. Le plus récent devient automatiquement la « new release ».

## Développer sans Docker

```bash
# API (Maven requis, ou via Docker : voir backend/Dockerfile)
cd backend && mvn spring-boot:run
# Front, avec rechargement à chaud (proxy /api → :8080)
cd frontend && npm install && npm run dev
```
