import { cn } from '../../lib/cn'

export function Input({ label, hint, error, className, id, ...props }) {
  const inputId = id || props.name
  return (
    <label className={cn('flex min-w-0 flex-1 flex-col gap-1.5 text-sm', className)}>
      {label ? <span className="text-[11px] tracking-[0.16em] text-ink-faint uppercase">{label}</span> : null}
      <input
        id={inputId}
        className={cn(
          'h-11 w-full rounded-sm border border-line bg-bg px-3.5 text-ink outline-none transition-colors placeholder:text-ink-faint focus:border-accent',
          error && 'border-danger/60',
        )}
        {...props}
      />
      {error ? <span className="text-xs text-danger">{error}</span> : hint ? <span className="text-xs text-ink-faint">{hint}</span> : null}
    </label>
  )
}

export function Textarea({ label, className, ...props }) {
  return (
    <label className="flex flex-col gap-1.5 text-sm">
      {label ? <span className="text-[11px] tracking-[0.16em] text-ink-faint uppercase">{label}</span> : null}
      <textarea
        className={cn(
          'min-h-24 rounded-sm border border-line bg-bg px-3.5 py-2.5 text-ink outline-none transition-colors placeholder:text-ink-faint focus:border-accent',
          className,
        )}
        {...props}
      />
    </label>
  )
}
