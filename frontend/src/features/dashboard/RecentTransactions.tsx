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
import { useState } from 'react'

type Transaction = {
  id: number
  tipo: 'INGRESO' | 'GASTO'
  motivo: string
  cuenta: string
  contacto: string
  fecha: string
  monto: number
}

const initialTransactions: Transaction[] = [
  {
    id: 1,
    tipo: 'INGRESO',
    motivo: 'Salario Upwork',
    cuenta: 'Cuenta Principal',
    contacto: 'Upwork Inc.',
    fecha: 'Hoy, 10:23',
    monto: 4200.00,
  },
  {
    id: 2,
    tipo: 'GASTO',
    motivo: 'Viaje Uber',
    cuenta: 'Gastos',
    contacto: 'Uber',
    fecha: 'Ayer, 20:45',
    monto: -24.50,
  },
  {
    id: 3,
    tipo: 'GASTO',
    motivo: 'Spotify Premium',
    cuenta: 'Gastos',
    contacto: 'Spotify',
    fecha: '12 Oct',
    monto: -12.00,
  },
  {
    id: 4,
    tipo: 'GASTO',
    motivo: 'Apple Store',
    cuenta: 'Cuenta Principal',
    contacto: 'Apple',
    fecha: '10 Oct',
    monto: -124.99,
  },
  {
    id: 5,
    tipo: 'INGRESO',
    motivo: 'Freelance',
    cuenta: 'Ahorros',
    contacto: 'Cliente X',
    fecha: '08 Oct',
    monto: 850.00,
  },
  {
    id: 6,
    tipo: 'GASTO',
    motivo: 'Supermercado',
    cuenta: 'Gastos',
    contacto: 'Mercadona',
    fecha: '07 Oct',
    monto: -86.30,
  },
]

export const columns: ColumnDef<Transaction>[] = [
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
      return <div className="font-medium">{row.getValue("motivo")}</div>
    },
  },
  {
    accessorKey: "cuenta",
    header: "Cuenta",
    enableHiding: true,
    cell: ({ row }) => {
      return <div className="text-muted-foreground text-sm">{row.getValue("cuenta")}</div>
    },
  },
  {
    accessorKey: "contacto",
    header: "Contacto",
    enableHiding: true,
    cell: ({ row }) => {
      return <div className="text-muted-foreground text-sm">{row.getValue("contacto")}</div>
    },
  },
  {
    accessorKey: "fecha",
    header: "Fecha",
    enableHiding: true,
    cell: ({ row }) => {
      return <div className="text-muted-foreground text-sm">{row.getValue("fecha")}</div>
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
          className={`text-right font-semibold ${
            tipo === 'INGRESO' ? 'text-green-500' : 'text-red-500'
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
  const [transactions, setTransactions] = useState(initialTransactions)

  const handleReorder = (newData: Transaction[]) => {
    setTransactions(newData)
  }

  return (
    <DataTable 
      columns={columns} 
      data={transactions}
      onReorder={handleReorder}
      title="Actividad Reciente"
    />
  )
}
