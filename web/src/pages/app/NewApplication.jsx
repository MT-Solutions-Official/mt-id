import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '../../components/ui/Button'
import { Input, Textarea } from '../../components/ui/Input'
import { Callout, Dialog } from '../../components/ui/Dialog'
import { apps } from '../../lib/api'
import { useAuth } from '../../lib/auth'
import { REQUIRED_FIELDS } from '../../lib/constants'
import { getErrorMessage } from '../../lib/errors'
import { OriginEditor } from '../../components/OriginEditor'
import { useQueryClient } from '@tanstack/react-query'

export function NewApplication() {
  const { owner } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [loading, setLoading] = useState(false)
  const [secret, setSecret] = useState(null)
  const [origins, setOrigins] = useState(['http://localhost:5173'])
  const [fields, setFields] = useState(['NAME', 'EMAIL', 'PASSWORD'])
  const [form, setForm] = useState({
    name: '',
    description: '',
    logoUrl: '',
    googleAudience: '',
    jwtExpirationInMinutes: 15,
    refreshTokenExpirationInDays: 30,
    fromName: '',
    loginUrl: '',
    verificationRedirectUrl: '',
    passwordResetRedirectUrl: '',
  })

  function setField(key, value) {
    setForm((current) => ({ ...current, [key]: value }))
  }

  function toggleField(id) {
    setFields((current) => (current.includes(id) ? current.filter((item) => item !== id) : [...current, id]))
  }

  async function onSubmit(event) {
    event.preventDefault()
    setLoading(true)
    try {
      const { data } = await apps.create({
        name: form.name,
        ownerId: owner.ownerId,
        description: form.description || undefined,
        logoUrl: form.logoUrl || undefined,
        jwtExpirationInMinutes: Number(form.jwtExpirationInMinutes),
        refreshTokenExpirationInDays: Number(form.refreshTokenExpirationInDays),
        allowedOrigins: origins.filter(Boolean),
        googleAudience: form.googleAudience || undefined,
        requiredUserFields: fields,
        emailSettings: {
          fromName: form.fromName || undefined,
          loginUrl: form.loginUrl || undefined,
          verificationRedirectUrl: form.verificationRedirectUrl || undefined,
          passwordResetRedirectUrl: form.passwordResetRedirectUrl || undefined,
        },
      })
      await queryClient.invalidateQueries({ queryKey: ['apps'] })
      setSecret(data)
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-2xl">
      <h1 className="display text-5xl">Nova aplicação</h1>
      <p className="mt-2 text-sm text-ink-muted">O apiSecret aparece uma única vez depois de criar.</p>

      <form onSubmit={onSubmit} className="mt-8 space-y-8">
        <section className="space-y-4">
          <Input label="Nome" value={form.name} onChange={(e) => setField('name', e.target.value)} required />
          <Textarea label="Descrição" value={form.description} onChange={(e) => setField('description', e.target.value)} />
          <Input label="Logo URL" value={form.logoUrl} onChange={(e) => setField('logoUrl', e.target.value)} />
        </section>

        <section className="space-y-3">
          <h2 className="text-sm font-medium">Allowed origins</h2>
          <OriginEditor value={origins} onChange={setOrigins} />
        </section>

        <section className="grid gap-4 sm:grid-cols-2">
          <Input
            label="JWT access (minutos)"
            type="number"
            min="1"
            value={form.jwtExpirationInMinutes}
            onChange={(e) => setField('jwtExpirationInMinutes', e.target.value)}
          />
          <Input
            label="Refresh (dias)"
            type="number"
            min="1"
            value={form.refreshTokenExpirationInDays}
            onChange={(e) => setField('refreshTokenExpirationInDays', e.target.value)}
          />
          <Input
            className="sm:col-span-2"
            label="Google audience (Client ID)"
            value={form.googleAudience}
            onChange={(e) => setField('googleAudience', e.target.value)}
          />
        </section>

        <section>
          <h2 className="mb-3 text-sm font-medium">Campos obrigatórios no cadastro de user</h2>
          <div className="flex flex-wrap gap-2">
            {REQUIRED_FIELDS.map((field) => (
              <button
                key={field.id}
                type="button"
                onClick={() => toggleField(field.id)}
                className={`rounded-sm border px-3 py-1.5 text-xs ${
                  fields.includes(field.id) ? 'border-accent/40 bg-accent/10 text-accent' : 'border-line text-ink-muted'
                }`}
              >
                {field.label}
              </button>
            ))}
          </div>
        </section>

        <section className="space-y-4">
          <h2 className="text-sm font-medium">E-mail / redirects</h2>
          <Input label="From name" value={form.fromName} onChange={(e) => setField('fromName', e.target.value)} />
          <Input label="Login URL" value={form.loginUrl} onChange={(e) => setField('loginUrl', e.target.value)} />
          <Input label="Verification redirect" value={form.verificationRedirectUrl} onChange={(e) => setField('verificationRedirectUrl', e.target.value)} />
          <Input label="Password reset redirect" value={form.passwordResetRedirectUrl} onChange={(e) => setField('passwordResetRedirectUrl', e.target.value)} />
        </section>

        <Button type="submit" disabled={loading}>
          {loading ? 'Criando…' : 'Criar aplicação'}
        </Button>
      </form>

      <Dialog
        open={Boolean(secret)}
        onOpenChange={(open) => {
          if (!open && secret) {
            navigate(`/app/applications/${secret.appId}`)
          }
        }}
        title="Guarde o apiSecret agora"
        description="Ele não será exibido de novo. Use só no servidor da sua aplicação."
        footer={
          <Button onClick={() => navigate(`/app/applications/${secret.appId}`)}>Ir para a aplicação</Button>
        }
      >
        <Callout title="Credenciais" tone="accent">
          Copie e armazene fora do Git.
        </Callout>
        <SecretRow label="appId" value={secret?.appId} />
        <SecretRow label="apiKey" value={secret?.apiKey} />
        <SecretRow label="apiSecret" value={secret?.apiSecret} />
      </Dialog>
    </div>
  )
}

export function SecretRow({ label, value }) {
  return (
    <div className="mt-3">
      <div className="mb-1 text-[11px] tracking-wide text-ink-faint uppercase">{label}</div>
      <button
        type="button"
        className="w-full truncate rounded-sm border border-line bg-surface-2 px-3 py-2 text-left font-mono text-xs"
        onClick={async () => {
          await navigator.clipboard.writeText(value || '')
          toast.success(`${label} copiado`)
        }}
      >
        {value}
      </button>
    </div>
  )
}
