import { useState } from 'react'
import { motion } from 'framer-motion'
import { BadgeDollarSign, ArrowRightLeft, CreditCard, Receipt } from 'lucide-react'
import { TransactionModal } from '@/components/TransactionModal'
import { AccountTransferModal } from '@/components/AccountTransferModal'
import { CreditPurchaseModal } from '@/components/CreditPurchaseModal'
import { CardPaymentModal } from '@/components/CardPaymentModal'
import { useAppStore } from '@/store/app-store'

type ActionId = 'transaction' | 'transfer' | 'credit' | 'payment'

const actions = [
  {
    id: 'transaction' as ActionId,
    icon: BadgeDollarSign,
    ariaLabel: 'Registrar gastos e ingresos',
  },
  {
    id: 'transfer' as ActionId,
    icon: ArrowRightLeft,
    ariaLabel: 'Movimiento entre cuentas',
  },
  {
    id: 'credit' as ActionId,
    icon: CreditCard,
    ariaLabel: 'Compras con crédito',
  },
  {
    id: 'payment' as ActionId,
    icon: Receipt,
    ariaLabel: 'Resumen mensual de tarjetas',
  },
]

export function LiquidGlassNavigationBar() {
  const currentWorkspace = useAppStore((state) => state.currentWorkspace)
  const [activeModal, setActiveModal] = useState<ActionId | null>(null)

  const handleActionClick = (actionId: ActionId) => {
    // Remover el foco del elemento activo para evitar warnings de aria-hidden de Radix
    if (document.activeElement instanceof HTMLElement) {
      document.activeElement.blur()
    }
    setActiveModal(actionId)
  }

  if (!currentWorkspace) {
    return null
  }

  return (
    <>
      <motion.nav
        aria-label="Acciones rápidas"
        initial={{ opacity: 0, y: 16, scale: 0.92, x: '-50%' }}
        animate={{ opacity: 1, y: 0, scale: 1, x: '-50%' }}
        transition={{ type: 'spring', stiffness: 380, damping: 30, mass: 0.8 }}
        className="fixed bottom-4 left-1/2 z-50 md:hidden flex items-center gap-1 rounded-full p-1.5 border border-white/40 dark:border-white/15 bg-[linear-gradient(180deg,rgba(255,255,255,0.16),rgba(255,255,255,0.02))] dark:bg-[linear-gradient(180deg,rgba(255,255,255,0.05),rgba(255,255,255,0.01))] backdrop-blur-xl backdrop-saturate-[1.8] backdrop-brightness-[1.12] shadow-[0_8px_32px_rgba(0,0,0,0.16),inset_0_1px_0_rgba(255,255,255,0.5)]"
        style={{ paddingBottom: 'max(0.375rem, env(safe-area-inset-bottom))' }}
      >
        <span
          aria-hidden="true"
          className="pointer-events-none absolute inset-0 rounded-full bg-gradient-to-b from-white/50 via-white/10 to-transparent dark:from-white/10 dark:via-white/5 dark:to-transparent"
        />
        {actions.map((action) => {
          const Icon = action.icon
          return (
            <motion.button
              key={action.id}
              type="button"
              onClick={() => handleActionClick(action.id)}
              aria-label={action.ariaLabel}
              title={action.ariaLabel}
              whileTap={{ scale: 0.85 }}
              whileHover={{ scale: 1.06 }}
              className="flex items-center justify-center h-12 w-12 rounded-full text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <Icon className="h-6 w-6" aria-hidden="true" />
            </motion.button>
          )
        })}
      </motion.nav>

      <TransactionModal
        open={activeModal === 'transaction'}
        onOpenChange={(open) => !open && setActiveModal(null)}
      />
      <AccountTransferModal
        open={activeModal === 'transfer'}
        onOpenChange={(open) => !open && setActiveModal(null)}
      />
      <CreditPurchaseModal
        open={activeModal === 'credit'}
        onOpenChange={(open) => !open && setActiveModal(null)}
      />
      <CardPaymentModal
        open={activeModal === 'payment'}
        onOpenChange={(open) => !open && setActiveModal(null)}
      />
    </>
  )
}