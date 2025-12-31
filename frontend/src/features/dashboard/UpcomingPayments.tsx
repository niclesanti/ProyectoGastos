import { DataTable } from '@/components/ui/data-table'
import { ColumnDef } from '@tanstack/react-table'
import { formatCurrency } from '@/lib/utils'
import { useAppStore } from '@/store/app-store'
import { useEffect, useState } from 'react'
import type { CompraCreditoDTOResponse } from '@/types'
import { Skeleton } from '@/components/ui/skeleton'
import { Badge } from '@/components/ui/badge'

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
]

export function UpcomingPayments() {
  const currentWorkspace = useAppStore((state) => state.currentWorkspace)
  const loadComprasPendientes = useAppStore((state) => state.loadComprasPendientes)
  const comprasPendientesCache = useAppStore((state) => state.comprasPendientes)
  
  const [compras, setCompras] = useState<CompraCreditoDTOResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

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
    <DataTable 
      columns={columns} 
      data={compras}
      title="Compras con cuotas pendientes"
      pageSize={6}
    />
  )
}
