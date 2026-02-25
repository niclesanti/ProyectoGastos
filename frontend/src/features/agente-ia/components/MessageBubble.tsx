import { BrainCircuit, User } from 'lucide-react'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { motion } from 'framer-motion'
import { useAuth } from '@/contexts/AuthContext'
import type { AgenteIAMensaje } from '@/types'
import ReactMarkdown from 'react-markdown'

interface MessageBubbleProps {
  mensaje: AgenteIAMensaje
  isStreaming?: boolean
}

// Mapeo de nombres de funciones a labels en español
const functionLabels: Record<string, string> = {
  'obtenerDashboardFinanciero': 'Consultó saldos',
  'buscarTransacciones': 'Buscó transacciones',
  'listarTarjetasCredito': 'Consultó tarjetas',
  'listarResumenesTarjetas': 'Consultó resúmenes',
  'listarCuentasBancarias': 'Consultó cuentas',
  'listarMotivosTransacciones': 'Consultó categorías',
}

export function MessageBubble({ mensaje, isStreaming = false }: MessageBubbleProps) {
  const { user } = useAuth()
  const isUser = mensaje.role === 'user'

  // ── USER message: right-aligned rounded bubble ──────────────────────────────
  if (isUser) {
    return (
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        className="flex gap-3 mb-6 flex-row-reverse"
      >
        <Avatar className="w-8 h-8 shrink-0">
          <AvatarImage src={user?.fotoPerfil} alt={user?.nombre || 'Usuario'} />
          <AvatarFallback>
            <User className="w-4 h-4" />
          </AvatarFallback>
        </Avatar>

        <div className="max-w-[80%] md:max-w-[65%]">
          <div className="rounded-2xl rounded-tr-sm px-4 py-3 bg-zinc-800 text-foreground break-words">
            <p className="text-sm whitespace-pre-wrap leading-relaxed">{mensaje.content}</p>
          </div>
        </div>
      </motion.div>
    )
  }

  // ── AGENT message: flat on background, avatar + name on same row ─────────────
  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      className="flex flex-col gap-2 mb-6"
    >
      {/* Header row: avatar + name perfectly aligned */}
      <div className="flex items-center gap-2.5">
        <div className="w-8 h-8 rounded-full bg-violet-500/10 flex items-center justify-center shrink-0">
          <BrainCircuit className="w-4 h-4 text-violet-400" />
        </div>
        <span className="text-sm font-bold text-violet-400">
          Finanzas Copilot
        </span>
      </div>

      {/* Content column — indented to align with name */}
      <div className="flex flex-col gap-2 pl-10 min-w-0">

        {/* Plain text — no bubble */}
        <div className="prose prose-sm dark:prose-invert max-w-none prose-p:my-2 prose-ul:my-2 prose-ol:my-2 prose-li:my-0 break-words">
          <ReactMarkdown
            components={{
              p: ({ children }) => <p className="text-sm leading-relaxed">{children}</p>,
              ul: ({ children }) => <ul className="text-sm space-y-1 ml-4">{children}</ul>,
              ol: ({ children }) => <ol className="text-sm space-y-1 ml-4">{children}</ol>,
              li: ({ children }) => <li className="text-sm">{children}</li>,
              strong: ({ children }) => <strong className="font-semibold text-foreground">{children}</strong>,
              a: ({ href, children }) => (
                <a href={href} className="text-primary hover:underline" target="_blank" rel="noopener noreferrer">
                  {children}
                </a>
              ),
            }}
          >
            {mensaje.content}
          </ReactMarkdown>

          {/* Blinking cursor during streaming */}
          {isStreaming && (
            <motion.span
              animate={{ opacity: [1, 0, 1] }}
              transition={{ duration: 0.8, repeat: Infinity }}
              className="inline-block w-2 h-4 bg-violet-400 ml-1 align-middle rounded-sm"
            />
          )}
        </div>

        {/* Function call badges */}
        {mensaje.functionsCalled && mensaje.functionsCalled.length > 0 && (
          <div className="flex flex-wrap gap-1.5 mt-1">
            {mensaje.functionsCalled.map((fn, index) => (
              <Badge
                key={index}
                variant="secondary"
                className="text-xs px-2 py-0.5"
              >
                {functionLabels[fn] || fn}
              </Badge>
            ))}
          </div>
        )}

        {/* Token count on hover */}
        {mensaje.tokensUsed && (
          <p className="text-xs text-muted-foreground/50 opacity-0 hover:opacity-100 transition-opacity">
            {mensaje.tokensUsed} tokens
          </p>
        )}
      </div>
    </motion.div>
  )
}
