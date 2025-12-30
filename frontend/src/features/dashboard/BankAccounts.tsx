import { DataTable } from '@/components/ui/data-table'
import { ColumnDef } from '@tanstack/react-table'
import { formatCurrency } from '@/lib/utils'
import { useAppStore } from '@/store/app-store'
import { useEffect, useState } from 'react'
import type { CuentaBancaria } from '@/types'
import { Skeleton } from '@/components/ui/skeleton'

const columns: ColumnDef<CuentaBancaria>[] = [
  {
    accessorKey: "nombre",
    header: "Nombre",
    enableHiding: true,
    cell: ({ row }) => {
      return <div className="font-medium">{row.getValue("nombre")}</div>
    },
  },
  {
    accessorKey: "entidadFinanciera",
    header: "Entidad",
    enableHiding: true,
    cell: ({ row }) => {
      return <div className="text-muted-foreground">{row.getValue("entidadFinanciera")}</div>
    },
  },
  {
    accessorKey: "saldoActual",
    header: () => <div className="text-right">Saldo</div>,
    enableHiding: true,
    cell: ({ row }) => {
      const saldo = parseFloat(row.getValue("saldoActual"))
      return (
        <div className="text-right font-semibold">
          {formatCurrency(saldo)}
        </div>
      )
    },
  },
]

export function BankAccounts() {
  const currentWorkspace = useAppStore((state) => state.currentWorkspace)
  const loadBankAccounts = useAppStore((state) => state.loadBankAccounts)
  const bankAccountsCache = useAppStore((state) => state.bankAccounts)
  
  const [accounts, setAccounts] = useState<CuentaBancaria[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchAccounts = async () => {
      if (!currentWorkspace?.id) {
        setAccounts([])
        setLoading(false)
        setError(null)
        return
      }

      try {
        setLoading(true)
        setError(null)
        const data = await loadBankAccounts(currentWorkspace.id)
        setAccounts(data)
      } catch (err) {
        console.error('Error al cargar cuentas bancarias:', err)
        setError('Error al cargar las cuentas bancarias')
      } finally {
        setLoading(false)
      }
    }

    fetchAccounts()
  }, [currentWorkspace?.id, loadBankAccounts, bankAccountsCache])

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
      data={accounts}
      title="Cuentas bancarias"
      pageSize={6}
    />
  )
}
