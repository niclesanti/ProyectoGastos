"use client"

import { Card, CardContent, CardHeader } from '@/components/ui/card'
import { Pie, PieChart, Cell, Label } from 'recharts'
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from '@/components/ui/chart'
import { useMemo } from 'react'
import { useDashboardStats } from '@/hooks/useDashboardStats'
import { Loader2, PieChart as PieChartIcon } from 'lucide-react'
import { formatCurrency } from '@/lib/utils'
import { EmptyState } from '@/components/EmptyState'

const generateCategoryColor = (index: number, total: number): string => {
  const hue = (index * 360) / total
  const saturation = 72 + (index % 4) * 4
  const lightness = 48 + (index % 4) * 4
  return `hsl(${hue}, ${saturation}%, ${lightness}%)`
}

export function CreditSpendingByCategory() {
  const { stats, isLoading } = useDashboardStats()

  // Total de compras con crédito del mes actual (último elemento de flujoTarjetaMensual)
  const totalComprasCredito = useMemo(() => {
    if (!stats?.flujoTarjetaMensual || stats.flujoTarjetaMensual.length === 0) return 0
    const mesActual = stats.flujoTarjetaMensual[stats.flujoTarjetaMensual.length - 1]
    return Number(mesActual?.comprasCredito ?? 0)
  }, [stats])

  const chartDataWithColors = useMemo(() => {
    if (!stats?.distribucionComprasCredito || stats.distribucionComprasCredito.length === 0) {
      return []
    }
    return stats.distribucionComprasCredito.map((item, index) => ({
      category: item.motivo,
      value: item.porcentaje,
      fill: generateCategoryColor(index, stats.distribucionComprasCredito.length),
    }))
  }, [stats])

  const chartConfig = useMemo(() => {
    const config: ChartConfig = {
      value: { label: 'Porcentaje' },
    }
    chartDataWithColors.forEach((item) => {
      config[item.category] = {
        label: item.category,
        color: item.fill,
      }
    })
    return config
  }, [chartDataWithColors])

  return (
    <Card className="flex flex-col">
      <CardHeader>
        <h2 className="text-lg sm:text-xl font-semibold">Compras crédito por categoría</h2>
        <p className="text-xs sm:text-sm text-muted-foreground">
          Total {formatCurrency(totalComprasCredito)} este mes
        </p>
      </CardHeader>
      <CardContent className="flex-1 pb-4 sm:pb-6">
        {isLoading ? (
          <div className="flex items-center justify-center min-h-[150px] sm:min-h-[200px]">
            <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
          </div>
        ) : chartDataWithColors.length === 0 ? (
          <EmptyState
            illustration={
              <div className="relative">
                <PieChartIcon className="w-full h-full text-muted-foreground" strokeWidth={1.5} />
              </div>
            }
            title="Sin compras con tarjeta"
            description="Aún no tienes compras con tarjeta este mes. Registra tus compras para ver la distribución por categoría."
          />
        ) : (
          <ChartContainer
            config={chartConfig}
            className="mx-auto aspect-square max-h-[180px] sm:max-h-[240px]"
          >
            <PieChart>
              <ChartTooltip content={<ChartTooltipContent hideLabel />} />
              <Pie
                data={chartDataWithColors}
                dataKey="value"
                nameKey="category"
                innerRadius={60}
                strokeWidth={5}
              >
                {chartDataWithColors.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.fill} />
                ))}
                <Label
                  content={({ viewBox }) => {
                    if (viewBox && 'cx' in viewBox && 'cy' in viewBox) {
                      return (
                        <text
                          x={viewBox.cx}
                          y={viewBox.cy}
                          textAnchor="middle"
                          dominantBaseline="middle"
                        >
                          <tspan
                            x={viewBox.cx}
                            y={viewBox.cy}
                            className="fill-foreground text-3xl font-bold"
                          >
                            {chartDataWithColors.length}
                          </tspan>
                          <tspan
                            x={viewBox.cx}
                            y={(viewBox.cy || 0) + 24}
                            className="fill-muted-foreground"
                          >
                            Categorías
                          </tspan>
                        </text>
                      )
                    }
                  }}
                />
              </Pie>
            </PieChart>
          </ChartContainer>
        )}
        {!isLoading && chartDataWithColors.length > 0 && (
          <div className="mt-4 sm:mt-6 grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4 px-2">
            {chartDataWithColors.map((item) => (
              <div key={item.category} className="flex items-center gap-2">
                <div
                  className="h-3 w-3 rounded-full shrink-0"
                  style={{ backgroundColor: item.fill }}
                />
                <div className="flex flex-col">
                  <span className="text-sm font-medium normal-case">
                    {chartConfig[item.category as keyof typeof chartConfig]?.label}
                  </span>
                  <span className="text-xs text-muted-foreground">{Number(item.value).toFixed(2)}%</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
