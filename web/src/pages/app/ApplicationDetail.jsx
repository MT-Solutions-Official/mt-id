import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { apps, roles } from '../../lib/api'
import { REQUIRED_FIELDS } from '../../lib/constants'
import { getErrorMessage } from '../../lib/errors'
import { Badge } from '../../components/ui/Badge'
import { Button } from '../../components/ui/Button'
import { Callout, Dialog } from '../../components/ui/Dialog'
import { Input, Textarea } from '../../components/ui/Input'
import { OriginEditor } from '../../components/OriginEditor'
import { SecretRow } from './NewApplication'

const tabs = ['Geral', 'Auth', 'E-mail', 'Equipe', 'Papéis', 'Credenciais']

export function ApplicationDetail() {
  const { appId } = useParams()
  const queryClient = useQueryClient()
  const [tab, setTab] = useState('Geral')
  const [secret, setSecret] = useState(null)
  const [form, setForm] = useState(null)

  const appQuery = useQuery({
    queryKey: ['apps', appId],
    queryFn: async () => (await apps.get(appId)).data,
  })
  const rolesQuery = useQuery({
    queryKey: ['roles', appId],
    queryFn: async () => (await roles.list(appId)).data,
  })

  useEffect(() => {
    if (appQuery.data) {
      const app = appQuery.data
      setForm({
        name: app.name || '',
        description: app.description || '',
        logoUrl: app.logoUrl || '',
        googleAudience: app.googleAudience || '',
        jwtExpirationInMinutes: app.jwtExpirationInMinutes || 15,
        refreshTokenExpirationInDays: app.refreshTokenExpirationInDays || 30,
        allowedOrigins: app.allowedOrigins?.length ? app.allowedOrigins : [''],
        requiredUserFields: app.requiredUserFields || [],
        fromName: app.emailSettings?.fromName || '',
        fromEmail: app.emailSettings?.fromEmail || '',
        replyTo: app.emailSettings?.replyTo || '',
        supportEmail: app.emailSettings?.supportEmail || '',
        supportUrl: app.emailSettings?.supportUrl || '',
        loginUrl: app.emailSettings?.loginUrl || '',
        verificationRedirectUrl: app.emailSettings?.verificationRedirectUrl || '',
        passwordResetRedirectUrl: app.emailSettings?.passwordResetRedirectUrl || '',
      })
    }
  }, [appQuery.data])

  const save = useMutation({
    mutationFn: async () =>
      (
        await apps.update({
          appId,
          name: form.name,
          description: form.description,
          logoUrl: form.logoUrl,
          googleAudience: form.googleAudience,
          jwtExpirationInMinutes: Number(form.jwtExpirationInMinutes),
          refreshTokenExpirationInDays: Number(form.refreshTokenExpirationInDays),
          allowedOrigins: form.allowedOrigins.filter(Boolean),
          requiredUserFields: form.requiredUserFields,
          emailSettings: {
            fromName: form.fromName || undefined,
            fromEmail: form.fromEmail || undefined,
            replyTo: form.replyTo || undefined,
            supportEmail: form.supportEmail || undefined,
            supportUrl: form.supportUrl || undefined,
            loginUrl: form.loginUrl || undefined,
            verificationRedirectUrl: form.verificationRedirectUrl || undefined,
            passwordResetRedirectUrl: form.passwordResetRedirectUrl || undefined,
          },
        })
      ).data,
    onSuccess: (data) => {
      queryClient.setQueryData(['apps', appId], data)
      queryClient.invalidateQueries({ queryKey: ['apps'] })
      toast.success('Settings salvos')
    },
    onError: (error) => toast.error(getErrorMessage(error)),
  })

  if (appQuery.isLoading || !form) {
    return <p className="text-sm text-ink-muted">Carregando…</p>
  }
  if (appQuery.isError) {
    return <p className="text-sm text-danger">{getErrorMessage(appQuery.error)}</p>
  }

  const app = appQuery.data

  function setField(key, value) {
    setForm((current) => ({ ...current, [key]: value }))
  }

  function toggleField(id) {
    setForm((current) => ({
      ...current,
      requiredUserFields: current.requiredUserFields.includes(id)
        ? current.requiredUserFields.filter((item) => item !== id)
        : [...current.requiredUserFields, id],
    }))
  }

  return (
    <div>
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <Link to="/app/applications" className="text-xs text-ink-faint hover:text-ink">
            ← Aplicações
          </Link>
          <h1 className="display mt-2 text-4xl md:text-5xl">{app.name}</h1>
          <p className="mt-1 font-mono text-xs text-ink-faint">{app.appId}</p>
        </div>
        <Badge tone={app.active === false ? 'danger' : 'ok'}>{app.active === false ? 'Inativa' : 'Ativa'}</Badge>
      </div>

      <div className="mt-6 flex gap-1 overflow-x-auto border-b border-line">
        {tabs.map((item) => (
          <button
            key={item}
            type="button"
            onClick={() => setTab(item)}
            className={`px-3 py-2 text-sm ${tab === item ? 'border-b-2 border-accent text-ink' : 'text-ink-muted'}`}
          >
            {item}
          </button>
        ))}
      </div>

      <form
        className="mt-8 max-w-2xl space-y-6"
        onSubmit={(event) => {
          event.preventDefault()
          save.mutate()
        }}
      >
        {tab === 'Geral' ? (
          <>
            <Input label="Nome" value={form.name} onChange={(e) => setField('name', e.target.value)} />
            <Textarea label="Descrição" value={form.description} onChange={(e) => setField('description', e.target.value)} />
            <Input label="Logo URL" value={form.logoUrl} onChange={(e) => setField('logoUrl', e.target.value)} />
          </>
        ) : null}

        {tab === 'Auth' ? (
          <>
            <div>
              <div className="mb-2 text-sm text-ink-muted">Allowed origins</div>
              <OriginEditor value={form.allowedOrigins} onChange={(value) => setField('allowedOrigins', value)} />
            </div>
            <Input label="Google audience" value={form.googleAudience} onChange={(e) => setField('googleAudience', e.target.value)} />
            <div className="grid gap-4 sm:grid-cols-2">
              <Input label="JWT minutos" type="number" value={form.jwtExpirationInMinutes} onChange={(e) => setField('jwtExpirationInMinutes', e.target.value)} />
              <Input label="Refresh dias" type="number" value={form.refreshTokenExpirationInDays} onChange={(e) => setField('refreshTokenExpirationInDays', e.target.value)} />
            </div>
            <div>
              <div className="mb-3 text-sm text-ink-muted">requiredUserFields</div>
              <div className="flex flex-wrap gap-2">
                {REQUIRED_FIELDS.map((field) => (
                  <button
                    key={field.id}
                    type="button"
                    onClick={() => toggleField(field.id)}
                    className={`rounded-sm border px-3 py-1.5 text-xs ${
                      form.requiredUserFields.includes(field.id)
                        ? 'border-accent/40 bg-accent/10 text-accent'
                        : 'border-line text-ink-muted'
                    }`}
                  >
                    {field.label}
                  </button>
                ))}
              </div>
            </div>
          </>
        ) : null}

        {tab === 'E-mail' ? (
          <>
            <Input label="From name" value={form.fromName} onChange={(e) => setField('fromName', e.target.value)} />
            <Input label="From email (display)" value={form.fromEmail} onChange={(e) => setField('fromEmail', e.target.value)} />
            <Input label="Reply-to" value={form.replyTo} onChange={(e) => setField('replyTo', e.target.value)} />
            <Input label="Support email" value={form.supportEmail} onChange={(e) => setField('supportEmail', e.target.value)} />
            <Input label="Support URL" value={form.supportUrl} onChange={(e) => setField('supportUrl', e.target.value)} />
            <Input label="Login URL" value={form.loginUrl} onChange={(e) => setField('loginUrl', e.target.value)} />
            <Input label="Verification redirect" value={form.verificationRedirectUrl} onChange={(e) => setField('verificationRedirectUrl', e.target.value)} />
            <Input label="Password reset redirect" value={form.passwordResetRedirectUrl} onChange={(e) => setField('passwordResetRedirectUrl', e.target.value)} />
            <Callout title="Origins">Redirects precisam bater com allowedOrigins. Sem URL, o MT ID usa as páginas HTML internas.</Callout>
          </>
        ) : null}

        {tab === 'Equipe' ? <OwnersPanel app={app} appId={appId} /> : null}
        {tab === 'Papéis' ? <RolesPanel appId={appId} items={rolesQuery.data || []} /> : null}

        {tab === 'Credenciais' ? (
          <div className="space-y-4">
            <SecretRow label="appId" value={app.appId} />
            <SecretRow label="apiKey" value={app.apiKey} />
            <Callout title="apiSecret">Não fica armazenado em texto. Gere um novo se perdeu o original.</Callout>
            <div className="flex flex-wrap gap-2">
              <Button
                type="button"
                variant="secondary"
                onClick={async () => {
                  try {
                    const { data } = await apps.rotateSecret(appId)
                    setSecret(data)
                    toast.success('Secret rotacionado')
                  } catch (error) {
                    toast.error(getErrorMessage(error))
                  }
                }}
              >
                Rotacionar secret
              </Button>
              <Button
                type="button"
                variant={app.active === false ? 'primary' : 'danger'}
                onClick={async () => {
                  try {
                    const { data } = app.active === false ? await apps.enable(appId) : await apps.disable(appId)
                    queryClient.setQueryData(['apps', appId], data)
                    toast.success(app.active === false ? 'App ativada' : 'App desativada')
                  } catch (error) {
                    toast.error(getErrorMessage(error))
                  }
                }}
              >
                {app.active === false ? 'Ativar app' : 'Desativar app'}
              </Button>
            </div>
          </div>
        ) : null}

        {['Geral', 'Auth', 'E-mail'].includes(tab) ? (
          <Button type="submit" disabled={save.isPending}>
            {save.isPending ? 'Salvando…' : 'Salvar'}
          </Button>
        ) : null}
      </form>

      <Dialog open={Boolean(secret)} onOpenChange={() => setSecret(null)} title="Novo apiSecret">
        <SecretRow label="apiSecret" value={secret?.apiSecret} />
      </Dialog>
    </div>
  )
}

