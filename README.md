# Foglio

Foglio è una piattaforma leggera per la condivisione di file con architettura separata: backend API in Spring Boot e frontend in Next.js servito da Nginx. Offre registrazione con API key, upload/download, anteprima per immagini/video e gestione di file pubblici/privati. Il deploy è plug‑and‑play tramite Docker Compose.

Architettura separata in due progetti:
- `foglio-be`: Spring Boot API (porta interna 8080)
- `foglio-fe`: Frontend Next.js servito da Nginx (porta 80)

## Avvio rapido (Docker Compose)

Prerequisiti: Docker Desktop.

```
# build + run
docker compose up --build

# run successivi
docker compose up

# stop
docker compose down
```

Servizi:
- Frontend: http://localhost
- API (via proxy): http://localhost/api

## Autenticazione
Le API richiedono l'header:
```
X-API-KEY: <la tua api key>
```

Flusso tipico:
1. Signup: POST `/api/users/register?name=...` → salva la `apiKey`
2. Me: GET `/api/users/me`
3. Lista file: GET `/api/files`
4. Upload: POST `/api/files/upload` (multipart) con `public` opzionale
5. Download/Preview: GET `/api/files/{id}/(download|preview)`

## Configurazione
- Database: PostgreSQL in Docker (volume persistente)
- Storage file: volume `files_data` montato in backend
- Profilo Spring: `docker` (impostato nel Dockerfile)

## Struttura
```
Foglio/
├─ foglio-be/  # Backend API
├─ foglio-fe/  # Frontend Next.js
└─ docker-compose.yml
```
