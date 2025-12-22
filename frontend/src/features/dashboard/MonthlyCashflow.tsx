"use client"

import { Card, CardContent, CardHeader } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from 'recharts'
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
  ChartLegendContent,
  type ChartConfig,
} from '@/components/ui/chart'

const chartData = [
  { month: 'Ene', ingresos: 4000, gastos: 2400 },
  { month: 'Feb', ingresos: 3000, gastos: 1398 },
  { month: 'Mar', ingresos: 5000, gastos: 3800 },
  { month: 'Abr', ingresos: 4500, gastos: 3908 },
  { month: 'May', ingresos: 6000, gastos: 4800 },
  { month: 'Jun', ingresos: 5500, gastos: 3800 },
]

const chartConfig = {
  ingresos: {
    label: 'Ingresos',
    color: 'hsl(142, 76%, 36%)',
  },
  gastos: {
    label: 'Gastos',
    color: 'hsl(0, 84%, 60%)',
  },
} satisfies ChartConfig

export function MonthlyCashflow() {
  return (
    <Card className="col-span-1 lg:col-span-2">
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-semibold">Flujo de Caja Mensual</h2>
            <p className="text-sm text-muted-foreground">Ingresos vs Gastos</p>
          </div>
          <Tabs defaultValue="6months" className="w-auto">
            <TabsList>
              <TabsTrigger value="3months">Últimos 3 meses</TabsTrigger>
              <TabsTrigger value="6months">Últimos 6 meses</TabsTrigger>
              <TabsTrigger value="year">Este año</TabsTrigger>
            </TabsList>
          </Tabs>
        </div>
      </CardHeader>
      <CardContent>
        <ChartContainer config={chartConfig} className="min-h-[350px] w-full">
          <AreaChart accessibilityLayer data={chartData}>
            <defs>
              <linearGradient id="fillIngresos" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="var(--color-ingresos)" stopOpacity={0.8} />
                <stop offset="95%" stopColor="var(--color-ingresos)" stopOpacity={0.1} />
              </linearGradient>
              <linearGradient id="fillGastos" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="var(--color-gastos)" stopOpacity={0.8} />
                <stop offset="95%" stopColor="var(--color-gastos)" stopOpacity={0.1} />
              </linearGradient>
            </defs>
            <CartesianGrid vertical={false} />
            <XAxis
              dataKey="month"
              tickLine={false}
              axisLine={false}
              tickMargin={8}
            />
            <YAxis
              tickLine={false}
              axisLine={false}
              tickMargin={8}
            />
            <ChartTooltip content={<ChartTooltipContent indicator="dot" />} />
            <ChartLegend content={<ChartLegendContent />} />
            <Area
              dataKey="ingresos"
              type="natural"
              fill="url(#fillIngresos)"
              fillOpacity={0.4}
              stroke="var(--color-ingresos)"
              stackId="a"
            />
            <Area
              dataKey="gastos"
              type="natural"
              fill="url(#fillGastos)"
              fillOpacity={0.4}
              stroke="var(--color-gastos)"
              stackId="a"
            />
          </AreaChart>
        </ChartContainer>
      </CardContent>
    </Card>
  )
}
