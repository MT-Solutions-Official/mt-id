# MT ID

API de identidade multi-aplicação (Quarkus 3 / Java 25) na porta **8081**. Users, papéis e CORS são isolados por `appId`. MongoDB guarda os dados; SMTP envia verificação e reset de senha.

O console do owner (landing, `/docs`, dashboard) vive no repositório irmão **[mt-id-front](../mt-id-front)** — Vite + React na porta **3000**.

- Swagger: [http://localhost:8081/swagger-ui](http://localhost:8081/swagger-ui)
- OpenAPI: [http://localhost:8081/q/openapi](http://localhost:8081/q/openapi)
- Docs de integração: [http://localhost:3000/docs](http://localhost:3000/docs) (com o console no ar)

## O que você precisa

- **Java 25** e o wrapper Maven (`./mvnw`)
- **MongoDB** em `localhost:27017` (database `mt-id` por padrão)
- **SMTP** para e-mails de verificação e senha (Gmail, Mailtrap, etc.)
- Opcional: clone de `mt-id-front` se for usar o console

## Subir localmente

### 1. MongoDB

```bash
docker run --name mt-id-mongo -p 27017:27017 -d mongo:7
```

Connection string padrão: `mongodb://localhost:27017/` (database `mt-id`). Override com `APP_MT_ID_MONGODB_ENDPOINT` e `APP_MT_ID_MONGODB_DATABASE`.

### 2. API — porta 8081

```bash
./mvnw quarkus:dev
```

Sobe em [http://localhost:8081](http://localhost:8081).

A origin da plataforma (`http://localhost:3000` por padrão) passa no CORS sem `appId`. Override: `APP_MT_ID_CORS_ORIGIN` e `APP_MT_ID_FRONTEND_BASE_URL`.

### 3. Console — outro repositório

```bash
cd ../mt-id-front
cp .env.example .env   # VITE_API_URL=http://localhost:8081
npm install
npm run dev
```

Abra [http://localhost:3000](http://localhost:3000). Primeiro owner: `/signup`. Se for o único no banco, o e-mail é marcado como verificado no startup (o console funciona sem SMTP). Owners seguintes precisam confirmar o e-mail.

## SMTP

Sem SMTP configurado, o cadastro de user com e-mail falha no envio (o user ainda é criado).

```bash
export APP_MT_ID_MAILER_FROM="no-reply@seu-dominio.com"
export APP_MT_ID_MAILER_HOST="smtp.gmail.com"
export APP_MT_ID_MAILER_PORT="587"
export APP_MT_ID_MAILER_USERNAME="seu-usuario"
export APP_MT_ID_MAILER_PASSWORD="sua-senha-de-app"
```

`APP_MT_ID_MAILER_MOCK=true` evita SMTP real. Não commite senhas — use env, não `application-dev.yml`.

## Google e Cloudinary (opcional)

- **Google (users da sua app):** Client ID OAuth em `googleAudience` da client application.
- **Google (owner do console):** `VITE_GOOGLE_CLIENT_ID` no `.env` do **mt-id-front** e a audience correspondente nesta API.
- **Imagens:** `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`. Sem isso, upload de foto quebra; o resto da API segue.

## Primeiro fluxo

1. Owner no console (`mt-id-front`): `/signup` → `/login`.
2. Aplicações → Nova. Informe `allowedOrigins` do **seu** front (ex. `http://localhost:5173`). Guarde o `apiSecret` — só aparece uma vez.
3. Backend da sua app: `POST /api/v1/auth/application/token` com headers `apiKey` + `apiSecret`.
4. Com o JWT `APPLICATION`: `POST /api/v1/users/create`, `GET /api/v1/users`, disable/enable.
5. No browser do user: header `appId` em toda chamada; login em `POST /api/v1/auth/users/token`. O próprio user atualiza nome/e-mail/senha em `PATCH /api/v1/users/me`.

Contrato completo: `/docs` no console, ou Swagger nesta API.

## Empacotar

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```