function OwnersPanel({ app, appId }) {
  const queryClient = useQueryClient()
  const [email, setEmail] = useState('')

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        {(app.owners || []).map((owner) => (
          <div key={owner.ownerId} className="flex items-center justify-between rounded-sm border border-line px-4 py-3">
            <div>
              <div className="text-sm font-medium">{owner.name}</div>
              <div className="text-xs text-ink-faint">{owner.email?.email}</div>
            </div>
            <Button
              type="button"
              size="sm"
              variant="ghost"
              onClick={async () => {
                try {
                  const { data } = await apps.removeOwner(appId, owner.ownerId)
                  queryClient.setQueryData(['apps', appId], data)
                  toast.success('Owner removido')
                } catch (error) {
                  toast.error(getErrorMessage(error))
                }
              }}
            >
              Remover
            </Button>
          </div>
        ))}
      </div>
      <div className="flex gap-2">
        <Input placeholder="owner@empresa.com" value={email} onChange={(e) => setEmail(e.target.value)} />
        <Button
          type="button"
          variant="secondary"
          onClick={async () => {
            try {
              await apps.addOwners({ appId, emails: [email] })
              await queryClient.invalidateQueries({ queryKey: ['apps', appId] })
              setEmail('')
              toast.success('Owner adicionado')
            } catch (error) {
              toast.error(getErrorMessage(error))
            }
          }}
        >
          Adicionar
        </Button>
      </div>
    </div>
  )
}

