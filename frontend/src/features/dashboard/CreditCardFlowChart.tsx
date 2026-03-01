"use client"

import { Card, CardContent, CardHeader } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  Area,
  AreaChart,
  CartesianGrid,
  XAxis,
  YAxis,
} from 'recharts'
import {
  ChartContainer,
  ChartTooltip,
  ChartLegend,
  ChartLegendContent,
  type ChartConfig,
} from '@/components/ui/chart'
import { useDashboardStats } from '@/hooks/useDashboardStats'
import { Loader2, CreditCard } from 'lucide-react'
import { useMemo, useState } from 'react'
import { formatCurrency } from '@/lib/utils'
import { EmptyState } from '@/components/EmptyState'

const chartConfig = {
  comprasCredito: {
    label: 'Compras con crédito',
    color: 'hsl(var(--chart-4))',
  },
  pagoResumen: {
    label: 'Pago de resumen',
    color: 'hsl(var(--chart-5))',
  },
} satisfies ChartConfig

const monthMap: Record<string, string> = {
  '01': 'Ene',
  '02': 'Feb',
  '03': 'Mar',
  '04': 'Abr',
  '05': 'May',
  '06': 'Jun',
  '07': 'Jul',
  '08': 'Ago',
  '09': 'Sep',
  '10': 'Oct',
  '11': 'Nov',
  '12': 'Dic',
}

export function CreditCardFlowChart() {
  const { stats, isLoading } = useDashboardStats()
  const [range, setRange] = useState<'3months' | '6months' | '12months'>('6months')

  const chartData = useMemo(() => {
    if (!stats?.flujoTarjetaMensual) return []

    const allData = stats.flujoTarjetaMensual.map((item) => {
      const [, month] = item.mes.split('-')
      return {
        month: monthMap[month] || item.mes,
        fullDate: item.mes,
        comprasCredito: Number(item.comprasCredito),
        pagoResumen: Number(item.pagoResumen),
      }
    })

    if (range === '3months') return allData.slice(-3)
    if (range === '6months') return allData.slice(-6)
    return allData.slice(-12)
  }, [stats, range])

  const hasRealData = useMemo(() => {
    return chartData.some((item) => item.comprasCredito > 0 || item.pagoResumen > 0)
  }, [chartData])

  const formatYAxis = (value: number) => {
    if (value >= 1000000) return `$${(value / 1000000).toFixed(1)}M`
    if (value >= 1000) return `$${(value / 1000).toFixed(0)}k`
    return `$${value}`
  }

  const CustomTooltip = ({ active, payload }: any) => {
    if (!active || !payload || payload.length === 0) return null

    const compras = payload.find((p: any) => p.dataKey === 'comprasCredito')?.value || 0
    const pagos = payload.find((p: any) => p.dataKey === 'pagoResumen')?.value || 0

    return (
      <div className="rounded-lg border bg-background p-2 sm:p-3 shadow-md max-w-[280px]">
        <p className="font-semibold mb-1 sm:mb-2 text-xs sm:text-sm">{payload[0].payload.month}</p>
        <div className="space-y-0.5 sm:space-y-1">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full" style={{ backgroundColor: 'hsl(var(--chart-4))' }} />
            <span className="text-sm text-muted-foreground">Compras:</span>
            <span className="text-sm font-semibold ml-auto">{formatCurrency(compras)}</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full" style={{ backgroundColor: 'hsl(var(--chart-5))' }} />
            <span className="text-sm text-muted-foreground">Pago resumen:</span>
            <span className="text-sm font-semibold ml-auto">{formatCurrency(pagos)}</span>
          </div>
        </div>
      </div>
    )
  }

  return (
    <Card className="col-span-1 lg:col-span-2">
      <CardHeader>
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 sm:gap-4">
          <div>
            <h2 className="text-lg sm:text-xl font-semibold">Flujo de tarjeta mensual</h2>
            <p className="text-xs sm:text-sm text-muted-foreground">Compras con crédito vs pagos de resumen</p>
          </div>
          <Tabs value={range} onValueChange={(value) => setRange(value as any)} className="w-full sm:w-auto">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="3months" className="text-xs sm:text-sm px-2 sm:px-4">3m</TabsTrigger>
              <TabsTrigger value="6months" className="text-xs sm:text-sm px-2 sm:px-4">6m</TabsTrigger>
              <TabsTrigger value="12months" className="text-xs sm:text-sm px-2 sm:px-4">12m</TabsTrigger>
            </TabsList>
          </Tabs>
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="flex items-center justify-center h-[250px] sm:h-[300px] lg:h-[350px]">
            <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
          </div>
        ) : !hasRealData ? (
          <EmptyState
            illustration={
              <div className="relative">
                <CreditCard className="w-full h-full text-muted-foreground" strokeWidth={1.5} />
              </div>
            }
            title="Sin compras con tarjeta"
            description="Comienza a registrar tus compras con crédito para ver el flujo de tu tarjeta aquí."
          />
        ) : (
          <ChartContainer config={chartConfig} className="h-[200px] sm:h-[280px] lg:h-[350px] w-full">
            <AreaChart data={chartData} margin={{ left: -20, right: 10, top: 10, bottom: 0 }}>
              <defs>
                <linearGradient id="colorCompras" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="hsl(var(--chart-4))" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="hsl(var(--chart-4))" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="colorPagos" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="hsl(var(--chart-5))" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="hsl(var(--chart-5))" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid vertical={false} strokeDasharray="3 3" stroke="hsl(var(--border))" opacity={0.3} />
              <XAxis
                dataKey="month"
                tickLine={false}
                axisLine={false}
                tickMargin={8}
                tick={{ fill: 'hsl(var(--muted-foreground))', fontSize: 11 }}
              />
              <YAxis
                tickLine={false}
                axisLine={false}
                tickMargin={8}
                tickFormatter={formatYAxis}
                tick={{ fill: 'hsl(var(--muted-foreground))', fontSize: 11 }}
              />
              <ChartTooltip content={<CustomTooltip />} />
              <ChartLegend content={<ChartLegendContent />} />
              <Area
                type="monotone"
                dataKey="comprasCredito"
                stroke="hsl(var(--chart-4))"
                strokeWidth={2}
                fill="url(#colorCompras)"
                dot={false}
                activeDot={{ r: 4 }}
              />
              <Area
                type="monotone"
                dataKey="pagoResumen"
                stroke="hsl(var(--chart-5))"
                strokeWidth={2}
                fill="url(#colorPagos)"
                dot={false}
                activeDot={{ r: 4 }}
              />
            </AreaChart>
          </ChartContainer>
        )}
      </CardContent>
    </Card>
  )
}
