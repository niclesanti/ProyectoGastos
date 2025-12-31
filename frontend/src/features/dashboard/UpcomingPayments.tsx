import { DataTable } from '@/components/ui/data-table'
import { ColumnDef } from '@tanstack/react-table'
import { formatCurrency } from '@/lib/utils'
import { useAppStore } from '@/store/app-store'
import { useEffect, useState } from 'react'
import type { CompraCreditoDTOResponse } from '@/types'
import { Skeleton } from '@/components/ui/skeleton'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { MoreHorizontal, Eye, Trash2 } from 'lucide-react'
import { CreditPurchaseDetailsModal } from '@/components/CreditPurchaseDetailsModal'

export function UpcomingPayments() {
  const currentWorkspace = useAppStore((state) => state.currentWorkspace)
  const loadComprasPendientes = useAppStore((state) => state.loadComprasPendientes)
  const comprasPendientesCache = useAppStore((state) => state.comprasPendientes)
  
  const [compras, setCompras] = useState<CompraCreditoDTOResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [selectedPurchase, setSelectedPurchase] = useState<CompraCreditoDTOResponse | null>(null)
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false)

  const handleViewDetails = (purchase: CompraCreditoDTOResponse) => {
    setSelectedPurchase(purchase)
    setIsDetailsModalOpen(true)
  }

  const columns: ColumnDef<CompraCreditoDTOResponse>[] = [
    {
      accessorKey: "cantidadCuotas",
      header: "Cuotas/Pagadas",
      enableHiding: true,
      cell: ({ row }) => {
        const pagadas = row.original.cuotasPagadas
        const total = row.getValue("cantidadCuotas") as number
        const porcentaje = Math.round((pagadas / total) * 100)
        
        return (
          <div className="flex items-center gap-2">
            <span className="font-medium">{total}/{pagadas}</span>
            <Badge variant={pagadas === total ? "success" : "secondary"} className="text-xs">
              {porcentaje}%
            </Badge>
          </div>
        )
      },
    },
    {
      accessorKey: "nombreMotivo",
      header: "Motivo",
      enableHiding: true,
      cell: ({ row }) => {
        return <div>{row.getValue("nombreMotivo")}</div>
      },
    },
    {
      accessorKey: "nombreComercio",
      header: "Comercio",
      enableHiding: true,
      cell: ({ row }) => {
        const comercio = row.getValue("nombreComercio") as string | undefined
        return <div className="text-muted-foreground">{comercio || '-'}</div>
      },
    },
    {
      accessorKey: "montoTotal",
      header: () => <div className="text-right">Monto</div>,
      enableHiding: true,
      cell: ({ row }) => {
        const monto = parseFloat(row.getValue("montoTotal"))
        return (
          <div className="text-right font-semibold">
            {formatCurrency(monto)}
          </div>
        )
      },
    },
    {
      id: "actions",
      enableHiding: false,
      cell: ({ row }) => {
        return (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="h-8 w-8 p-0">
                <span className="sr-only">Abrir menú</span>
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => handleViewDetails(row.original)}>
                <Eye className="mr-2 h-4 w-4" />
                Ver detalles
              </DropdownMenuItem>
              <DropdownMenuItem className="text-destructive">
                <Trash2 className="mr-2 h-4 w-4" />
                Eliminar
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )
      },
    },
  ]

  useEffect(() => {
    const fetchCompras = async () => {
      if (!currentWorkspace?.id) {
        setCompras([])
        setLoading(false)
        setError(null)
        return
      }

      try {
        setLoading(true)
        setError(null)
        const data = await loadComprasPendientes(currentWorkspace.id)
        setCompras(data)
      } catch (err) {
        console.error('Error al cargar compras pendientes:', err)
        setError('Error al cargar las compras pendientes')
      } finally {
        setLoading(false)
      }
    }

    fetchCompras()
  }, [currentWorkspace?.id, loadComprasPendientes, comprasPendientesCache])

  if (loading) {
    return (
      <div className="space-y-3">
        <Skeleton className="h-10 w-full" />
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
        data={compras}
        title="Compras con cuotas pendientes"
        pageSize={6}
      />
      
      <CreditPurchaseDetailsModal
        purchase={selectedPurchase ? {
          id: selectedPurchase.id.toString(),
          fechaCompra: selectedPurchase.fechaCompra,
          montoTotal: selectedPurchase.montoTotal,
          cantidadCuotas: selectedPurchase.cantidadCuotas,
          cuotasPagadas: selectedPurchase.cuotasPagadas,
          descripcion: selectedPurchase.descripcion,
          nombreCompletoAuditoria: selectedPurchase.nombreCompletoAuditoria,
          fechaCreacion: selectedPurchase.fechaCreacion,
          nombreEspacioTrabajo: selectedPurchase.nombreEspacioTrabajo,
          nombreMotivo: selectedPurchase.nombreMotivo,
          nombreComercio: selectedPurchase.nombreComercio,
          numeroTarjeta: selectedPurchase.numeroTarjeta,
          entidadFinanciera: selectedPurchase.entidadFinanciera,
          redDePago: selectedPurchase.redDePago,
        } : null}
        open={isDetailsModalOpen}
        onOpenChange={setIsDetailsModalOpen}
      />
    </>
  )
}
