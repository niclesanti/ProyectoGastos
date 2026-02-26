import { useEffect, useRef } from 'react'
import { ScrollArea } from '@/components/ui/scroll-area'
import { MessageBubble } from './MessageBubble'
import type { AgenteIAMensaje, AgenteIAEstado } from '@/types'
import { BrainCircuit } from 'lucide-react'
import { motion } from 'framer-motion'

interface ChatMessagesProps {
  mensajes: AgenteIAMensaje[]
  estado: AgenteIAEstado
}

export function ChatMessages({ mensajes, estado }: ChatMessagesProps) {
  const scrollAreaRef = useRef<HTMLDivElement>(null)
  const bottomRef = useRef<HTMLDivElement>(null)

  // Auto-scroll al final cuando llegan nuevos mensajes o tokens
  useEffect(() => {
    if (bottomRef.current) {
      bottomRef.current.scrollIntoView({ behavior: 'smooth', block: 'end' })
    }
  }, [mensajes, estado])

  // Obtener el último mensaje para saber si está streaming
  const ultimoMensaje = mensajes[mensajes.length - 1]
  const isStreaming = estado === 'streaming' && ultimoMensaje?.role === 'assistant'

  return (
    <ScrollArea className="flex-1 px-4 md:px-6 lg:px-8" ref={scrollAreaRef}>
      <div className="max-w-4xl mx-auto py-6 space-y-4">
        {/* Renderizar mensajes — skip the empty placeholder assistant bubble while thinking */}
        {mensajes.map((mensaje, index) => {
          const esUltimoMensaje = index === mensajes.length - 1
          if (esUltimoMensaje && estado === 'thinking' && mensaje.role === 'assistant' && !mensaje.content) {
            return null
          }
          return (
            <MessageBubble
              key={mensaje.id}
              mensaje={mensaje}
              isStreaming={esUltimoMensaje && isStreaming}
            />
          )
        })}

        {/* Indicador de "pensando" — hybrid: pulsing avatar + bouncing dots */}
        {estado === 'thinking' && (
          <motion.div
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3 }}
            className="flex flex-col gap-2 mb-6"
          >
            {/* Header row — same layout as MessageBubble agent */}
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-full bg-violet-500/10 flex items-center justify-center shrink-0 animate-avatar-pulse">
                <BrainCircuit className="w-4 h-4 text-violet-400" />
              </div>
              <span className="text-sm font-bold text-violet-400">Finanzas Copilot</span>
            </div>

            {/* Bouncing dots — indented to align with name */}
            <div className="pl-10">
              <div className="flex items-center gap-1.5 py-1">
                <span className="w-2 h-2 rounded-full bg-zinc-500 animate-bounce [animation-delay:-0.3s]" />
                <span className="w-2 h-2 rounded-full bg-zinc-500 animate-bounce [animation-delay:-0.15s]" />
                <span className="w-2 h-2 rounded-full bg-zinc-500 animate-bounce" />
              </div>
            </div>
          </motion.div>
        )}

        {/* Indicador de error */}
        {estado === 'error' && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="flex gap-3 mb-4"
          >
            <div className="flex-shrink-0">
              <div className="w-8 h-8 rounded-full bg-destructive/10 flex items-center justify-center">
                <BrainCircuit className="w-4 h-4 text-destructive" />
              </div>
            </div>
            <div className="flex flex-col gap-1 max-w-[75%]">
              <div className="rounded-2xl bg-destructive/10 text-destructive px-4 py-3 rounded-bl-sm">
                <p className="text-sm">
                  Ocurrió un error al procesar tu mensaje. Por favor, intenta nuevamente.
                </p>
              </div>
            </div>
          </motion.div>
        )}

        {/* Referencia para auto-scroll */}
        <div ref={bottomRef} />
      </div>
    </ScrollArea>
  )
}
