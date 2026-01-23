import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { ArrowUpRight, ArrowDownRight, DollarSign, CreditCard, AlertCircle, Wallet, Loader2 } from 'lucide-react'
import { formatCurrency } from '@/lib/utils'
import { useDashboardStats } from '@/hooks/useDashboardStats'


interface StatsCardProps {
  title: string
  value: string | number
  change?: number
  description: string
  icon: React.ReactNode
  trend?: 'up' | 'down'
  isLoading?: boolean
  highlightNegative?: boolean
}

export function StatsCard({ title, value, change, description, icon, trend, isLoading, highlightNegative }: StatsCardProps) {
  const formattedValue = typeof value === 'number' ? formatCurrency(value) : value
  const isNegative = typeof value === 'number' && value < 0 && highlightNegative
  const valueClass = isNegative ? 'text-rose-300' : 'text-white'

  return (
    <Card className="relative">
      <CardContent className="p-4 sm:p-6">
        <div className="absolute top-4 right-4 text-muted-foreground">
          {icon}
        </div>
        <div className="text-left">
          {isLoading ? (
            <div className="flex items-center gap-2">
              <Loader2 className="h-4 w-4 animate-spin" />
              <span className="text-sm text-muted-foreground">Cargando...</span>
            </div>
          ) : (
            <>
              <p className="text-sm font-medium text-muted-foreground mb-2">{title}</p>
              <div className="flex items-baseline gap-2">
                <h3 className={`text-2xl font-bold ${valueClass}`}>{formattedValue}</h3>
                {change !== undefined && (
                  <Badge variant={trend === 'up' ? 'success' : 'warning'} className="text-xs">
                    {trend === 'up' ? (
                      <ArrowUpRight className="mr-1 h-3 w-3" />
                    ) : (
                      <ArrowDownRight className="mr-1 h-3 w-3" />
                    )}
                    {change > 0 ? '+' : ''}{change}%
                  </Badge>
                )}
              </div>
              <p className="text-xs text-muted-foreground mt-1">{description}</p>
            </>
          )}
        </div>
      </CardContent>
    </Card>
  )
}

export function DashboardStats() {
  const { stats, isLoading } = useDashboardStats()

  return (
    <div className="grid gap-4 md:gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
      <StatsCard
        title="Balance total"
        value={stats?.balanceTotal || 0}
        description="Saldo disponible"
        icon={<Wallet className="h-4 w-4" />}
        isLoading={isLoading}
        highlightNegative
      />
      <StatsCard
        title="Gastos mensuales"
        value={stats?.gastosMensuales || 0}
        description="Marzo del periodo"
        icon={<DollarSign className="h-4 w-4" />}
        isLoading={isLoading}
      />
      <StatsCard
        title="Resumen mensual"
        value={stats?.resumenMensual || 0}
        description="Suma próximos resúmenes"
        icon={<CreditCard className="h-4 w-4" />}
        isLoading={isLoading}
      />
      <StatsCard
        title="Deuda pendiente"
        value={stats?.deudaTotalPendiente || 0}
        description="Sin cuotas activas"
        icon={<AlertCircle className="h-4 w-4" />}
        isLoading={isLoading}
      />
    </div>
  )
}
