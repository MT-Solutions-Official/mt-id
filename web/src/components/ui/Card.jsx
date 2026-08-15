import { cn } from '../../lib/cn'

export function Card({ className, ...props }) {
  return (
    <div
      className={cn(
        'rounded-sm border border-line bg-surface',
        className,
      )}
      {...props}
    />
  )
}

export function CardHeader({ className, ...props }) {
  return <div className={cn('border-b border-line px-6 py-5', className)} {...props} />
}

export function CardBody({ className, ...props }) {
  return <div className={cn('px-6 py-5', className)} {...props} />
}
