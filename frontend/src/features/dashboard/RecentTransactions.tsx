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
import type { Transaccion } from '@/types'
import { Skeleton } from '@/components/ui/skeleton'
import { format, isToday, isYesterday, parseISO } from 'date-fns'
import { es } from 'date-fns/locale'

const formatFecha = (fechaString: string): string => {
  try {
    const fecha = parseISO(fechaString)
    
    if (isToday(fecha)) {
      return `Hoy, ${format(fecha, 'HH:mm')}`
    }
    
    if (isYesterday(fecha)) {
      return `Ayer, ${format(fecha, 'HH:mm')}`
    }
    
    return format(fecha, 'd MMM', { locale: es })
  } catch {
    return fechaString
  }
}

export const columns: ColumnDef<Transaccion>[] = [
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
    accessorKey: "motivo",
    header: "Motivo",
    enableHiding: true,
    cell: ({ row }) => {
      const motivoNombre = row.original.motivo?.motivo || 'Sin motivo'
      return <div className="font-medium">{motivoNombre}</div>
    },
  },
  {
    accessorKey: "cuentaBancaria",
    header: "Cuenta",
    enableHiding: true,
    cell: ({ row }) => {
      const cuentaNombre = row.original.cuentaBancaria?.nombre || 'Sin cuenta'
      return <div className="text-muted-foreground text-sm">{cuentaNombre}</div>
    },
  },
  {
    accessorKey: "contacto",
    header: "Contacto",
    enableHiding: true,
    cell: ({ row }) => {
      const contactoNombre = row.original.contacto?.nombre || '-'
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
              onClick={() => {
                console.log('Ver detalles:', transaction.id)
              }}
            >
              <Eye className="mr-2 h-4 w-4" />
              Ver detalles
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onClick={() => {
                console.log('Eliminar transacción:', transaction.id)
              }}
              className="text-destructive focus:text-destructive"
            >
              <Trash2 className="mr-2 h-4 w-4" />
              Eliminar
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      )
    },
  },
]

export function RecentTransactions() {
  const { currentWorkspace, loadRecentTransactions } = useAppStore()
  const [transactions, setTransactions] = useState<Transaccion[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

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
  }, [currentWorkspace?.id, loadRecentTransactions])

  const handleReorder = (newData: Transaccion[]) => {
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
    <DataTable 
      columns={columns} 
      data={transactions}
      onReorder={handleReorder}
      title="Actividad reciente"
    />
  )
}
