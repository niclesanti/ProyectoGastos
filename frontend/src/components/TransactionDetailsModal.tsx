import {
  ResponsiveModal,
  ResponsiveModalContent,
  ResponsiveModalDescription,
  ResponsiveModalHeader,
  ResponsiveModalTitle,
  ResponsiveModalScrollArea,
} from '@/components/ui/responsive-modal'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import {
  Tag,
  Calendar,
  User,
  Landmark,
} from 'lucide-react'
import { format, parseISO } from 'date-fns'
import { es } from 'date-fns/locale'
import { cn } from '@/lib/utils'
import { MoneyDecimal } from '@/lib/money'
import { MoneyDisplay } from '@/components/MoneyDisplay'

interface Transaction {
  id: string
  tipo: 'Ingreso' | 'Gasto'
  fecha: string
  motivo: string
  contacto: string
  cuenta: string
  monto: MoneyDecimal
  descripcion?: string
  nombreEspacioTrabajo: string
  nombreCompletoAuditoria: string
  fechaCreacion: string
}

interface TransactionDetailsModalProps {
  transaction: Transaction | null
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function TransactionDetailsModal({
  transaction,
  open,
  onOpenChange,
}: TransactionDetailsModalProps) {
  if (!transaction) return null

  return (
    <ResponsiveModal open={open} onOpenChange={onOpenChange}>
      <ResponsiveModalContent className="max-w-lg sm:max-w-xl lg:max-w-2xl">
        <ResponsiveModalHeader className="space-y-2">
          <ResponsiveModalTitle className="text-lg sm:text-xl">Detalles de transacción</ResponsiveModalTitle>
          <ResponsiveModalDescription className="text-xs sm:text-sm">
            Resumen completo del movimiento registrado
          </ResponsiveModalDescription>
        </ResponsiveModalHeader>

        <ResponsiveModalScrollArea>
          <div className="space-y-4 pb-4">
            {/* Hero Section - Monto y Tipo */}
            <div className="flex items-center justify-between p-4 rounded-lg bg-zinc-900/50 border border-zinc-800">
              <div>
                <p className="text-xs text-muted-foreground mb-1">Monto</p>
                <p className={cn(
                  'text-2xl font-bold font-mono tabular-nums',
                  transaction.tipo === 'Ingreso' ? 'text-emerald-400' : 'text-rose-400'
                )}>
                  {transaction.tipo === 'Ingreso' ? '+' : '-'}
                  <MoneyDisplay value={transaction.monto} />
                </p>
              </div>
              <Badge
                variant={transaction.tipo === 'Ingreso' ? 'default' : 'destructive'}
                className={cn(
                  'text-sm px-3 py-1 font-semibold',
                  transaction.tipo === 'Ingreso' && 'bg-green-600 hover:bg-green-700'
                )}
              >
                {transaction.tipo}
              </Badge>
            </div>

          {/* Details Grid - Información Principal */}
            <div className="grid grid-cols-2 gap-3">
              {/* Motivo */}
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-xs text-zinc-400">
                  <Tag className="h-3 w-3" />
                  <span>Motivo</span>
                </div>
                <p className="text-sm font-medium">{transaction.motivo}</p>
              </div>

              {/* Fecha */}
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-xs text-zinc-400">
                  <Calendar className="h-3 w-3" />
                  <span>Fecha</span>
                </div>
                <p className="text-sm font-medium">
                  {format(parseISO(transaction.fecha), "dd 'de' MMMM 'de' yyyy", { locale: es })}
                </p>
              </div>

              {/* Contacto */}
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-xs text-zinc-400">
                  <User className="h-3 w-3" />
                  <span>Contacto</span>
                </div>
                <p className="text-sm font-medium">{transaction.contacto}</p>
              </div>

              {/* Cuenta */}
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-xs text-zinc-400">
                  <Landmark className="h-3 w-3" />
                  <span>Cuenta bancaria</span>
                </div>
                <p className="text-sm font-medium">{transaction.cuenta}</p>
              </div>
            </div>

            {/* Description Section */}
            <Separator />
            <div className="space-y-1">
              <p className="text-xs text-zinc-400">Descripción</p>
              <div className="p-3 rounded-lg bg-zinc-900/50 border border-zinc-800 max-h-20 overflow-y-auto">
                <p className="text-xs leading-relaxed">
                  {transaction.descripcion || 'Ninguna'}
                </p>
              </div>
            </div>

            {/* Audit Footer */}
            <Separator />
            <div className="space-y-1 text-xs text-muted-foreground">
              <div className="space-y-0.5">
                <p>Espacio: <span className="font-medium text-foreground">{transaction.nombreEspacioTrabajo}</span></p>
                <p>Registrado por: <span className="font-medium text-foreground">{transaction.nombreCompletoAuditoria}</span></p>
                <p>Fecha creación: <span className="font-medium text-foreground">{format(parseISO(transaction.fechaCreacion), "dd/MM/yyyy HH:mm", { locale: es })}</span></p>
              </div>
            </div>
          </div>
        </ResponsiveModalScrollArea>
      </ResponsiveModalContent>
    </ResponsiveModal>
  )
}
