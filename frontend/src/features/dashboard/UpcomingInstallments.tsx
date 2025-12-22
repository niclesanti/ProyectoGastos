import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { formatCurrency, formatDate } from '@/lib/utils'
import { CreditCard, Building2, Home } from 'lucide-react'

const installments = [
  {
    id: 1,
    name: 'Suscripción Netflix',
    card: 'Visa •••• 4242',
    amount: 15.99,
    dueDate: '15 Oct',
    icon: <CreditCard className="h-5 w-5" />,
  },
  {
    id: 2,
    name: 'Servicios AWS',
    card: 'Mastercard •••• 8821',
    amount: 64.20,
    dueDate: '18 Oct',
    icon: <Building2 className="h-5 w-5" />,
  },
  {
    id: 3,
    name: 'Alquiler Mensual',
    card: 'Débito Directo',
    amount: 1800.00,
    dueDate: '01 Nov',
    icon: <Home className="h-5 w-5" />,
  },
]

export function UpcomingInstallments() {
  return (
    <Card className="col-span-1 lg:col-span-2">
      <CardHeader>
        <CardTitle>Próximas Cuotas</CardTitle>
        <CardDescription>Tu calendario de pagos</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {installments.map((item) => (
            <div
              key={item.id}
              className="flex items-center justify-between rounded-lg border p-4 transition-colors hover:bg-accent"
            >
              <div className="flex items-center gap-4">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-muted">
                  {item.icon}
                </div>
                <div>
                  <p className="font-medium">{item.name}</p>
                  <p className="text-sm text-muted-foreground">{item.card}</p>
                </div>
              </div>
              <div className="text-right">
                <p className="font-semibold">{formatCurrency(item.amount)}</p>
                <p className="text-sm text-muted-foreground">Vence {item.dueDate}</p>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
