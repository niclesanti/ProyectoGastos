import { DataTable } from '@/components/ui/data-table'
import { ColumnDef } from '@tanstack/react-table'
import { formatCurrency } from '@/lib/utils'

type Payment = {
  id: number
  numero: number
  motivo: string
  vencimiento: string
  monto: number
}

const payments: Payment[] = [
  { id: 1, numero: 1, motivo: 'Suscripción Netflix', vencimiento: '15 Oct', monto: 15.99 },
  { id: 2, numero: 2, motivo: 'Servicios AWS', vencimiento: '18 Oct', monto: 64.20 },
  { id: 3, numero: 3, motivo: 'Alquiler Mensual', vencimiento: '01 Nov', monto: 1800.00 },
  { id: 4, numero: 4, motivo: 'Spotify Premium', vencimiento: '05 Nov', monto: 12.00 },
  { id: 5, numero: 5, motivo: 'Seguro Coche', vencimiento: '10 Nov', monto: 85.50 },
  { id: 6, numero: 6, motivo: 'Gym Membership', vencimiento: '15 Nov', monto: 45.00 },
]

export const columns: ColumnDef<Payment>[] = [
  {
    accessorKey: "numero",
    header: "Número",
    enableHiding: true,
    cell: ({ row }) => {
      return <div className="font-medium">{row.getValue("numero")}</div>
    },
  },
  {
    accessorKey: "motivo",
    header: "Motivo",
    enableHiding: true,
    cell: ({ row }) => {
      return <div>{row.getValue("motivo")}</div>
    },
  },
  {
    accessorKey: "vencimiento",
    header: "Vencimiento",
    enableHiding: true,
    cell: ({ row }) => {
      return <div className="text-muted-foreground">{row.getValue("vencimiento")}</div>
    },
  },
  {
    accessorKey: "monto",
    header: () => <div className="text-right">Monto</div>,
    enableHiding: true,
    cell: ({ row }) => {
      const monto = parseFloat(row.getValue("monto"))
      return (
        <div className="text-right font-semibold">
          {formatCurrency(monto)}
        </div>
      )
    },
  },
]

export function UpcomingPayments() {
  return (
    <DataTable 
      columns={columns} 
      data={payments}
      title="Próximas Cuotas"
      pageSize={6}
    />
  )
}
