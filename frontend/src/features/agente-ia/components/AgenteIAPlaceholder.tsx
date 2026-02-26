import { Card } from '@/components/ui/card'
import { BrainCircuit } from 'lucide-react'

export function AgenteIAPlaceholder() {
  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-200px)] p-6">
      <Card className="max-w-2xl w-full border-dashed border-2 bg-zinc-950/50 p-12">
        <div className="flex flex-col items-center text-center space-y-6">
          <div className="rounded-full bg-primary/10 p-6">
            <BrainCircuit className="h-16 w-16 text-primary" />
          </div>
          
          <div className="space-y-2">
            <h3 className="text-2xl font-semibold tracking-tight">
              Finanzas Copilot
            </h3>
            <p className="text-muted-foreground max-w-md">
              Para interactuar con tu asistente inteligente, primero selecciona un espacio de trabajo en el menú lateral.
            </p>
          </div>

          <div className="pt-4">
            <div className="inline-flex items-center gap-2 text-sm text-muted-foreground">
              <div className="h-1.5 w-1.5 rounded-full bg-muted-foreground/50" />
              <span>El agente necesita contexto financiero para ayudarte</span>
            </div>
          </div>
        </div>
      </Card>
    </div>
  )
}
