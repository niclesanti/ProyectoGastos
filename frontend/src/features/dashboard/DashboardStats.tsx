import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { ArrowUpRight, ArrowDownRight, DollarSign, CreditCard, AlertCircle, Wallet, Loader2 } from 'lucide-react'
import { formatCurrency } from '@/lib/utils'
import { useDashboardStats } from '@/hooks/useDashboardStats'
import { useAppStore } from '@/store/app-store'
import { useEffect, useState } from 'react'

interface StatsCardProps {
  title: string
  value: string | number
  change?: number
  description: string
  icon: React.ReactNode
  trend?: 'up' | 'down'
  isLoading?: boolean
}

export function StatsCard({ title, value, change, description, icon, trend, isLoading }: StatsCardProps) {
  const formattedValue = typeof value === 'number' ? formatCurrency(value) : value

  return (
    <Card className="relative">
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        <div className="absolute top-4 right-4 text-muted-foreground">
          {icon}
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="flex items-center gap-2">
            <Loader2 className="h-4 w-4 animate-spin" />
            <span className="text-sm text-muted-foreground">Cargando...</span>
          </div>
        ) : (
          <>
            <div className="text-2xl font-bold tabular-nums">{formattedValue}</div>
            <div className="flex items-center gap-2 mt-1">
              <p className="text-xs text-zinc-400">{description}</p>
              {change !== undefined && (
                <Badge variant={trend === 'up' ? 'success' : 'warning'} className="text-[10px] px-1 py-0">
                  {trend === 'up' ? (
                    <ArrowUpRight className="mr-0.5 h-2.5 w-2.5" />
                  ) : (
                    <ArrowDownRight className="mr-0.5 h-2.5 w-2.5" />
                  )}
                  {change > 0 ? '+' : ''}{change}%
                </Badge>
              )}
            </div>
          </>
        )}
      </CardContent>
    </Card>
  )
}

export function DashboardStats() {
  const { stats, isLoading } = useDashboardStats()
  const comprasPendientes = useAppStore((state) => state.comprasPendientes)
  const currentWorkspace = useAppStore((state) => state.currentWorkspace)
  const [cantidadCompras, setCantidadCompras] = useState(0)

  useEffect(() => {
    if (currentWorkspace?.id) {
      const compras = comprasPendientes.get(currentWorkspace.id)
      setCantidadCompras(compras?.data?.length || 0)
    }
  }, [comprasPendientes, currentWorkspace])

  return (
    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
      <StatsCard
        title="Balance total"
        value={stats?.balanceTotal || 0}
        description="desde el mes pasado"
        icon={<Wallet className="h-4 w-4" />}
        isLoading={isLoading}
      />
      <StatsCard
        title="Gastos mensuales"
        value={stats?.gastosMensuales || 0}
        description="desde el mes pasado"
        icon={<DollarSign className="h-4 w-4" />}
        isLoading={isLoading}
      />
      <StatsCard
        title="Próximo vencimiento"
        value={stats?.resumenTarjeta || 0}
        description="Vence fin de mes"
        icon={<CreditCard className="h-4 w-4" />}
        isLoading={isLoading}
      />
      <StatsCard
        title="Deuda pendiente"
        value={stats?.deudaTotalPendiente || 0}
        description={`+${cantidadCompras} préstamos activos`}
        icon={<AlertCircle className="h-4 w-4" />}
        isLoading={isLoading}
      />
    </div>
  )
}
