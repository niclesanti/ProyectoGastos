"use client"

import { Card, CardContent, CardHeader } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Bar, BarChart, CartesianGrid, XAxis, YAxis } from 'recharts'
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
  ChartLegendContent,
  type ChartConfig,
} from '@/components/ui/chart'
import { useDashboardStats } from '@/hooks/useDashboardStats'
import { Loader2 } from 'lucide-react'
import { useMemo, useState } from 'react'
import { formatCurrency } from '@/lib/utils'

const chartConfig = {
  ingresos: {
    label: 'Ingresos',
    color: 'hsl(var(--chart-2))',
  },
  gastos: {
    label: 'Gastos',
    color: 'hsl(var(--chart-3))',
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

export function MonthlyCashflow() {
  const { stats, isLoading } = useDashboardStats()
  const [range, setRange] = useState<'3months' | '6months' | '12months'>('6months')

  const chartData = useMemo(() => {
    if (!stats?.flujoMensual) return []
    
    const allData = stats.flujoMensual.map((item) => {
      const [year, month] = item.mes.split('-')
      return {
        month: monthMap[month] || item.mes,
        fullDate: item.mes,
        ingresos: Number(item.ingresos),
        gastos: Number(item.gastos),
      }
    })

    // Filtrar según el rango seleccionado
    let filtered = allData
    if (range === '3months') {
      filtered = allData.slice(-3)
    } else if (range === '6months') {
      filtered = allData.slice(-6)
    } else if (range === '12months') {
      // Mostrar los últimos 12 meses
      filtered = allData.slice(-12)
    }

    return filtered
  }, [stats, range])

  // Función para formatear el eje Y con valores abreviados
  const formatYAxis = (value: number) => {
    if (value >= 1000000) {
      return `$${(value / 1000000).toFixed(1)}M`
    } else if (value >= 1000) {
      return `$${(value / 1000).toFixed(0)}k`
    }
    return `$${value}`
  }

  // Tooltip personalizado
  const CustomTooltip = ({ active, payload }: any) => {
    if (!active || !payload || payload.length === 0) return null

    const ingresos = payload.find((p: any) => p.dataKey === 'ingresos')?.value || 0
    const gastos = payload.find((p: any) => p.dataKey === 'gastos')?.value || 0
    const balance = ingresos - gastos

    return (
      <div className="rounded-lg border bg-background p-2 sm:p-3 shadow-md max-w-[280px]">
        <p className="font-semibold mb-1 sm:mb-2 text-xs sm:text-sm">{payload[0].payload.month}</p>
        <div className="space-y-0.5 sm:space-y-1">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full" style={{ backgroundColor: 'hsl(var(--chart-2))' }} />
            <span className="text-sm text-muted-foreground">Ingresos:</span>
            <span className="text-sm font-semibold ml-auto">{formatCurrency(ingresos)}</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full" style={{ backgroundColor: 'hsl(var(--chart-3))' }} />
            <span className="text-sm text-muted-foreground">Gastos:</span>
            <span className="text-sm font-semibold ml-auto">{formatCurrency(gastos)}</span>
          </div>
          <div className="pt-2 mt-2 border-t">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium">Balance Neto:</span>
              <span className={`text-sm font-bold ${balance >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
                {balance >= 0 ? '+' : ''}{formatCurrency(balance)}
              </span>
            </div>
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
            <h2 className="text-lg sm:text-xl font-semibold">Flujo de caja mensual</h2>
            <p className="text-xs sm:text-sm text-muted-foreground">Ingresos vs gastos</p>
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
        ) : (
          <ChartContainer config={chartConfig} className="h-[200px] sm:h-[280px] lg:h-[350px] w-full">
            <BarChart accessibilityLayer data={chartData} margin={{ left: -20, right: 10, top: 10, bottom: 0 }}>
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
              <ChartTooltip content={<CustomTooltip />} cursor={{ fill: 'hsl(var(--muted) / 0.1)' }} />
              <ChartLegend content={<ChartLegendContent />} />
              <Bar
                dataKey="ingresos"
                fill="hsl(var(--chart-2))"
                radius={[4, 4, 0, 0]}
              />
              <Bar
                dataKey="gastos"
                fill="hsl(var(--chart-3))"
                radius={[4, 4, 0, 0]}
              />
            </BarChart>
          </ChartContainer>
        )}
      </CardContent>
    </Card>
  )
}
