import { DataTable } from '@/components/ui/data-table'
import { ColumnDef } from '@tanstack/react-table'
import { formatCurrency } from '@/lib/utils'
import { useAppStore } from '@/store/app-store'
import { useEffect, useState, useMemo } from 'react'
import type { CuentaBancaria } from '@/types'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/EmptyState'
import { Landmark } from 'lucide-react'

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
    header: () => <div className="hidden md:table-cell">Entidad</div>,
    enableHiding: true,
    cell: ({ row }) => {
      return <div className="text-muted-foreground text-sm hidden md:table-cell">{row.getValue("entidadFinanciera")}</div>
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

  // Calcular el saldo total
  const saldoTotal = useMemo(() => {
    return accounts.reduce((total, cuenta) => {
      return total + parseFloat(cuenta.saldoActual.toString())
    }, 0)
  }, [accounts])

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

  if (accounts.length === 0) {
    return (
      <div>
        <h3 className="text-lg font-semibold mb-4">Cuentas bancarias</h3>
        <EmptyState
          illustration={
            <div className="relative">
              <Landmark className="w-full h-full text-muted-foreground" strokeWidth={1.5} />
            </div>
          }
          title="Tu billetera está esperando"
          description="Registra tus cuentas bancarias para tener una visión clara de tu saldo disponible."
          size="md"
        />
      </div>
    )
  }

  return (
    <DataTable 
      columns={columns} 
      data={accounts}
      title="Cuentas bancarias"
      pageSize={6}
      footer={
        <div className="bg-muted/30 border-t">
          <div className="grid grid-cols-[1fr_auto] md:grid-cols-[1fr_minmax(150px,1fr)_auto] gap-4 px-4 py-3 items-center">
            <div className="font-semibold text-sm">
              Saldo total
            </div>
            <div className="hidden md:block"></div>
            <div className="text-right font-semibold">
              {formatCurrency(saldoTotal)}
            </div>
          </div>
        </div>
      }
    />
  )
}
