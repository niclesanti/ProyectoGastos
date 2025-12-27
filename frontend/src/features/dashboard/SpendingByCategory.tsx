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

// Función para generar colores con alta saturación similares al chart de flujo de caja
const generateCategoryColor = (index: number, total: number): string => {
  // Distribuir los tonos uniformemente en el espectro de colores (0-360)
  const hue = (index * 360) / total
  // Saturación alta entre 72% y 84% para mantener consistencia con el diseño
  const saturation = 72 + (index % 4) * 4 // Alterna entre 72%, 76%, 80%, 84%
  // Luminosidad entre 48% y 62% para buena visibilidad
  const lightness = 48 + (index % 4) * 4 // Alterna entre 48%, 52%, 56%, 60%
  
  return `hsl(${hue}, ${saturation}%, ${lightness}%)`
}

const chartData = [
  { category: 'vivienda', value: 35 },
  { category: 'transporte', value: 25 },
  { category: 'alimentacion', value: 20 },
  { category: 'ocio', value: 20 },
]

const categoryLabels: Record<string, string> = {
  vivienda: 'Vivienda',
  transporte: 'Transporte',
  alimentacion: 'Alimentación',
  ocio: 'Ocio',
}

export function SpendingByCategory() {
  // Generar colores dinámicamente para cada categoría
  const chartDataWithColors = useMemo(() => {
    return chartData.map((item, index) => ({
      ...item,
      fill: generateCategoryColor(index, chartData.length),
    }))
  }, [])

  // Generar configuración dinámica para el chart
  const chartConfig = useMemo(() => {
    const config: ChartConfig = {
      value: {
        label: 'Porcentaje',
      },
    }
    
    chartDataWithColors.forEach((item) => {
      config[item.category] = {
        label: categoryLabels[item.category] || item.category,
        color: item.fill,
      }
    })
    
    return config
  }, [chartDataWithColors])

  return (
    <Card className="flex flex-col">
      <CardHeader>
        <h2 className="text-xl font-semibold">Gastos por categoría</h2>
        <p className="text-sm text-muted-foreground">Total $3,200 este mes</p>
      </CardHeader>
      <CardContent className="flex-1 pb-6">
        <ChartContainer
          config={chartConfig}
          className="mx-auto aspect-square max-h-[250px]"
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
                  if (viewBox && "cx" in viewBox && "cy" in viewBox) {
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
                          {chartData.length}
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
        <div className="mt-6 grid grid-cols-2 gap-4 px-2">
          {chartDataWithColors.map((item) => (
            <div key={item.category} className="flex items-center gap-2">
              <div
                className="h-3 w-3 rounded-full shrink-0"
                style={{ backgroundColor: item.fill }}
              />
              <div className="flex flex-col">
                <span className="text-sm font-medium capitalize">
                  {chartConfig[item.category as keyof typeof chartConfig]?.label}
                </span>
                <span className="text-xs text-muted-foreground">{item.value}%</span>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
