import { cn } from '../../lib/cn'

export function Badge({ children, tone = 'default', className }) {
  const tones = {
    default: 'text-ink-muted border-line',
    accent: 'text-accent border-accent/30',
    ok: 'text-ok border-ok/30',
    danger: 'text-danger border-danger/30',
  }
  return (
    <span className={cn('inline-flex items-center rounded-sm border px-2 py-0.5 text-[10px] tracking-[0.14em] uppercase', tones[tone], className)}>
      {children}
    </span>
  )
}
