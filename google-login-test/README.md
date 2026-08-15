# Google Login Test (MT-ID)

Pagina simples para testar os endpoints:

- `POST /api/v1/auth/users/google-token`
- `POST /api/v1/auth/owners/google-token`

## 1) Subir backend

Use seu fluxo normal (`quarkus:dev`) e garanta que `application-dev.yml` esta com o `audience` correto.

## 2) Subir este front de teste em `localhost:3000`

No macOS/Linux:

```bash
cd /Users/matteus.guimaraes/Documents/Projects/Personal/mt-id/google-login-test
python3 -m http.server 3000
```

## 3) Abrir no navegador

- `http://localhost:3000`

## 4) Testar login

1. Clique em **Inicializar botao Google**
2. Clique no botao Google e faca login
3. O `id_token` sera preenchido automaticamente
4. Clique em **Enviar token para backend**

## Observacoes

- O backend valida `iss`, `aud`, `email_verified` e usuario ativo no banco.
- Login de user exige `appId` no body, alem do `idToken`.
- Se der `401`, confira se:
  - O `aud` do token bate com o `app.mt.id.google.user.audience` (ou owner).
  - O email da conta Google existe na colecao de usuarios/owners.
- Se der erro de CORS, confirme origem liberada em `quarkus.http.cors.origins`.

