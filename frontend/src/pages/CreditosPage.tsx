import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { formatCurrency, formatDate } from '@/lib/utils'
import { Plus, CreditCard } from 'lucide-react'

const mockCredits = [
  {
    id: 1,
    description: 'Laptop Dell',
    totalAmount: 85000,
    installments: 12,
    paidInstallments: 4,
    monthlyAmount: 7083.33,
    nextDueDate: '2024-12-25',
  },
  {
    id: 2,
    description: 'Muebles de sala',
    totalAmount: 45000,
    installments: 6,
    paidInstallments: 2,
    monthlyAmount: 7500,
    nextDueDate: '2024-12-28',
  },
  {
    id: 3,
    description: 'Televisor Samsung',
    totalAmount: 120000,
    installments: 18,
    paidInstallments: 8,
    monthlyAmount: 6666.67,
    nextDueDate: '2025-01-05',
  },
]

export function CreditosPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Tarjetas de Crédito</h2>
          <p className="text-muted-foreground">
            Gestiona tus compras a crédito y cuotas
          </p>
        </div>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Nueva Compra a Crédito
        </Button>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {mockCredits.map((credit) => {
          const progress = (credit.paidInstallments / credit.installments) * 100
          const remaining = credit.installments - credit.paidInstallments

          return (
            <Card key={credit.id} className="overflow-hidden">
              <CardHeader className="bg-gradient-to-br from-primary/10 to-primary/5">
                <div className="flex items-start justify-between">
                  <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
                    <CreditCard className="h-6 w-6 text-primary" />
                  </div>
                  <div className="rounded-full bg-background px-3 py-1 text-xs font-medium">
                    {remaining} cuotas restantes
                  </div>
                </div>
                <CardTitle className="mt-4">{credit.description}</CardTitle>
                <CardDescription>
                  {credit.paidInstallments} de {credit.installments} cuotas pagadas
                </CardDescription>
              </CardHeader>
              <CardContent className="pt-6">
                <div className="space-y-4">
                  <div className="space-y-2">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-muted-foreground">Progreso</span>
                      <span className="font-medium">{Math.round(progress)}%</span>
                    </div>
                    <div className="h-2 overflow-hidden rounded-full bg-secondary">
                      <div
                        className="h-full bg-primary transition-all"
                        style={{ width: `${progress}%` }}
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-xs text-muted-foreground">Cuota Mensual</p>
                      <p className="text-lg font-bold">
                        {formatCurrency(credit.monthlyAmount)}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">Total</p>
                      <p className="text-lg font-bold">
                        {formatCurrency(credit.totalAmount)}
                      </p>
                    </div>
                  </div>

                  <div className="rounded-lg border bg-muted/50 p-3">
                    <p className="text-xs text-muted-foreground">Próximo Vencimiento</p>
                    <p className="text-sm font-semibold">
                      {formatDate(credit.nextDueDate)}
                    </p>
                  </div>

                  <Button variant="outline" className="w-full">
                    Ver Detalles
                  </Button>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>

      {mockCredits.length === 0 && (
        <Card>
          <CardContent className="py-12 text-center">
            <CreditCard className="mx-auto h-12 w-12 text-muted-foreground" />
            <h3 className="mt-4 text-lg font-semibold">No hay compras a crédito</h3>
            <p className="mt-2 text-sm text-muted-foreground">
              Comienza agregando tu primera compra a crédito
            </p>
            <Button className="mt-4">
              <Plus className="mr-2 h-4 w-4" />
              Agregar Compra
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
