import { cva } from 'class-variance-authority'
import { cn } from '../../lib/cn'

const variants = cva(
  'inline-flex items-center justify-center gap-2 rounded-sm text-sm tracking-tight transition-colors duration-200 disabled:pointer-events-none disabled:opacity-40 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-accent',
  {
    variants: {
      variant: {
        primary: 'bg-accent text-accent-fg hover:bg-accent/90',
        secondary: 'border border-line bg-transparent text-ink hover:border-line-strong hover:bg-surface-2',
        ghost: 'text-ink-muted hover:text-ink',
        danger: 'bg-danger/15 text-danger hover:bg-danger/25',
        outline: 'border border-line text-ink hover:bg-surface-2',
      },
      size: {
        sm: 'h-8 px-3 text-xs',
        md: 'h-10 px-4',
        lg: 'h-12 px-6 text-[15px]',
        icon: 'h-10 w-10',
      },
    },
    defaultVariants: {
      variant: 'primary',
      size: 'md',
    },
  },
)

export function Button({ className, variant, size, type = 'button', ...props }) {
  return <button type={type} className={cn(variants({ variant, size }), className)} {...props} />
}
