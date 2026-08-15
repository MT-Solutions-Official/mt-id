import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '../../components/ui/Button'
import { Input } from '../../components/ui/Input'
import { owners } from '../../lib/api'
import { useAuth } from '../../lib/auth'
import { PASSWORD_HINT } from '../../lib/constants'
import { getErrorMessage } from '../../lib/errors'
import { AuthScreen } from './Login'

export function Signup() {
  const { hasSession } = useAuth()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [form, setForm] = useState({
    name: '',
    email: '',
    phoneNumber: '',
    password: '',
    cpf: '',
  })

  if (hasSession) {
    return <Navigate to="/app" replace />
  }

  function setField(key, value) {
    setForm((current) => ({ ...current, [key]: value }))
  }

  async function onSubmit(event) {
    event.preventDefault()
    setLoading(true)
    try {
      await owners.create({
        name: form.name,
        email: form.email,
        phoneNumber: form.phoneNumber,
        password: form.password,
        document: { cpf: form.cpf },
      })
      toast.success('Owner criado. Verifique o e-mail e entre.')
      navigate('/login')
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthScreen
      title="Criar owner"
      subtitle="O primeiro owner vira OWNER_WRITER. Os próximos só um writer autenticado cria."
    >
      <form onSubmit={onSubmit} className="space-y-4">
        <Input label="Nome" value={form.name} onChange={(e) => setField('name', e.target.value)} required />
        <Input label="E-mail" type="email" value={form.email} onChange={(e) => setField('email', e.target.value)} required />
        <Input label="Telefone" placeholder="+5521999999999" value={form.phoneNumber} onChange={(e) => setField('phoneNumber', e.target.value)} required />
        <Input label="CPF" value={form.cpf} onChange={(e) => setField('cpf', e.target.value)} required />
        <Input
          label="Senha"
          type="password"
          hint={PASSWORD_HINT}
          value={form.password}
          onChange={(e) => setField('password', e.target.value)}
          required
        />
        <Button type="submit" className="w-full" disabled={loading}>
          {loading ? 'Criando…' : 'Criar conta'}
        </Button>
        <p className="text-center text-xs text-ink-muted">
          Já tem conta?{' '}
          <Link to="/login" className="text-ink">
            Entrar
          </Link>
        </p>
      </form>
    </AuthScreen>
  )
}
