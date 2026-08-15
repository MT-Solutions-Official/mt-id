import { Link, NavLink } from 'react-router-dom'
import { Logo } from '../Logo'
import { Button } from '../ui/Button'
import { useAuth } from '../../lib/auth'
import { cn } from '../../lib/cn'

const links = [
  { to: '/docs', label: 'Docs' },
  { to: '/docs/quickstart', label: 'Quickstart' },
]

export function SiteHeader({ solid = false }) {
  const { hasSession } = useAuth()

  return (
    <header className={cn('sticky top-0 z-40', solid && 'border-b border-line bg-bg/80 backdrop-blur-xl')}>
      <div className="mx-auto flex h-[72px] w-full max-w-6xl items-center justify-between px-5">
        <div className="flex items-center gap-10">
          <Logo />
          <nav className="hidden items-center gap-7 text-[13px] text-ink-muted md:flex">
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={({ isActive }) => cn('transition-colors hover:text-ink', isActive && 'text-ink')}
              >
                {link.label}
              </NavLink>
            ))}
          </nav>
        </div>
        <div className="flex items-center gap-5">
          {hasSession ? (
            <Link to="/app">
              <Button size="sm">Console</Button>
            </Link>
          ) : (
            <>
              <Link to="/login" className="hidden text-[13px] text-ink-muted hover:text-ink sm:inline">
                Entrar
              </Link>
              <Link to="/signup">
                <Button size="sm">Começar</Button>
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}

export function SiteFooter() {
  return (
    <footer className="border-t border-line">
      <div className="mx-auto flex max-w-6xl flex-col gap-2 px-5 py-10 sm:flex-row sm:items-center sm:justify-between">
        <Logo size="sm" />
        <span className="font-mono text-[11px] tracking-wide text-ink-faint">identity layer · localhost:8081</span>
      </div>
    </footer>
  )
}
