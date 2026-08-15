# MT ID Console

Frontend do provedor de identidade: landing, documentação de integração e dashboard do owner.

## Stack

React + Vite + Tailwind (JavaScript). Porta **3000** — é a origin de plataforma do backend.

## Desenvolvimento

Na raiz do monorepo, o IdP:

```bash
mvn quarkus:dev
```

Neste diretório:

```bash
npm install
npm run dev
```

Abra `http://localhost:3000`.

## Env

```
VITE_API_URL=http://localhost:8081
VITE_GOOGLE_CLIENT_ID=<Google OAuth client id de owner>
```
