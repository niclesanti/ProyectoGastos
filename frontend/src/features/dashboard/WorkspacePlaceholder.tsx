import type { ReactNode } from 'react'
import { Card } from '@/components/ui/card'
import { Shovel } from 'lucide-react'

interface WorkspacePlaceholderProps {
  icon?: ReactNode
  title?: string
  description?: string
}

export function WorkspacePlaceholder({
  icon,
  title = 'Selecciona un espacio de trabajo',
  description = 'Para visualizar tus estadísticas y movimientos, primero elige un espacio en el menú lateral o crea uno nuevo.',
}: WorkspacePlaceholderProps = {}) {
  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-200px)] p-6">
      <Card className="max-w-2xl w-full border-dashed border-2 bg-zinc-950/50 p-12">
        <div className="flex flex-col items-center text-center space-y-6">
          <div className="rounded-full bg-zinc-900 p-6">
            {icon ?? <Shovel className="h-16 w-16 text-muted-foreground/50" />}
          </div>
          
          <div className="space-y-2">
            <h3 className="text-2xl font-semibold tracking-tight">
              {title}
            </h3>
            <p className="text-muted-foreground max-w-md">
              {description}
            </p>
          </div>

          <div className="pt-4">
            <div className="inline-flex items-center gap-2 text-sm text-muted-foreground">
              <div className="h-1.5 w-1.5 rounded-full bg-muted-foreground/50" />
              <span>Usa el menú lateral para comenzar</span>
            </div>
          </div>
        </div>
      </Card>
    </div>
  )
}
