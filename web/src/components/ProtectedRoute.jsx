import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../lib/auth'

export function ProtectedRoute() {
  const { hasSession, loading } = useAuth()
  if (!hasSession) {
    return <Navigate to="/login" replace />
  }
  if (loading) {
    return (
      <div className="grid min-h-screen place-items-center text-sm text-ink-muted">
        Carregando sessão…
      </div>
    )
  }
  return <Outlet />
}
