"use client"

import { useState, useRef, useEffect } from 'react'
import { Plus, TrendingDown, ArrowRightLeft, CreditCard, Receipt } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  Drawer,
  DrawerClose,
  DrawerContent,
  DrawerDescription,
  DrawerFooter,
  DrawerHeader,
  DrawerTitle,
  DrawerTrigger,
} from '@/components/ui/drawer'
import { TransactionModal } from '@/components/TransactionModal'
import { AccountTransferModal } from '@/components/AccountTransferModal'
import { CreditPurchaseModal } from '@/components/CreditPurchaseModal'
import { CardPaymentModal } from '@/components/CardPaymentModal'
import { useAppStore } from '@/store/app-store'

const actions = [
  {
    id: 'transaction',
    icon: TrendingDown,
    label: 'Registrar transacción',
    description: 'Gastos e ingresos',
    color: 'text-rose-500',
  },
  {
    id: 'transfer',
    icon: ArrowRightLeft,
    label: 'Movimiento entre cuentas',
    description: 'Transferir dinero',
    color: 'text-blue-500',
  },
  {
    id: 'credit',
    icon: CreditCard,
    label: 'Compra con crédito',
    description: 'Nueva compra en cuotas',
    color: 'text-purple-500',
  },
  {
    id: 'payment',
    icon: Receipt,
    label: 'Pagar resumen tarjeta',
    description: 'Liquidar resumen mensual',
    color: 'text-emerald-500',
  },
]

export function MobileActionsFAB() {
  const currentWorkspace = useAppStore((state) => state.currentWorkspace)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [transactionModalOpen, setTransactionModalOpen] = useState(false)
  const [accountTransferModalOpen, setAccountTransferModalOpen] = useState(false)
  const [creditPurchaseModalOpen, setCreditPurchaseModalOpen] = useState(false)
  const [cardPaymentModalOpen, setCardPaymentModalOpen] = useState(false)
  const triggerButtonRef = useRef<HTMLButtonElement>(null)

  // Remover el foco del botón trigger cuando se abre el drawer para evitar conflictos con aria-hidden
  useEffect(() => {
    if (drawerOpen && triggerButtonRef.current) {
      triggerButtonRef.current.blur()
    }
  }, [drawerOpen])

  const handleActionClick = (actionId: string) => {
    setDrawerOpen(false)
    
    // Pequeño delay para que el drawer se cierre suavemente antes de abrir el modal
    setTimeout(() => {
      switch (actionId) {
        case 'transaction':
          setTransactionModalOpen(true)
          break
        case 'transfer':
          setAccountTransferModalOpen(true)
          break
        case 'credit':
          setCreditPurchaseModalOpen(true)
          break
        case 'payment':
          setCardPaymentModalOpen(true)
          break
      }
    }, 150)
  }

  if (!currentWorkspace) {
    return null
  }

  return (
    <>
      {/* Floating Action Button - Solo visible en móviles y tablets */}
      <Drawer open={drawerOpen} onOpenChange={setDrawerOpen}>
        <DrawerTrigger asChild>
          <Button
            ref={triggerButtonRef}
            size="icon"
            className="fixed bottom-6 right-6 h-14 w-14 rounded-full shadow-lg md:hidden z-50 bg-white text-black hover:bg-gray-100 hover:text-black"
            aria-label="Nuevo registro"
          >
            <Plus className="h-6 w-6" />
          </Button>
        </DrawerTrigger>

        <DrawerContent className="md:hidden">
          <DrawerHeader className="text-left">
            <DrawerTitle>Nuevo registro</DrawerTitle>
            <DrawerDescription>
              Selecciona el tipo de operación que deseas realizar
            </DrawerDescription>
          </DrawerHeader>

          <div className="px-4 pb-4">
            <div className="space-y-2">
              {actions.map((action) => {
                const Icon = action.icon
                return (
                  <button
                    key={action.id}
                    onClick={() => handleActionClick(action.id)}
                    className="w-full flex items-center gap-4 p-4 rounded-lg bg-muted/50 hover:bg-muted transition-colors text-left min-h-[60px]"
                  >
                    <div className={`flex-shrink-0 ${action.color}`}>
                      <Icon className="h-5 w-5" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-foreground">{action.label}</p>
                      <p className="text-sm text-muted-foreground">{action.description}</p>
                    </div>
                  </button>
                )
              })}
            </div>
          </div>

          <DrawerFooter>
            <DrawerClose asChild>
              <Button variant="outline">Cancelar</Button>
            </DrawerClose>
          </DrawerFooter>
        </DrawerContent>
      </Drawer>

      {/* Modales existentes */}
      <TransactionModal
        open={transactionModalOpen}
        onOpenChange={setTransactionModalOpen}
      />
      
      <AccountTransferModal
        open={accountTransferModalOpen}
        onOpenChange={setAccountTransferModalOpen}
      />
      
      <CreditPurchaseModal
        open={creditPurchaseModalOpen}
        onOpenChange={setCreditPurchaseModalOpen}
      />
      
      <CardPaymentModal
        open={cardPaymentModalOpen}
        onOpenChange={setCardPaymentModalOpen}
      />
    </>
  )
}
