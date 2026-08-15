import { Link } from 'react-router-dom'
import { cn } from '../lib/cn'

export function Logo({ className, size = 'md' }) {
  const sizes = {
    sm: 'text-[22px]',
    md: 'text-[26px]',
    lg: 'text-4xl',
  }
  return (
    <Link to="/" className={cn('wordmark text-ink', sizes[size], className)}>
      MT ID
    </Link>
  )
}
