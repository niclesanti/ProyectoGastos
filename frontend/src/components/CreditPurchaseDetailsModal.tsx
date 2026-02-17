import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { ScrollArea } from '@/components/ui/scroll-area'
import {
  Tag,
  Calendar,
  Store,
  CreditCard,
  ListChecks,
  Building2,
  Wallet,
} from 'lucide-react'
import { format, parseISO } from 'date-fns'
import { es } from 'date-fns/locale'
import { cn } from '@/lib/utils'
import { MoneyDecimal } from '@/lib/money'
import { MoneyDisplay } from '@/components/MoneyDisplay'
import { useMoney } from '@/hooks/useMoney'

interface CreditPurchase {
  id: string
  fechaCompra: string
  montoTotal: MoneyDecimal
  cantidadCuotas: number
  cuotasPagadas: number
  descripcion?: string
  nombreCompletoAuditoria: string
  fechaCreacion: string
  nombreEspacioTrabajo: string
  nombreMotivo: string
  nombreComercio?: string
  numeroTarjeta?: string
  entidadFinanciera?: string
  redDePago?: string
}

interface CreditPurchaseDetailsModalProps {
  purchase: CreditPurchase | null
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function CreditPurchaseDetailsModal({
  purchase,
  open,
  onOpenChange,
}: CreditPurchaseDetailsModalProps) {
  const { divide } = useMoney()
  
  if (!purchase) return null

  const porcentajePagado = Math.round((purchase.cuotasPagadas / purchase.cantidadCuotas) * 100)
  const isPaid = purchase.cuotasPagadas === purchase.cantidadCuotas
  const montoPorCuota = divide(purchase.montoTotal, purchase.cantidadCuotas)

  const formatCardNumber = (numero: string | undefined) => {
    if (!numero) return '-'
    // Extraer los últimos 4 dígitos
    const lastFour = numero.slice(-4)
    // Mostrar asteriscos simulando una tarjeta completa
    return `**** **** **** ${lastFour}`
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg sm:max-w-xl lg:max-w-2xl max-h-[90vh] flex flex-col p-4 sm:p-6">
        <DialogHeader className="space-y-2">
          <DialogTitle className="text-lg sm:text-xl">Detalles compra con crédito</DialogTitle>
          <DialogDescription className="text-xs sm:text-sm">
            Resumen completo de la compra en cuotas
          </DialogDescription>
        </DialogHeader>

        <ScrollArea className="flex-1 pr-4 overflow-y-auto">
          <div className="space-y-4 pb-4">
            {/* Hero Section - Monto y Estado */}
            <div className="flex items-center justify-between p-4 rounded-lg bg-zinc-900/50 border border-zinc-800">
              <div>
                <p className="text-xs text-muted-foreground mb-1">Monto total</p>
                <p className="text-2xl font-bold font-mono tabular-nums text-amber-400">
                  <MoneyDisplay value={purchase.montoTotal} />
                </p>
                <p className="text-xs text-muted-foreground mt-1">
                  {purchase.cantidadCuotas} cuotas de <MoneyDisplay value={montoPorCuota} />
                </p>
              </div>
              <div className="flex flex-col items-end gap-2">
                <Badge
                  variant={isPaid ? 'default' : 'secondary'}
                  className={cn(
                    'text-sm px-3 py-1 font-semibold',
                    isPaid && 'bg-green-600 hover:bg-green-700'
                  )}
                >
                  {isPaid ? 'Pagado' : 'Pendiente'}
                </Badge>
                <div className="flex items-center gap-2 text-xs">
                  <span className="font-medium">{purchase.cantidadCuotas}/{purchase.cuotasPagadas}</span>
                  <Badge variant="outline" className="text-xs">
                    {porcentajePagado}%
                  </Badge>
                </div>
              </div>
            </div>

            {/* Details Grid - Información Principal */}
            <div className="grid grid-cols-2 gap-3">
              {/* Motivo */}
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-xs text-zinc-400">
                  <Tag className="h-3 w-3" />
                  <span>Motivo</span>
                </div>
                <p className="text-sm font-medium">{purchase.nombreMotivo}</p>
              </div>

              {/* Comercio */}
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-xs text-zinc-400">
                  <Store className="h-3 w-3" />
                  <span>Comercio</span>
                </div>
                <p className="text-sm font-medium">{purchase.nombreComercio || '-'}</p>
              </div>

              {/* Fecha */}
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-xs text-zinc-400">
                  <Calendar className="h-3 w-3" />
                  <span>Fecha de compra</span>
                </div>
                <p className="text-sm font-medium">
                  {format(parseISO(purchase.fechaCompra), "dd 'de' MMMM 'de' yyyy", { locale: es })}
                </p>
              </div>

              {/* Cuotas */}
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-xs text-zinc-400">
                  <ListChecks className="h-3 w-3" />
                  <span>Cuotas pagadas</span>
                </div>
                <p className="text-sm font-medium">
                  {purchase.cuotasPagadas} de {purchase.cantidadCuotas}
                </p>
              </div>

              {/* Tarjeta */}
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-xs text-zinc-400">
                  <CreditCard className="h-3 w-3" />
                  <span>Tarjeta</span>
                </div>
                <p className="text-sm font-medium font-mono">{formatCardNumber(purchase.numeroTarjeta)}</p>
              </div>

              {/* Entidad Financiera */}
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-xs text-zinc-400">
                  <Building2 className="h-3 w-3" />
                  <span>Entidad financiera</span>
                </div>
                <p className="text-sm font-medium">{purchase.entidadFinanciera || '-'}</p>
              </div>

              {/* Red de Pago */}
              <div className="space-y-1 col-span-2">
                <div className="flex items-center gap-2 text-xs text-zinc-400">
                  <Wallet className="h-3 w-3" />
                  <span>Red de pago</span>
                </div>
                <p className="text-sm font-medium">{purchase.redDePago || '-'}</p>
              </div>
            </div>

            {/* Description Section */}
            <Separator />
            <div className="space-y-1">
              <p className="text-xs text-zinc-400">Descripción</p>
              <div className="p-3 rounded-lg bg-zinc-900/50 border border-zinc-800 max-h-20 overflow-y-auto">
                <p className="text-xs leading-relaxed">
                  {purchase.descripcion || 'Ninguna'}
                </p>
              </div>
            </div>

            {/* Audit Footer */}
            <Separator />
            <div className="space-y-1 text-xs text-muted-foreground">
              <div className="space-y-0.5">
                <p>Espacio: <span className="font-medium text-foreground">{purchase.nombreEspacioTrabajo}</span></p>
                <p>Registrado por: <span className="font-medium text-foreground">{purchase.nombreCompletoAuditoria}</span></p>
                <p>Fecha creación: <span className="font-medium text-foreground">{format(parseISO(purchase.fechaCreacion), "dd/MM/yyyy HH:mm", { locale: es })}</span></p>
              </div>
            </div>
          </div>
        </ScrollArea>
      </DialogContent>
    </Dialog>
  )
}
