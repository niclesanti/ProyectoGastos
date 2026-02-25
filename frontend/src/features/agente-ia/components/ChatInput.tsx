import { useState, useRef, KeyboardEvent } from 'react'
import { Send } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

interface ChatInputProps {
  onSend: (mensaje: string) => void
  disabled?: boolean
  className?: string
}

export function ChatInput({ onSend, disabled = false, className }: ChatInputProps) {
  const [mensaje, setMensaje] = useState('')
  const textareaRef = useRef<HTMLTextAreaElement>(null)

  const handleSend = () => {
    const mensajeTrimmed = mensaje.trim()
    if (!mensajeTrimmed || disabled) return
    onSend(mensajeTrimmed)
    setMensaje('')
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto'
    }
  }

  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  const handleInput = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const target = e.target
    target.style.height = 'auto'
    target.style.height = `${Math.min(target.scrollHeight, 160)}px`
    setMensaje(target.value)
  }

  return (
    <div
      className={cn(
        'sticky bottom-0 left-0 right-0 z-10',
        'pb-[env(safe-area-inset-bottom)]',
        // Fade gradient above the input area so content doesn't hard-cut
        'bg-gradient-to-t from-background via-background/95 to-transparent',
        'pt-6 px-4 pb-4',
        className
      )}
    >
      {/* Floating pill — max-w-3xl, doesn't stretch edge-to-edge */}
      <div className="max-w-3xl mx-auto">
        <div
          className={cn(
            'flex items-end gap-0',
            'rounded-2xl border border-zinc-700/60',
            'bg-zinc-900/80 backdrop-blur-md',
            'shadow-lg shadow-black/20',
            'px-4 py-3',
            'transition-[border-color] duration-200',
            !disabled && 'focus-within:border-primary/50'
          )}
        >
          {/* ">" prefix — fixed decoration inside the pill */}
          <span
            className="shrink-0 mr-3 mb-0.5 self-end text-muted-foreground/60 font-mono text-base select-none leading-[1.75rem]"
            aria-hidden
          >
            &gt;
          </span>

          {/* Textarea — borderless, fills the pill */}
          <textarea
            ref={textareaRef}
            value={mensaje}
            onChange={handleInput}
            onKeyDown={handleKeyDown}
            placeholder="Pregunta sobre tus finanzas..."
            disabled={disabled}
            rows={1}
            className={cn(
              'flex-1 bg-transparent border-none outline-none resize-none',
              'min-h-[28px] max-h-[160px] overflow-y-auto',
              'text-base leading-7 text-foreground placeholder:text-muted-foreground/50',
              'scrollbar-thin scrollbar-thumb-zinc-700',
              disabled && 'opacity-50 cursor-not-allowed'
            )}
          />

          {/* Send button — inside the pill, right side */}
          <Button
            size="icon"
            onClick={handleSend}
            disabled={!mensaje.trim() || disabled}
            className={cn(
              'shrink-0 ml-2 self-end',
              'h-8 w-8 rounded-xl',
              'transition-all duration-200',
              !mensaje.trim() || disabled
                ? 'opacity-30'
                : 'opacity-100 hover:scale-105'
            )}
          >
            <Send className="h-4 w-4" />
          </Button>
        </div>

        {/* Hint sutil */}
        <p className="text-xs text-muted-foreground/50 mt-2 text-center">
          {disabled
            ? 'Esperando respuesta del agente...'
            : 'Enter para enviar, Shift + Enter para nueva línea'}
        </p>
      </div>
    </div>
  )
}
