import { DataTable } from '@/components/ui/data-table'
import { ColumnDef } from '@tanstack/react-table'
import { formatCurrency } from '@/lib/utils'

type Account = {
  id: number
  nombre: string
  entidad: string
  saldo: number
}

const accounts: Account[] = [
  { id: 1, nombre: 'Cuenta principal', entidad: 'Banco Santander', saldo: 15420.50 },
  { id: 2, nombre: 'Ahorros', entidad: 'BBVA', saldo: 8200.00 },
  { id: 3, nombre: 'Nómina', entidad: 'CaixaBank', saldo: 3450.75 },
  { id: 4, nombre: 'Inversiones', entidad: 'ING', saldo: 25600.00 },
  { id: 5, nombre: 'Gastos', entidad: 'Banco Santander', saldo: 1230.25 },
]

export const columns: ColumnDef<Account>[] = [
  {
    accessorKey: "nombre",
    header: "Nombre",
    enableHiding: true,
    cell: ({ row }) => {
      return <div className="font-medium">{row.getValue("nombre")}</div>
    },
  },
  {
    accessorKey: "entidad",
    header: "Entidad",
    enableHiding: true,
    cell: ({ row }) => {
      return <div className="text-muted-foreground">{row.getValue("entidad")}</div>
    },
  },
  {
    accessorKey: "saldo",
    header: () => <div className="text-right">Saldo</div>,
    enableHiding: true,
    cell: ({ row }) => {
      const saldo = parseFloat(row.getValue("saldo"))
      return (
        <div className="text-right font-semibold">
          {formatCurrency(saldo)}
        </div>
      )
    },
  },
]

export function BankAccounts() {
  return (
    <DataTable 
      columns={columns} 
      data={accounts}
      title="Cuentas bancarias"
      pageSize={6}
    />
  )
}
