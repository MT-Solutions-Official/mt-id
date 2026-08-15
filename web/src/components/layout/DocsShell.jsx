import { NavLink, Outlet } from 'react-router-dom'
import { SiteFooter, SiteHeader } from './SiteChrome'
import { docsNav } from '../../content/docs'
import { cn } from '../../lib/cn'

export function DocsShell() {
  return (
    <div className="min-h-screen bg-bg">
      <SiteHeader solid />
      <div className="mx-auto grid w-full max-w-6xl gap-12 px-5 py-12 lg:grid-cols-[200px_minmax(0,1fr)]">
        <aside className="lg:sticky lg:top-24 lg:h-[calc(100vh-8rem)] lg:overflow-y-auto">
          {docsNav.map((group) => (
            <div key={group.title} className="mb-8">
              <div className="mb-3 text-[10px] tracking-[0.2em] text-ink-faint uppercase">{group.title}</div>
              <div className="flex flex-col">
                {group.items.map((item) => (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    end={item.to === '/docs'}
                    className={({ isActive }) =>
                      cn(
                        'border-l border-transparent py-1.5 pl-3 text-[13px] text-ink-muted hover:text-ink',
                        isActive && 'border-accent text-ink',
                      )
                    }
                  >
                    {item.label}
                  </NavLink>
                ))}
              </div>
            </div>
          ))}
        </aside>
        <article className="prose-docs min-w-0 pb-20">
          <Outlet />
        </article>
      </div>
      <SiteFooter />
    </div>
  )
}
