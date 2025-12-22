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
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        {change !== undefined && (
          <Badge variant={trend === 'up' ? 'success' : 'warning'} className="ml-auto">
            {trend === 'up' ? (
              <ArrowUpRight className="mr-1 h-3 w-3" />
            ) : (
              <ArrowDownRight className="mr-1 h-3 w-3" />
            )}
            {change > 0 ? '+' : ''}{change}%
          </Badge>
        )}
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{formattedValue}</div>
        <p className="text-xs text-muted-foreground mt-1">{description}</p>
      </CardContent>
    </Card>
  )
}

export function DashboardStats() {
  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <StatsCard
        title="Balance Total"
        value={24500.00}
        change={2.5}
        description="desde el mes pasado"
        icon={<Wallet className="h-4 w-4" />}
        trend="up"
      />
      <StatsCard
        title="Gastos Mensuales"
        value={3200.00}
        change={-2}
        description="desde el mes pasado"
        icon={<DollarSign className="h-4 w-4" />}
        trend="down"
      />
      <StatsCard
        title="Próximo Vencimiento"
        value={450.00}
        description="Vence 24 Oct"
        icon={<CreditCard className="h-4 w-4" />}
      />
      <StatsCard
        title="Deuda Pendiente"
        value={1200.00}
        description="+4 préstamos activos"
        icon={<AlertCircle className="h-4 w-4" />}
      />
    </div>
  )
}
