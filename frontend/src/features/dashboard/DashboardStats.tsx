import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { ArrowUpRight, ArrowDownRight, DollarSign, CreditCard, AlertCircle, Wallet } from 'lucide-react'
import { formatCurrency } from '@/lib/utils'

interface StatsCardProps {
  title: string
  value: string | number
  change?: number
  description: string
  icon: React.ReactNode
  trend?: 'up' | 'down'
}

export function StatsCard({ title, value, change, description, icon, trend }: StatsCardProps) {
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
      </CardContent>
    </Card>
  )
}

export function DashboardStats() {
  return (
    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
      <StatsCard
        title="Balance total"
        value={24500.00}
        change={2.5}
        description="desde el mes pasado"
        icon={<Wallet className="h-4 w-4" />}
        trend="up"
      />
      <StatsCard
        title="Gastos mensuales"
        value={3200.00}
        change={-2}
        description="desde el mes pasado"
        icon={<DollarSign className="h-4 w-4" />}
        trend="down"
      />
      <StatsCard
        title="Próximo vencimiento"
        value={450.00}
        description="Vence 24 Oct"
        icon={<CreditCard className="h-4 w-4" />}
      />
      <StatsCard
        title="Deuda pendiente"
        value={1200.00}
        description="+4 préstamos activos"
        icon={<AlertCircle className="h-4 w-4" />}
      />
    </div>
  )
}
