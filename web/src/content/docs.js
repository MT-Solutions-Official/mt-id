export const docsNav = [
  {
    title: 'Começar',
    items: [
      { to: '/docs', label: 'Visão geral' },
      { to: '/docs/quickstart', label: 'Quickstart' },
      { to: '/docs/architecture', label: 'Arquitetura' },
    ],
  },
  {
    title: 'Integração',
    items: [
      { to: '/docs/application-auth', label: 'Token da aplicação' },
      { to: '/docs/users', label: 'Usuários' },
      { to: '/docs/user-auth', label: 'Login de usuários' },
      { to: '/docs/google', label: 'Google' },
      { to: '/docs/cors', label: 'CORS e browser' },
    ],
  },
  {
    title: 'Referência',
    items: [
      { to: '/docs/emails', label: 'E-mail e redirects' },
      { to: '/docs/errors', label: 'Erros e limites' },
      { to: '/docs/addresses', label: 'Endereços' },
    ],
  },
]

export const docs = {
  overview: {
    kicker: 'Documentação',
    title: 'MT ID como provedor de identidade',
    lead: 'O MT ID autentica três atores: o owner da plataforma, a aplicação cliente e o usuário final dessa aplicação. Sua app nunca implementa senha, JWT ou e-mail de verificação — ela consome este serviço.',
    sections: [
      {
        title: 'O que você recebe',
        paragraphs: [
          'Cada client application tem um appId, um apiKey/apiSecret (servidor) e origens liberadas para o browser. Users pertencem a um appId. O access token dura 15 minutos por padrão; o refresh token rota a sessão.',
        ],
        bullets: [
          'Cadastro e login de users com senha (e-mail verificado obrigatório)',
          'Login Google com JWKS, audience por app e campos obrigatórios no provision',
          'Refresh com rotação, logout e corte de sessão se a conta for desativada',
          'CORS amarrado ao appId da request',
          'Política de senha + checagem HIBP, throttle por conta e por IP',
        ],
      },
      {
        title: 'Base URL',
        paragraphs: ['Em desenvolvimento o IdP sobe em:'],
        code: { language: 'bash', code: 'http://localhost:8081\nSwagger: http://localhost:8081/swagger-ui' },
      },
    ],
  },
  quickstart: {
    kicker: 'Integração',
    title: 'Quickstart da aplicação cliente',
    lead: 'Crie a app no console, guarde o secret, obtenha um JWT de aplicação no seu backend e só então crie ou autentique users.',
    sections: [
      {
        title: '1. Crie a aplicação no console',
        paragraphs: [
          'Faça login como owner e abra Aplicações → Nova. Informe origins do seu front (ex.: http://localhost:5173), audience do Google se for usar, e os requiredUserFields.',
          'A resposta de create devolve apiKey, apiSecret e appId. O secret não volta em GET — copie na hora.',
        ],
      },
      {
        title: '2. Token da aplicação (somente servidor)',
        paragraphs: ['Nunca coloque apiSecret no frontend. O backend da sua app chama:'],
        code: {
          language: 'bash',
          code: `curl -X POST http://localhost:8081/api/v1/auth/application/token \\
  -H "apiKey: YOUR_API_KEY" \\
  -H "apiSecret: YOUR_API_SECRET"`,
        },
      },
      {
        title: '3. Crie um usuário',
        code: {
          language: 'bash',
          code: `curl -X POST http://localhost:8081/api/v1/users/create \\
  -H "Authorization: Bearer APP_ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{
    "name": "Jane Doe",
    "email": ["jane@example.com"],
    "password": "StrongPassword123!",
    "phones": [{ "phoneNumber": "+5511999999999" }]
  }'`,
        },
      },
      {
        title: '4. Login do usuário (browser ou BFF)',
        paragraphs: ['O front da sua app pode chamar o login diretamente se a origin estiver em allowedOrigins e o header appId for enviado.'],
        code: {
          language: 'javascript',
          code: `const response = await fetch("http://localhost:8081/api/v1/auth/users/token", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    appId: "YOUR_APP_ID",
  },
  body: JSON.stringify({
    email: "jane@example.com",
    password: "StrongPassword123!",
  }),
});`,
        },
      },
    ],
  },
  architecture: {
    kicker: 'Modelo',
    title: 'Arquitetura multi-app',
    lead: 'Um owner administra uma ou mais client applications. Cada app isola os seus users, papéis, origins e branding de e-mail.',
    sections: [
      {
        title: 'Atores e papéis JWT',
        table: {
          headers: ['Grupo', 'Quem', 'Para quê'],
          rows: [
            ['OWNER_WRITER / OWNER_VIEWER', 'Operador da plataforma', 'Console MT ID: criar apps, settings, time'],
            ['APPLICATION', 'Backend da sua app', 'CRUD de users daquele appId, criar users'],
            ['USER', 'Usuário final', 'GET /users/me, editar o próprio perfil'],
            ['REFRESH_TOKEN', 'Sessão', 'POST /auth/users/refresh e /logout'],
          ],
        },
      },
      {
        title: 'Isolamento',
        bullets: [
          'User pertence a um único appId. E-mail e username são únicos por app, não globalmente.',
          'O token de uma app não lê users de outra.',
          'CORS só libera a origin se ela estiver na app resolvida pelo header/query appId ou pelo claim app_id do JWT.',
          'O origin da plataforma (este console, localhost:3000) é o único liberado sem appId.',
        ],
      },
    ],
  },
  'application-auth': {
    kicker: 'Contrato',
    title: 'Autenticação da aplicação',
    lead: 'POST /api/v1/auth/application/token troca apiKey + apiSecret por um JWT com group APPLICATION. Use só no servidor.',
    sections: [
      {
        title: 'Request',
        paragraphs: ['Credenciais vão em headers, não no body.'],
        code: {
          language: 'bash',
          code: `POST /api/v1/auth/application/token
apiKey: mt_key_...
apiSecret: mt_secret_...`,
        },
      },
      {
        title: 'Response 200',
        code: {
          language: 'json',
          code: `{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}`,
        },
      },
      {
        title: 'Regras',
        bullets: [
          'App inativa (active=false) falha a autenticação.',
          'Throttle: 10 tentativas / 15 min por apiKey e 30 / 15 min por IP. Depois, 429 RATE_LIMIT_EXCEEDED.',
          'O secret é bcrypt. Rotate no console (owner) ou PATCH /client-applications/rotate-secret com o JWT APPLICATION.',
          'Não há refresh token de aplicação — peça um token novo quando expirar.',
        ],
      },
    ],
  },
  users: {
    kicker: 'Contrato',
    title: 'Usuários da sua aplicação',
    lead: 'Criar, desativar e gerenciar users exige o JWT APPLICATION. O próprio user autenticado lê e edita só a conta dele.',
    sections: [
      {
        title: 'POST /api/v1/users/create',
        paragraphs: ['RolesAllowed: APPLICATION. Campos efetivamente obrigatórios dependem de requiredUserFields da app. Senha precisa passar na política e no HIBP.'],
        code: {
          language: 'json',
          code: `{
  "name": "John Doe",
  "username": "john.doe",
  "email": ["john.doe@example.com"],
  "password": "StrongPassword123!",
  "phones": [{ "phoneNumber": "+5511999999999" }],
  "document": { "cpf": "12345678900" },
  "maritalStatus": "SINGLE",
  "roles": ["ADMIN"]
}`,
        },
      },
      {
        title: 'requiredUserFields',
        paragraphs: [
          'Valores: NAME, USERNAME, EMAIL, PASSWORD, PHONE, DOCUMENT, MARITAL_STATUS.',
          'No cadastro via Google, PASSWORD é ignorado. Os demais exigidos precisam ir no mesmo POST /auth/users/google-token.',
        ],
      },
      {
        title: 'Outros endpoints',
        table: {
          headers: ['Método', 'Rota', 'Quem'],
          rows: [
            ['GET', '/api/v1/users/me', 'USER'],
            ['GET', '/api/v1/users/{userId}', 'APPLICATION ou o próprio USER'],
            ['PATCH', '/api/v1/users/{userId}/disable | /enable', 'APPLICATION'],
            ['PATCH', '/api/v1/users/{userId}/address', 'APPLICATION ou o próprio USER'],
            ['POST', '/api/v1/users/{userId}/images/{imageType}', 'APPLICATION ou o próprio USER'],
          ],
        },
      },
    ],
  },
  'user-auth': {
    kicker: 'Contrato',
    title: 'Login, refresh e logout de users',
    lead: 'Access token curto. Guarde o refresh com o mesmo cuidado de uma sessão. Rotação a cada refresh.',
    sections: [
      {
        title: 'POST /api/v1/auth/users/token',
        paragraphs: ['O appId vai no header, não no body. O mesmo header também amarra o CORS à aplicação.'],
        code: {
          language: 'bash',
          code: `POST /api/v1/auth/users/token
Content-Type: application/json
appId: 507f1f77bcf86cd799439011

{
  "email": "user@example.com",
  "password": "user-password"
}`,
        },
      },
      {
        title: 'Response 200',
        code: {
          language: 'json',
          code: `{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "refreshTokenExpiresIn": 2592000
}`,
        },
      },
      {
        title: 'Refresh e logout',
        paragraphs: ['Authorization: Bearer <refreshToken>. O grupo do JWT de refresh é REFRESH_TOKEN.'],
        code: {
          language: 'bash',
          code: `POST /api/v1/auth/users/refresh
Authorization: Bearer <refreshToken>

POST /api/v1/auth/users/logout
Authorization: Bearer <refreshToken>`,
        },
      },
      {
        title: 'Regras de login',
        bullets: [
          'E-mail precisa estar verificado. Caso contrário: 403 EMAIL_NOT_VERIFIED.',
          'Conta desativada: 403 ACCOUNT_DISABLED. Access JWT também é recusado nas requests seguintes.',
          'Falha de credencial é genérica (não revela se o e-mail existe).',
          'Throttle por e-mail (10/15 min) e por IP (30/15 min).',
          'Header appId é obrigatório.',
        ],
      },
      {
        title: 'Perfil',
        paragraphs: ['Com o access token USER: GET /api/v1/users/me.'],
      },
    ],
  },
  google: {
    kicker: 'Contrato',
    title: 'Login Google',
    lead: 'O MT ID valida o ID token localmente via JWKS (iss, assinatura, expiração, email_verified) e a audience configurada na app.',
    sections: [
      {
        title: 'Users — POST /api/v1/auth/users/google-token',
        paragraphs: ['Envie o header appId para o CORS fechar na app certa. Se o e-mail ainda não existe naquele appId, a conta é provisionada — e os requiredUserFields (exceto senha) precisam vir no body.'],
        code: {
          language: 'json',
          code: `{
  "idToken": "google-id-token",
  "appId": "507f1f77bcf86cd799439011",
  "nonce": "optional-nonce",
  "name": "Jane Doe",
  "username": "jane",
  "phones": [{ "phoneNumber": "+5511999999999" }],
  "document": { "cpf": "12345678900" }
}`,
        },
      },
      {
        title: 'Owners — POST /api/v1/auth/owners/google-token',
        code: {
          language: 'json',
          code: `{ "idToken": "google-id-token", "nonce": "optional-nonce" }`,
        },
        paragraphs: ['Audience de owner é a do MT ID (configurada no servidor), não a da client application.'],
      },
      {
        title: 'No console da app',
        paragraphs: [
          'Grave o Client ID OAuth da sua aplicação em googleAudience (settings). Sem isso, o token Google do user é rejeitado.',
        ],
      },
    ],
  },
  cors: {
    kicker: 'Browser',
    title: 'CORS, appId e tokens no front',
    lead: 'O IdP não usa a CORS global do Quarkus. Um filtro libera a Origin só se ela for a da plataforma ou se pertencer à app da request.',
    sections: [
      {
        title: 'Como o appId é resolvido',
        bullets: [
          'Header appId',
          'Query ?appId=',
          'Claim app_id do JWT em Authorization',
        ],
      },
      {
        title: 'O que o front da sua app deve fazer',
        code: {
          language: 'javascript',
          code: `await fetch(url, {
  headers: {
    "Content-Type": "application/json",
    appId: APP_ID,
    Authorization: accessToken ? \`Bearer \${accessToken}\` : undefined,
  },
});`,
        },
      },
      {
        title: 'Credenciais',
        bullets: [
          'Access token de user: 15 min. Implemente refresh automático.',
          'Refresh vem no JSON, não em cookie httpOnly. Evite localStorage se puder; sessionStorage reduz a janela, mas XSS ainda lê.',
          'apiKey/apiSecret nunca no browser.',
          'Access-Control-Allow-Headers inclui Content-Type, Authorization, apiKey, apiSecret, appId.',
        ],
      },
    ],
  },
  emails: {
    kicker: 'Fluxos',
    title: 'Verificação e reset de senha',
    lead: 'O MT ID envia os e-mails. Você configura o branding e, se quiser, as URLs do seu front.',
    sections: [
      {
        title: 'Padrão (sem frontend do cliente)',
        paragraphs: [
          'Os links abrem páginas HTML do próprio MT ID: /api/v1/email/users/verify e /api/v1/email/users/reset-password.',
        ],
      },
      {
        title: 'Redirect para a sua app',
        paragraphs: [
          'Em emailSettings, defina verificationRedirectUrl, passwordResetRedirectUrl e loginUrl. Elas precisam ter origin em allowedOrigins (https, ou http em localhost).',
          'O MT ID acrescenta ?token=... na URL. No reset, o seu front deve POST /api/v1/users/password/reset com { token, newPassword }.',
        ],
      },
      {
        title: 'Forgot password',
        code: {
          language: 'json',
          code: `{ "email": "user@example.com", "appId": "507f1f77bcf86cd799439011" }`,
        },
        paragraphs: ['Sempre 204. Não revela se a conta existe. Throttle 5 / hora.'],
      },
    ],
  },
  errors: {
    kicker: 'Referência',
    title: 'Erros, senha e limites',
    lead: 'Erros de domínio vêm como JSON com errorCode, message, status, path e timestamp.',
    sections: [
      {
        title: 'Formato',
        code: {
          language: 'json',
          code: `{
  "errorCode": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests. Please try again later.",
  "status": 429,
  "path": "/api/v1/auth/users/token",
  "timestamp": "2026-08-15T14:00:00"
}`,
        },
      },
      {
        title: 'Códigos úteis',
        table: {
          headers: ['errorCode', 'HTTP', 'Quando'],
          rows: [
            ['APPLICATION_AUTHENTICATION_FAILED', '401', 'apiKey/apiSecret inválidos'],
            ['APPLICATION_FORBIDDEN', '403', 'App ou owner sem acesso ao recurso'],
            ['EMAIL_NOT_VERIFIED', '403', 'Login com e-mail ainda não confirmado'],
            ['ACCOUNT_DISABLED', '403', 'User, owner ou app desativados'],
            ['WEAK_PASSWORD', '400', 'Política: 8+ Aa1 e especial'],
            ['PASSWORD_COMPROMISED', '400', 'Senha encontrada no HIBP'],
            ['RATE_LIMIT_EXCEEDED', '429', 'Login, token de app, e-mail ou CEP'],
            ['REQUIRED_USER_FIELD_MISSING', '400', 'Campo exigido pela app ausente'],
          ],
        },
      },
    ],
  },
  addresses: {
    kicker: 'Utilitário',
    title: 'Lookup de endereço',
    lead: 'Público, limitado a 30 chamadas por IP por minuto. Use no checkout da sua app, não como proxy aberto.',
    sections: [
      {
        title: 'Rotas',
        table: {
          headers: ['País', 'Rota'],
          rows: [
            ['Brasil (ViaCEP)', 'GET /api/v1/addresses/br/{zipCode}'],
            ['Indonésia (KodePos)', 'GET /api/v1/addresses/id/{zipCode}'],
            ['EUA (Zippopotam)', 'GET /api/v1/addresses/us/{zipCode}'],
            ['Portugal (Zippopotam)', 'GET /api/v1/addresses/pt/{zipCode}'],
          ],
        },
      },
      {
        title: 'Anexar ao user',
        paragraphs: [
          'PATCH /api/v1/users/{userId}/address não chama ViaCEP. Envie o endereço completo (country, zipCode, street, number, city, state). Países: BR, US, PT, ID.',
        ],
      },
    ],
  },
}
