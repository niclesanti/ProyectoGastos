import React from 'react'
import { BrainCircuit, User } from 'lucide-react'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { motion } from 'framer-motion'
import { useAuth } from '@/contexts/AuthContext'
import type { AgenteIAMensaje } from '@/types'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'

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
  'listarResumenesPorTarjeta': 'Consultó historial de resúmenes',
  'listarCuotasPorTarjeta': 'Consultó cuotas de tarjeta',
  'buscarTodasComprasCredito': 'Consultó compras en cuotas',
  'listarComprasCreditoPendientes': 'Consultó cuotas pendientes',
  'listarCuentasBancarias': 'Consultó cuentas',
  'listarMotivosTransacciones': 'Consultó categorías',
  'listarContactosTransaccion': 'Consultó contactos',
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
        <div className="prose prose-sm dark:prose-invert max-w-none break-words
          prose-p:my-1.5 prose-p:leading-relaxed
          prose-ul:my-1.5 prose-ol:my-1.5 prose-li:my-0.5
          prose-headings:text-foreground prose-headings:font-bold
          prose-h1:text-xl prose-h2:text-base prose-h3:text-sm
          prose-strong:text-foreground prose-strong:font-semibold
          prose-code:text-violet-300 prose-code:bg-zinc-800 prose-code:px-1 prose-code:py-0.5 prose-code:rounded prose-code:text-xs prose-code:before:content-none prose-code:after:content-none
          prose-pre:bg-zinc-900 prose-pre:border prose-pre:border-zinc-700/50 prose-pre:rounded-xl prose-pre:text-xs
          prose-table:text-xs prose-th:text-left prose-th:font-semibold
          prose-a:text-primary prose-a:no-underline hover:prose-a:underline">
          <ReactMarkdown
            remarkPlugins={[remarkGfm]}
            components={{
              // Headings
              h1: ({ children }) => <h1 className="text-xl font-bold mt-4 mb-2 text-foreground">{children}</h1>,
              h2: ({ children }) => <h2 className="text-base font-bold mt-3 mb-1.5 text-foreground">{children}</h2>,
              h3: ({ children }) => <h3 className="text-sm font-bold mt-2 mb-1 text-foreground">{children}</h3>,
              // Inline
              p: ({ children }) => <p className="text-sm leading-relaxed my-1.5">{children}</p>,
              strong: ({ children }) => <strong className="font-semibold text-foreground">{children}</strong>,
              em: ({ children }) => <em className="italic text-muted-foreground">{children}</em>,
              // Lists
              ul: ({ children }) => <ul className="text-sm space-y-1 ml-4 list-disc">{children}</ul>,
              ol: ({ children }) => <ol className="text-sm space-y-1 ml-4 list-decimal">{children}</ol>,
              li: ({ children }) => <li className="text-sm leading-relaxed">{children}</li>,
              // Code
              code: ({ inline, children, ...props }: { inline?: boolean; children?: React.ReactNode; className?: string }) =>
                inline ? (
                  <code className="bg-zinc-800 text-violet-300 px-1.5 py-0.5 rounded text-xs font-mono" {...props}>{children}</code>
                ) : (
                  <code className="text-xs font-mono" {...props}>{children}</code>
                ),
              pre: ({ children }) => (
                <pre className="bg-zinc-900 border border-zinc-700/50 rounded-xl p-3 overflow-x-auto text-xs my-2">{children}</pre>
              ),
              // Tables (requires remark-gfm)
              table: ({ children }) => (
                <div className="overflow-x-auto my-2">
                  <table className="w-full text-xs border-collapse">{children}</table>
                </div>
              ),
              thead: ({ children }) => <thead className="border-b border-zinc-700">{children}</thead>,
              tbody: ({ children }) => <tbody className="divide-y divide-zinc-800">{children}</tbody>,
              tr: ({ children }) => <tr className="hover:bg-zinc-800/40 transition-colors">{children}</tr>,
              th: ({ children }) => <th className="text-left font-semibold text-muted-foreground px-3 py-2">{children}</th>,
              td: ({ children }) => <td className="px-3 py-2 text-foreground">{children}</td>,
              // Links
              a: ({ href, children }) => (
                <a href={href} className="text-primary hover:underline" target="_blank" rel="noopener noreferrer">{children}</a>
              ),
              // Horizontal rule
              hr: () => <hr className="border-zinc-700/50 my-3" />,
              // Blockquote
              blockquote: ({ children }) => (
                <blockquote className="border-l-2 border-violet-400/50 pl-3 my-2 text-muted-foreground italic">{children}</blockquote>
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
