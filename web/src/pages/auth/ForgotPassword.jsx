import { useState } from 'react'
import { Link } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '../../components/ui/Button'
import { Input } from '../../components/ui/Input'
import { owners } from '../../lib/api'
import { getErrorMessage } from '../../lib/errors'
import { AuthScreen } from './Login'

export function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)

  async function onSubmit(event) {
    event.preventDefault()
    setLoading(true)
    try {
      await owners.forgotPassword(email)
      toast.success('Se a conta existir, o e-mail de reset foi enviado.')
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthScreen title="Reset de senha" subtitle="Enviamos o link se o e-mail existir. A resposta é sempre a mesma.">
      <form onSubmit={onSubmit} className="space-y-4">
        <Input label="E-mail" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <Button type="submit" className="w-full" disabled={loading}>
          {loading ? 'Enviando…' : 'Enviar link'}
        </Button>
        <p className="text-center text-xs text-ink-muted">
          <Link to="/login">Voltar ao login</Link>
        </p>
      </form>
    </AuthScreen>
  )
}
