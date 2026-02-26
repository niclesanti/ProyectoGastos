import { useAppStore } from '@/store/app-store'
import { useAgenteIA } from '@/hooks/useAgenteIA'
import { ChatWelcome } from '@/features/agente-ia/components/ChatWelcome'
import { ChatMessages } from '@/features/agente-ia/components/ChatMessages'
import { ChatInput } from '@/features/agente-ia/components/ChatInput'
import { AgenteIAPlaceholder } from '@/features/agente-ia/components/AgenteIAPlaceholder'
import { Button } from '@/components/ui/button'
import { RotateCcw } from 'lucide-react'

// dvh = dynamic viewport height (shrinks when mobile keyboard is up)
const PAGE_HEIGHT = 'h-[calc(100dvh-4rem)]'

export default function AgenteIAPage() {
  const currentWorkspace = useAppStore(state => state.currentWorkspace)
  const { mensajes, estado, enviarMensaje, limpiarConversacion } = useAgenteIA()

  if (!currentWorkspace) {
    return <AgenteIAPlaceholder />
  }

  // Welcome screen — no header, ChatWelcome owns the greeting
  if (mensajes.length === 0) {
    return (
      <div className={`flex flex-col ${PAGE_HEIGHT}`}>
        <div className="flex-1 overflow-hidden">
          <ChatWelcome onSuggestionClick={enviarMensaje} />
        </div>
        <ChatInput onSend={enviarMensaje} disabled={estado === 'streaming' || estado === 'thinking'} />
      </div>
    )
  }

  // Conversation screen — compact header + messages + input
  return (
    <div className={`flex flex-col ${PAGE_HEIGHT}`}>
      {/* Compact header */}
      <div className="flex items-center justify-between px-4 md:px-6 py-2.5 border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 shrink-0">
        <div className="flex items-center gap-2">
          <h2 className="text-base font-semibold leading-tight">Finanzas Copilot</h2>
        </div>

        <Button
          variant="ghost"
          size="sm"
          onClick={limpiarConversacion}
          disabled={estado === 'streaming' || estado === 'thinking'}
          className="gap-2 text-muted-foreground hover:text-foreground"
        >
          <RotateCcw className="h-3.5 w-3.5" />
          <span className="hidden sm:inline text-xs">Nueva conversación</span>
        </Button>
      </div>

      <ChatMessages mensajes={mensajes} estado={estado} />
      <ChatInput onSend={enviarMensaje} disabled={estado === 'streaming' || estado === 'thinking'} />
    </div>
  )
}
