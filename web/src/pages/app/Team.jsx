import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Badge } from '../../components/ui/Badge'
import { Button } from '../../components/ui/Button'
import { owners } from '../../lib/api'
import { useAuth } from '../../lib/auth'
import { getErrorMessage } from '../../lib/errors'

export function Team() {
  const { owner } = useAuth()
  const queryClient = useQueryClient()
  const { data = [], isLoading } = useQuery({
    queryKey: ['owners'],
    queryFn: async () => (await owners.list()).data,
  })

  const disable = useMutation({
    mutationFn: (ownerId) => owners.disable(ownerId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['owners'] })
      toast.success('Owner desativado')
    },
    onError: (error) => toast.error(getErrorMessage(error)),
  })
  const enable = useMutation({
    mutationFn: (ownerId) => owners.enable(ownerId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['owners'] })
      toast.success('Owner reativado')
    },
    onError: (error) => toast.error(getErrorMessage(error)),
  })

  return (
    <div>
      <h1 className="display text-5xl">Time</h1>
      <p className="mt-1 text-sm text-ink-muted">Owners da plataforma. Você não pode desativar a si mesmo.</p>
      <div className="mt-8 space-y-2">
        {isLoading ? <p className="text-sm text-ink-muted">Carregando…</p> : null}
        {data.map((item) => (
          <div key={item.ownerId} className="flex flex-wrap items-center justify-between gap-3 rounded-sm border border-line bg-surface px-5 py-4">
            <div>
              <div className="font-medium">{item.name}</div>
              <div className="text-xs text-ink-faint">
                {item.email?.email} · {item.role}
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Badge tone={item.active === false ? 'danger' : 'ok'}>{item.active === false ? 'Inativo' : 'Ativo'}</Badge>
              {item.ownerId !== owner?.ownerId ? (
                <Button
                  size="sm"
                  variant="secondary"
                  onClick={() => (item.active === false ? enable.mutate(item.ownerId) : disable.mutate(item.ownerId))}
                >
                  {item.active === false ? 'Ativar' : 'Desativar'}
                </Button>
              ) : null}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
