# foglio-fe (Frontend)

Frontend Next.js 14 + TailwindCSS. Serve static via Nginx in Docker e usa `/api` come base per le chiamate.

Pagine:
- Home, Login, Signup, Files (dashboard)

Config: `NEXT_PUBLIC_API_URL=/api` (impostato in Docker tramite Nginx proxy).

Dev (opzionale):
```
npm install
npm run dev
```
