import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowUpRight, Plus } from 'lucide-react'
import { apps } from '../../lib/api'
import { useAuth } from '../../lib/auth'
import { Button } from '../../components/ui/Button'
import { Badge } from '../../components/ui/Badge'

export function Overview() {
  const { owner } = useAuth()
  const { data: applications = [] } = useQuery({
    queryKey: ['apps'],
    queryFn: async () => (await apps.list()).data,
  })

  return (
    <div>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm text-ink-muted">Olá, {owner?.name?.split(' ')[0] || 'owner'}</p>
          <h1 className="display mt-1 text-5xl">Console</h1>
        </div>
        <Link to="/app/applications/new">
          <Button>
            <Plus className="h-4 w-4" />
            Nova aplicação
          </Button>
        </Link>
      </div>

      <div className="mt-8 grid gap-4 md:grid-cols-3">
        <Stat label="Aplicações" value={applications.length} />
        <Stat label="Papel" value={owner?.role || '—'} />
        <Stat label="E-mail" value={owner?.email?.verified ? 'Verificado' : 'Pendente'} />
      </div>

      <div className="mt-10">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-medium">Aplicações recentes</h2>
          <Link to="/app/applications" className="text-sm text-ink-muted hover:text-ink">
            Ver todas
          </Link>
        </div>
        {applications.length === 0 ? (
          <EmptyApps />
        ) : (
          <div className="grid gap-3">
            {applications.slice(0, 4).map((app) => (
              <Link
                key={app.appId}
                to={`/app/applications/${app.appId}`}
                className="flex items-center justify-between rounded-sm border border-line bg-surface px-5 py-4 hover:border-line-strong"
              >
                <div>
                  <div className="font-medium">{app.name}</div>
                  <div className="mt-1 font-mono text-xs text-ink-faint">{app.appId}</div>
                </div>
                <div className="flex items-center gap-3">
                  <Badge tone={app.active === false ? 'danger' : 'ok'}>{app.active === false ? 'Inativa' : 'Ativa'}</Badge>
                  <ArrowUpRight className="h-4 w-4 text-ink-faint" />
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

function Stat({ label, value }) {
  return (
    <div className="rounded-sm border border-line bg-surface px-5 py-4">
      <div className="text-xs text-ink-faint">{label}</div>
      <div className="mt-1 truncate text-lg font-medium">{value}</div>
    </div>
  )
}

export function EmptyApps() {
  return (
    <div className="rounded-sm border border-dashed border-line px-6 py-12 text-center">
      <p className="text-sm text-ink-muted">Nenhuma aplicação ainda. Crie a primeira para obter appId, apiKey e apiSecret.</p>
      <Link to="/app/applications/new" className="mt-4 inline-block">
        <Button size="sm">Criar aplicação</Button>
      </Link>
    </div>
  )
}
