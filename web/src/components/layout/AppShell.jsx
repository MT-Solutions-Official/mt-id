import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { AppWindow, BookOpen, LayoutDashboard, LogOut, Users } from 'lucide-react'
import { Logo } from '../Logo'
import { useAuth } from '../../lib/auth'
import { cn } from '../../lib/cn'

const nav = [
  { to: '/app', label: 'Visão geral', icon: LayoutDashboard, end: true },
  { to: '/app/applications', label: 'Aplicações', icon: AppWindow },
  { to: '/app/team', label: 'Time', icon: Users },
  { to: '/docs', label: 'Docs', icon: BookOpen },
]

export function AppShell() {
  const { owner, logout } = useAuth()
  const navigate = useNavigate()

  async function onLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen bg-bg lg:grid lg:grid-cols-[220px_1fr]">
      <aside className="hidden border-r border-line lg:flex lg:flex-col">
        <div className="flex h-[72px] items-center px-6">
          <Logo />
        </div>
        <nav className="flex flex-1 flex-col gap-0.5 px-3 py-2">
          {nav.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-2.5 px-3 py-2 text-[13px] text-ink-muted hover:text-ink',
                  isActive && 'bg-surface text-ink',
                )
              }
            >
              <item.icon className="h-3.5 w-3.5" />
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="border-t border-line p-5">
          <div className="mb-3 truncate font-mono text-[11px] text-ink-faint">{owner?.email?.email || owner?.name}</div>
          <button type="button" onClick={onLogout} className="inline-flex items-center gap-2 text-[13px] text-ink-muted hover:text-ink">
            <LogOut className="h-3.5 w-3.5" />
            Sair
          </button>
        </div>
      </aside>
      <div className="min-w-0">
        <header className="border-b border-line lg:hidden">
          <div className="flex h-[72px] items-center px-5">
            <Logo />
          </div>
          <nav className="flex gap-1 overflow-x-auto px-3 pb-3">
            {nav.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  cn('shrink-0 px-3 py-1.5 text-xs text-ink-muted', isActive && 'bg-surface text-ink')
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </header>
        <main className="mx-auto w-full max-w-5xl px-5 py-10">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
