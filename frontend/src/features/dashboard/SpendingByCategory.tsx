"use client"

import { Card, CardContent, CardHeader } from '@/components/ui/card'
import { Pie, PieChart, Cell, Label } from 'recharts'
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from '@/components/ui/chart'

const chartData = [
  { category: 'vivienda', value: 35, fill: 'hsl(var(--chart-1))' },
  { category: 'transporte', value: 25, fill: 'hsl(var(--chart-4))' },
  { category: 'alimentacion', value: 20, fill: 'hsl(var(--chart-2))' },
  { category: 'ocio', value: 20, fill: 'hsl(var(--chart-5))' },
]

const chartConfig = {
  value: {
    label: 'Porcentaje',
  },
  vivienda: {
    label: 'Vivienda',
    color: 'hsl(var(--chart-1))',
  },
  transporte: {
    label: 'Transporte',
    color: 'hsl(var(--chart-4))',
  },
  alimentacion: {
    label: 'Alimentación',
    color: 'hsl(var(--chart-2))',
  },
  ocio: {
    label: 'Ocio',
    color: 'hsl(var(--chart-5))',
  },
} satisfies ChartConfig

export function SpendingByCategory() {
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
              data={chartData}
              dataKey="value"
              nameKey="category"
              innerRadius={60}
              strokeWidth={5}
            >
              {chartData.map((entry, index) => (
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
                          4
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
          {chartData.map((item) => (
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
