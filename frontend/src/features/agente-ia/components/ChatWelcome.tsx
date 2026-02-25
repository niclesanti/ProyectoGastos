import { BrainCircuit, Wallet, Calendar, CreditCard, PieChart, TrendingUp, LucideIcon } from 'lucide-react'
import { motion } from 'framer-motion'
import { useAuth } from '@/contexts/AuthContext'
import { cn } from '@/lib/utils'

interface ChatWelcomeProps {
  onSuggestionClick: (mensaje: string) => void
}

interface Sugerencia {
  label: string
  fullText: string
  icon: LucideIcon
}

const sugerencias: Sugerencia[] = [
  { label: 'Balance',     fullText: '¿Cuál es mi balance actual?',          icon: Wallet     },
  { label: 'Este mes',    fullText: '¿Cuánto gasté este mes?',               icon: Calendar   },
  { label: 'Tarjetas',    fullText: '¿Cuánto debo pagar de tarjetas?',       icon: CreditCard },
  { label: 'Categorías',  fullText: 'Muéstrame mis gastos por categoría',    icon: PieChart   },
  { label: 'Tendencias',  fullText: 'Analizá mis tendencias de gasto',       icon: TrendingUp },
]

export function ChatWelcome({ onSuggestionClick }: ChatWelcomeProps) {
  const { user } = useAuth()
  const firstName = user?.nombre?.split(' ')[0] ?? 'Usuario'

  return (
    <div className="flex flex-col items-center justify-center h-full px-4 py-8">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, ease: 'easeOut' }}
        className="flex flex-col items-center gap-3 w-full max-w-2xl"
      >
        {/* Greeting line — small icon prefix, Gemini-style */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.1, duration: 0.35 }}
          className="flex items-center gap-2.5"
        >
          <BrainCircuit className="w-7 h-7 shrink-0 text-violet-500" />
          <span className="text-2xl font-semibold text-foreground">
            Hola, {firstName}
          </span>
        </motion.div>

        {/* Main title — large tracking-tight */}
        <motion.h1
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.15, duration: 0.4 }}
          className="text-4xl md:text-5xl font-bold tracking-tight text-center leading-tight"
        >
          ¿Por dónde empezamos?
        </motion.h1>

        {/* Quick-action chips */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.3, duration: 0.4 }}
          className="w-full mt-5"
        >
          {/* Mobile: horizontal scroll */}
          <div
            className={cn(
              'flex sm:hidden gap-2 overflow-x-auto pb-2 -mx-4 px-4',
              '[&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none]'
            )}
          >
            {sugerencias.map((s, i) => (
              <ChipButton key={i} sugerencia={s} delay={0.05 * i} onClick={onSuggestionClick} />
            ))}
          </div>

          {/* Desktop: wrap */}
          <div className="hidden sm:flex flex-wrap justify-center gap-2">
            {sugerencias.map((s, i) => (
              <ChipButton key={i} sugerencia={s} delay={0.05 * i} onClick={onSuggestionClick} />
            ))}
          </div>
        </motion.div>
      </motion.div>
    </div>
  )
}

function ChipButton({
  sugerencia,
  delay,
  onClick,
}: {
  sugerencia: Sugerencia
  delay: number
  onClick: (text: string) => void
}) {
  const Icon = sugerencia.icon
  return (
    <motion.button
      initial={{ opacity: 0, scale: 0.92 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ delay, duration: 0.25 }}
      whileHover={{ scale: 1.04 }}
      whileTap={{ scale: 0.97 }}
      onClick={() => onClick(sugerencia.fullText)}
      className={cn(
        'flex items-center gap-2 shrink-0',
        'px-4 py-2.5 rounded-full',
        'border border-border bg-zinc-900/60 hover:bg-zinc-800/80 hover:border-primary/40',
        'text-sm font-medium text-foreground',
        'transition-colors duration-150 cursor-pointer',
        'min-h-[44px]' // touch target
      )}
    >
      <Icon className="w-4 h-4 text-primary shrink-0" />
      <span>{sugerencia.label}</span>
    </motion.button>
  )
}