function RolesPanel({ appId, items }) {
  const queryClient = useQueryClient()
  const [roleName, setRoleName] = useState('')

  return (
    <div className="space-y-4">
      <p className="text-sm text-ink-muted">Papéis da sua app entram no JWT do user (`groups` / `roles`). Nomes reservados do IdP não são aceitos.</p>
      <div className="space-y-2">
        {items.map((role) => (
          <div key={role.userRoleId} className="flex items-center justify-between rounded-sm border border-line px-4 py-3">
            <span className="font-mono text-sm">{role.roleName}</span>
            <Button
              type="button"
              size="sm"
              variant="ghost"
              onClick={async () => {
                try {
                  await roles.remove(role.userRoleId)
                  await queryClient.invalidateQueries({ queryKey: ['roles', appId] })
                } catch (error) {
                  toast.error(getErrorMessage(error))
                }
              }}
            >
              Excluir
            </Button>
          </div>
        ))}
      </div>
      <div className="flex gap-2">
        <Input placeholder="ADMIN" value={roleName} onChange={(e) => setRoleName(e.target.value)} />
        <Button
          type="button"
          variant="secondary"
          onClick={async () => {
            try {
              await roles.create(appId, roleName)
              setRoleName('')
              await queryClient.invalidateQueries({ queryKey: ['roles', appId] })
              toast.success('Papel criado')
            } catch (error) {
              toast.error(getErrorMessage(error))
            }
          }}
        >
          Criar
        </Button>
      </div>
    </div>
  )
}
