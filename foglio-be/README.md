# foglio-be (Backend API)

Spring Boot 3 API per gestione file con autenticazione via API key. Espone endpoint per registrazione utente, upload, download, preview e lista dei propri file.

Punti chiave:
- Java 17, Spring Security, Spring Data JPA
- PostgreSQL in Docker, H2 in dev
- Storage file su disco (configurabile)

Avvio (in Docker Compose): vedi documentazione generale nel README principale (`../README.md`).

Endpoint principali (prefisso `/api`):
- POST `/users/register?name=...` → crea utente e restituisce API key
- GET `/users/me` → dati utente corrente
- GET `/files` → lista dei file dell’utente
- POST `/files/upload` → upload file (multipart), `public` opzionale
- GET `/files/{id}/(metadata|preview|download)` → metadati/preview/download

Auth: header `X-API-KEY: <apiKey>`

Build locale (opzionale):
- `./gradlew build`
- `./gradlew bootRun`
