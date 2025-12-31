import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { Button } from '@/components/ui/button'
import { ScrollArea } from '@/components/ui/scroll-area'
import {
  Tag,
  Calendar,
  User,
  Landmark,
  Copy,
  CheckCircle2,
} from 'lucide-react'
import { format, parseISO } from 'date-fns'
import { es } from 'date-fns/locale'
import { cn } from '@/lib/utils'
import { useState } from 'react'
import { toast } from 'sonner'

interface Transaction {
  id: string
  tipo: 'Ingreso' | 'Gasto'
  fecha: string
  motivo: string
  contacto: string
  cuenta: string
  monto: number
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
  const [copied, setCopied] = useState(false)

  if (!transaction) return null

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
      minimumFractionDigits: 2,
    }).format(amount)
  }

  const handleCopyId = async () => {
    try {
      await navigator.clipboard.writeText(transaction.id)
      setCopied(true)
      toast.success('ID copiado', {
        description: 'El ID de la transacción ha sido copiado al portapapeles.',
      })
      setTimeout(() => setCopied(false), 2000)
    } catch (error) {
      toast.error('Error al copiar', {
        description: 'No se pudo copiar el ID al portapapeles.',
      })
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] flex flex-col">
        <DialogHeader>
          <DialogTitle className="text-xl">Detalles de transacción</DialogTitle>
          <DialogDescription className="text-xs">
            Resumen completo del movimiento registrado
          </DialogDescription>
        </DialogHeader>

        <ScrollArea className="flex-1 pr-4">
          <div className="space-y-4">
            {/* Hero Section - Monto y Tipo */}
            <div className="flex items-center justify-between p-4 rounded-lg bg-zinc-900/50 border border-zinc-800">
              <div>
                <p className="text-xs text-muted-foreground mb-1">Monto</p>
                <p className={cn(
                  'text-2xl font-bold font-mono tabular-nums',
                  transaction.tipo === 'Ingreso' ? 'text-emerald-400' : 'text-rose-400'
                )}>
                  {transaction.tipo === 'Ingreso' ? '+' : '-'}{formatCurrency(transaction.monto)}
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
              <div className="flex items-start justify-between gap-4">
                <div className="space-y-0.5 flex-1">
                  <p>Espacio: <span className="font-medium text-foreground">{transaction.nombreEspacioTrabajo}</span></p>
                  <p>Registrado por: <span className="font-medium text-foreground">{transaction.nombreCompletoAuditoria}</span></p>
                  <p>Fecha creación: <span className="font-medium text-foreground">{format(parseISO(transaction.fechaCreacion), "dd/MM/yyyy HH:mm", { locale: es })}</span></p>
                  <p className="pt-1">ID: <span className="font-mono">{transaction.id}</span></p>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleCopyId}
                  className="h-7 text-xs shrink-0"
                >
                  {copied ? (
                    <>
                      <CheckCircle2 className="mr-1 h-3 w-3" />
                      Copiado
                    </>
                  ) : (
                    <>
                      <Copy className="mr-1 h-3 w-3" />
                      Copiar ID
                    </>
                  )}
                </Button>
              </div>
            </div>
          </div>
        </ScrollArea>
      </DialogContent>
    </Dialog>
  )
}
