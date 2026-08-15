import { motion } from 'framer-motion'
import { Link } from 'react-router-dom'
import { ArrowUpRight } from 'lucide-react'
import { Button } from '../components/ui/Button'
import { SiteFooter, SiteHeader } from '../components/layout/SiteChrome'

const fade = {
  initial: { opacity: 0, y: 18 },
  animate: { opacity: 1, y: 0 },
  transition: { duration: 0.7, ease: [0.22, 1, 0.36, 1] },
}

const features = [
  ['Multi-app', 'Users, papéis, origins e Google audience isolados por appId. O token de uma app não lê a outra.'],
  ['JWT curto', 'Access de 15 minutos, refresh com rotação, jti hasheado e corte se a conta for desativada.'],
  ['Caminho crítico', 'Bcrypt, HIBP, throttle por conta e IP, tokens de e-mail só em hash, CORS amarrado ao appId.'],
  ['Google JWKS', 'Validação local do ID token. Provision respeita requiredUserFields, menos a senha.'],
  ['E-mail da app', 'Verificação e reset com branding do cliente. Sem redirect, o MT ID serve as páginas.'],
  ['Console', 'O owner cria apps, rotaciona secret, define campos e origins — sem Postman.'],
]

const steps = [
  ['Owner', 'Você opera a plataforma neste console.'],
  ['Application', 'O backend da sua app troca apiKey/apiSecret por JWT APPLICATION.'],
  ['User', 'O browser da sua app autentica users com o header appId.'],
]

export function Landing() {
  return (
    <div className="min-h-screen bg-bg">
      <div className="relative overflow-hidden">
        <div className="pointer-events-none absolute inset-0 vignette" />
        <SiteHeader />
        <section className="relative mx-auto max-w-6xl px-5 pt-16 pb-20 md:pt-24 md:pb-28">
          <motion.p {...fade} className="font-mono text-[11px] tracking-[0.28em] text-ink-faint uppercase">
            Identity provider
          </motion.p>
          <motion.h1
            {...fade}
            transition={{ ...fade.transition, delay: 0.06 }}
            className="display mt-6 max-w-[14ch] text-[56px] text-ink md:text-[96px]"
          >
            Identidade para as suas apps.
          </motion.h1>
          <motion.p
            {...fade}
            transition={{ ...fade.transition, delay: 0.12 }}
            className="mt-8 max-w-xl text-[17px] leading-8 text-ink-muted"
          >
            Owners administram aplicações. Cada aplicação autentica os próprios users com JWT, Google e verificação de
            e-mail. Você não implementa auth de novo.
          </motion.p>
          <motion.div {...fade} transition={{ ...fade.transition, delay: 0.18 }} className="mt-10 flex flex-wrap gap-3">
            <Link to="/signup">
              <Button size="lg">
                Abrir o console
                <ArrowUpRight className="h-4 w-4" />
              </Button>
            </Link>
            <Link to="/docs">
              <Button size="lg" variant="secondary">
                Contratos
              </Button>
            </Link>
          </motion.div>
        </section>
      </div>

      <div className="hairline" />

      <section className="mx-auto grid max-w-6xl gap-12 px-5 py-16 md:grid-cols-[1fr_1.1fr] md:py-24">
        <div>
          <p className="font-mono text-[11px] tracking-[0.22em] text-ink-faint uppercase">Request</p>
          <h2 className="display mt-3 text-4xl md:text-5xl">Um POST. Uma sessão.</h2>
        </div>
        <pre className="overflow-x-auto border border-line bg-surface p-6 font-mono text-[12px] leading-7 text-ink-muted md:p-8">
{`POST /api/v1/auth/users/token
appId: 64f1c2a9e8b17d001234abcd

{
  "email": "jane@acme.io",
  "password": "••••••••"
}

→ 900s access · refresh rotacionado`}
        </pre>
      </section>

      <div className="hairline" />

      <section className="mx-auto max-w-6xl px-5 py-16 md:py-24">
        <h2 className="display text-4xl md:text-5xl">O que está no IdP</h2>
        <div className="mt-12 divide-y divide-line border-y border-line">
          {features.map(([title, text], index) => (
            <div key={title} className="grid gap-3 py-6 md:grid-cols-[80px_200px_1fr] md:items-baseline">
              <span className="font-mono text-[11px] text-ink-faint">0{index + 1}</span>
              <h3 className="text-[15px] text-ink">{title}</h3>
              <p className="text-sm leading-7 text-ink-muted">{text}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="border-y border-line bg-bg-muted">
        <div className="mx-auto grid max-w-6xl gap-12 px-5 py-16 md:grid-cols-3 md:py-20">
          {steps.map(([title, text], index) => (
            <div key={title}>
              <div className="font-mono text-[11px] text-accent">0{index + 1}</div>
              <h3 className="display mt-3 text-3xl">{title}</h3>
              <p className="mt-3 text-sm leading-7 text-ink-muted">{text}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-5 py-20 md:py-28">
        <h2 className="display max-w-[12ch] text-5xl md:text-7xl">Plugue a sua app.</h2>
        <p className="mt-6 max-w-lg text-ink-muted">
          Quickstart, CORS, Google e o formato de erro estão na documentação. O console é só para o owner.
        </p>
        <div className="mt-8 flex flex-wrap gap-3">
          <Link to="/docs/quickstart">
            <Button size="lg">
              Quickstart
              <ArrowUpRight className="h-4 w-4" />
            </Button>
          </Link>
          <Link to="/login">
            <Button size="lg" variant="secondary">
              Entrar
            </Button>
          </Link>
        </div>
      </section>
      <SiteFooter />
    </div>
  )
}
