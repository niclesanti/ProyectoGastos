import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { DataTable } from '@/components/ui/data-table'
import { ColumnDef } from '@tanstack/react-table'
import { formatCurrency } from '@/lib/utils'
import { ArrowUpRight, ArrowDownRight, MoreHorizontal, Trash2, Eye, GripVertical } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useAppStore } from '@/store/app-store'
import type { TransaccionDTOResponse } from '@/types'
import { Skeleton } from '@/components/ui/skeleton'
import { format, parseISO } from 'date-fns'
import { TransactionDetailsModal } from '@/components/TransactionDetailsModal'
import { DeleteConfirmDialog } from '@/components/DeleteConfirmDialog'
import { useRemoverTransaccion } from '@/features/selectors/api/selector-queries'
import { toast } from 'sonner'

const formatFecha = (fechaString: string): string => {
  try {
    const fecha = parseISO(fechaString)
    return format(fecha, 'dd/MM/yyyy')
  } catch {
    return fechaString
  }
}

export function RecentTransactions() {
  const currentWorkspace = useAppStore((state) => state.currentWorkspace)
  const loadRecentTransactions = useAppStore((state) => state.loadRecentTransactions)
  const recentTransactionsCache = useAppStore((state) => state.recentTransactions)
  
  const [transactions, setTransactions] = useState<TransaccionDTOResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [selectedTransaction, setSelectedTransaction] = useState<TransaccionDTOResponse | null>(null)
  const [detailsModalOpen, setDetailsModalOpen] = useState(false)
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false)
  const [transactionToDelete, setTransactionToDelete] = useState<TransaccionDTOResponse | null>(null)

  const removerTransaccionMutation = useRemoverTransaccion()

  const handleViewDetails = (transaction: TransaccionDTOResponse) => {
    setSelectedTransaction(transaction)
    setDetailsModalOpen(true)
  }

  const handleDeleteClick = (transaction: TransaccionDTOResponse) => {
    setTransactionToDelete(transaction)
    setDeleteConfirmOpen(true)
  }

  const handleDeleteConfirm = () => {
    if (!transactionToDelete) return

    removerTransaccionMutation.mutate(transactionToDelete.id, {
      onSuccess: () => {
        toast.success('Transacción eliminada', {
          description: 'La transacción ha sido eliminada correctamente.',
        })
      },
      onError: (error: any) => {
        console.error('Error al eliminar transacción:', error)
        toast.error('Error al eliminar', {
          description: error?.response?.data?.message || 'No se pudo eliminar la transacción. Intenta nuevamente.',
        })
      },
    })
  }

  const columns: ColumnDef<TransaccionDTOResponse>[] = [
    {
      id: "drag",
      header: "",
      cell: () => (
        <div className="cursor-grab active:cursor-grabbing">
          <GripVertical className="h-4 w-4 text-muted-foreground" />
        </div>
      ),
      enableSorting: false,
      enableHiding: false,
    },
    {
      accessorKey: "tipo",
      header: "Tipo",
      enableHiding: true,
      cell: ({ row }) => {
        const tipo = row.getValue("tipo") as string
        return (
          <Badge
            variant={tipo === 'INGRESO' ? 'success' : 'destructive'}
            className="flex w-fit items-center gap-1"
          >
            {tipo === 'INGRESO' ? (
              <ArrowUpRight className="h-3 w-3" />
            ) : (
              <ArrowDownRight className="h-3 w-3" />
            )}
            {tipo === 'INGRESO' ? 'Ingreso' : 'Gasto'}
          </Badge>
        )
      },
    },
    {
      accessorKey: "nombreMotivo",
      header: "Motivo",
      enableHiding: true,
      cell: ({ row }) => {
        const motivoNombre = row.getValue("nombreMotivo") as string || 'Sin motivo'
        return <div className="font-medium">{motivoNombre}</div>
      },
    },
    {
      accessorKey: "nombreCuentaBancaria",
      header: "Cuenta",
      enableHiding: true,
      cell: ({ row }) => {
        const cuentaNombre = row.getValue("nombreCuentaBancaria") as string || 'Sin cuenta'
        return <div className="text-muted-foreground text-sm">{cuentaNombre}</div>
      },
    },
    {
      accessorKey: "nombreContacto",
      header: "Contacto",
      enableHiding: true,
      cell: ({ row }) => {
        const contactoNombre = row.getValue("nombreContacto") as string || '-'
        return <div className="text-muted-foreground text-sm">{contactoNombre}</div>
      },
    },
    {
      accessorKey: "fecha",
      header: "Fecha",
      enableHiding: true,
      cell: ({ row }) => {
        const fechaFormateada = formatFecha(row.getValue("fecha"))
        return <div className="text-muted-foreground text-sm">{fechaFormateada}</div>
      },
    },
    {
      accessorKey: "monto",
      header: () => <div className="text-right">Monto</div>,
      enableHiding: true,
      cell: ({ row }) => {
        const monto = parseFloat(row.getValue("monto"))
        const tipo = row.original.tipo
        
        return (
          <div
            className={`text-right font-mono font-semibold tabular-nums ${
              tipo === 'INGRESO' ? 'text-emerald-400' : 'text-rose-400'
            }`}
          >
            {tipo === 'INGRESO' ? '+' : ''}
            {formatCurrency(monto)}
          </div>
        )
      },
    },
    {
      id: "actions",
      enableHiding: false,
      cell: ({ row }) => {
        const transaction = row.original
   
        return (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="h-8 w-8 p-0">
                <span className="sr-only">Abrir menú</span>
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem
                onClick={() => handleViewDetails(transaction)}
              >
                <Eye className="mr-2 h-4 w-4" />
                Ver detalles
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                onClick={() => handleDeleteClick(transaction)}
                className="text-destructive focus:text-destructive"
                disabled={removerTransaccionMutation.isPending}
              >
                <Trash2 className="mr-2 h-4 w-4" />
                {removerTransaccionMutation.isPending ? 'Eliminando...' : 'Eliminar'}
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )
      },
    },
  ]

  useEffect(() => {
    const fetchTransactions = async () => {
      if (!currentWorkspace?.id) {
        setTransactions([])
        setLoading(false)
        setError(null)
        return
      }

      try {
        setLoading(true)
        setError(null)
        const data = await loadRecentTransactions(currentWorkspace.id)
        setTransactions(data)
      } catch (err) {
        console.error('Error al cargar transacciones recientes:', err)
        setError('Error al cargar las transacciones recientes')
      } finally {
        setLoading(false)
      }
    }

    fetchTransactions()
  }, [currentWorkspace?.id, loadRecentTransactions, recentTransactionsCache])

  const handleReorder = (newData: TransaccionDTOResponse[]) => {
    setTransactions(newData)
  }

  if (loading) {
    return (
      <div className="space-y-3">
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-16 w-full" />
        <Skeleton className="h-16 w-full" />
        <Skeleton className="h-16 w-full" />
        <Skeleton className="h-16 w-full" />
        <Skeleton className="h-16 w-full" />
        <Skeleton className="h-16 w-full" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-4">
        <p className="text-sm text-destructive">{error}</p>
      </div>
    )
  }

  return (
    <>
      <DataTable 
        columns={columns} 
        data={transactions}
        onReorder={handleReorder}
        title="Actividad reciente"
      />
      
      <TransactionDetailsModal
        transaction={selectedTransaction ? {
          id: selectedTransaction.id.toString(),
          tipo: selectedTransaction.tipo === 'INGRESO' ? 'Ingreso' : 'Gasto',
          fecha: selectedTransaction.fecha,
          motivo: selectedTransaction.nombreMotivo || 'Sin motivo',
          contacto: selectedTransaction.nombreContacto || '-',
          cuenta: selectedTransaction.nombreCuentaBancaria || 'Sin cuenta',
          monto: selectedTransaction.monto,
          descripcion: selectedTransaction.descripcion,
          nombreEspacioTrabajo: selectedTransaction.nombreEspacioTrabajo,
          nombreCompletoAuditoria: selectedTransaction.nombreCompletoAuditoria,
          fechaCreacion: selectedTransaction.fechaCreacion
        } : null}
        open={detailsModalOpen}
        onOpenChange={setDetailsModalOpen}
      />

      <DeleteConfirmDialog
        open={deleteConfirmOpen}
        onOpenChange={setDeleteConfirmOpen}
        onConfirm={handleDeleteConfirm}
        title="¿Eliminar transacción?"
        description="Esta acción no se puede deshacer. La transacción será eliminada permanentemente del sistema."
        isLoading={removerTransaccionMutation.isPending}
      />
    </>
  )
}
