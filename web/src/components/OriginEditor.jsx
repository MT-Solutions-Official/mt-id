import { X } from 'lucide-react'
import { Input } from './ui/Input'

export function OriginEditor({ value, onChange }) {
  function update(index, next) {
    const copy = [...value]
    copy[index] = next
    onChange(copy)
  }

  return (
    <div className="space-y-2">
      {value.map((origin, index) => (
        <div key={index} className="flex gap-2">
          <Input
            className="flex-1"
            placeholder="https://app.seudominio.com"
            value={origin}
            onChange={(e) => update(index, e.target.value)}
          />
          <button
            type="button"
            className="grid h-11 w-11 place-items-center rounded-sm border border-line text-ink-faint hover:text-ink"
            onClick={() => onChange(value.filter((_, i) => i !== index))}
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      ))}
      <button type="button" className="text-xs text-ink-muted hover:text-ink" onClick={() => onChange([...value, ''])}>
        + origin
      </button>
    </div>
  )
}
