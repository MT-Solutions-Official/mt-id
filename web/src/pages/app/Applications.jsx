import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import { apps } from '../../lib/api'
import { Badge } from '../../components/ui/Badge'
import { Button } from '../../components/ui/Button'
import { EmptyApps } from './Overview'

export function Applications() {
  const { data: applications = [], isLoading } = useQuery({
    queryKey: ['apps'],
    queryFn: async () => (await apps.list()).data,
  })

  return (
    <div>
      <div className="flex items-end justify-between gap-4">
        <div>
          <h1 className="display text-5xl">Aplicações</h1>
          <p className="mt-1 text-sm text-ink-muted">Cada app isola users, origins e credenciais.</p>
        </div>
        <Link to="/app/applications/new">
          <Button>
            <Plus className="h-4 w-4" />
            Nova
          </Button>
        </Link>
      </div>

      <div className="mt-8">
        {isLoading ? <p className="text-sm text-ink-muted">Carregando…</p> : null}
        {!isLoading && applications.length === 0 ? <EmptyApps /> : null}
        <div className="grid gap-3 md:grid-cols-2">
          {applications.map((app) => (
            <Link
              key={app.appId}
              to={`/app/applications/${app.appId}`}
              className="rounded-sm border border-line bg-surface p-5 hover:border-line-strong"
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h2 className="font-medium">{app.name}</h2>
                  <p className="mt-1 line-clamp-2 text-sm text-ink-muted">{app.description || 'Sem descrição'}</p>
                </div>
                <Badge tone={app.active === false ? 'danger' : 'ok'}>{app.active === false ? 'Inativa' : 'Ativa'}</Badge>
              </div>
              <div className="mt-4 flex flex-wrap gap-2 text-[11px] text-ink-faint">
                <span className="rounded-sm bg-surface-2 px-2 py-1 font-mono">{app.appId}</span>
                {(app.allowedOrigins || []).slice(0, 2).map((origin) => (
                  <span key={origin} className="rounded-sm bg-surface-2 px-2 py-1">
                    {origin}
                  </span>
                ))}
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  )
}
