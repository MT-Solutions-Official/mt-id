import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { GoogleLogin } from '@react-oauth/google'
import { toast } from 'sonner'
import { Logo } from '../../components/Logo'
import { Button } from '../../components/ui/Button'
import { Input } from '../../components/ui/Input'
import { authError, useAuth } from '../../lib/auth'

export function Login() {
  const { hasSession, login, loginWithGoogle } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const googleId = import.meta.env.VITE_GOOGLE_CLIENT_ID

  if (hasSession) {
    return <Navigate to="/app" replace />
  }

  async function onSubmit(event) {
    event.preventDefault()
    setLoading(true)
    try {
      await login({ email, password })
      navigate('/app')
    } catch (error) {
      toast.error(authError(error))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthScreen title="Entrar" subtitle="Console do owner.">
      <form onSubmit={onSubmit} className="space-y-5">
        <Input label="E-mail" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <Input label="Senha" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <div className="flex items-center justify-between text-[13px] text-ink-muted">
          <Link to="/forgot-password" className="hover:text-ink">
            Esqueci a senha
          </Link>
          <Link to="/signup" className="hover:text-ink">
            Criar owner
          </Link>
        </div>
        <Button type="submit" className="w-full" disabled={loading}>
          {loading ? 'Entrando…' : 'Entrar'}
        </Button>
      </form>
      {googleId ? (
        <div className="mt-8">
          <div className="mb-4 text-center font-mono text-[10px] tracking-[0.2em] text-ink-faint uppercase">ou</div>
          <div className="flex justify-center">
            <GoogleLogin
              onSuccess={async (response) => {
                try {
                  await loginWithGoogle(response.credential)
                  navigate('/app')
                } catch (error) {
                  toast.error(authError(error))
                }
              }}
              onError={() => toast.error('Google login cancelado.')}
              theme="filled_black"
              shape="rectangular"
            />
          </div>
        </div>
      ) : null}
    </AuthScreen>
  )
}

export function AuthScreen({ title, subtitle, children }) {
  return (
    <div className="relative min-h-screen bg-bg">
      <div className="pointer-events-none absolute inset-0 vignette" />
      <div className="relative flex h-[72px] items-center px-5">
        <Logo />
      </div>
      <div className="relative mx-auto w-full max-w-md px-5 pt-16">
        <h1 className="display text-5xl">{title}</h1>
        <p className="mt-3 text-sm text-ink-muted">{subtitle}</p>
        <div className="mt-10">{children}</div>
      </div>
    </div>
  )
}
