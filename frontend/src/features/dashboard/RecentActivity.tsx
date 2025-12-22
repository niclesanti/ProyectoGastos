import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { formatCurrency, formatDateTime } from '@/lib/utils'
import { ArrowUpRight, ArrowDownRight } from 'lucide-react'
import { Button } from '@/components/ui/button'

const transactions = [
  {
    id: 1,
    description: 'Salario Upwork',
    amount: 4200.00,
    date: 'Hoy, 10:23 AM',
    type: 'income',
    status: 'success',
  },
  {
    id: 2,
    description: 'Viaje Uber',
    amount: -24.50,
    date: 'Ayer, 8:45 PM',
    type: 'expense',
    status: 'pending',
  },
  {
    id: 3,
    description: 'Spotify Premium',
    amount: -12.00,
    date: '12 Oct, 2023',
    type: 'expense',
    status: 'success',
  },
  {
    id: 4,
    description: 'Apple Store',
    amount: -124.99,
    date: '10 Oct, 2023',
    type: 'expense',
    status: 'success',
  },
]

export function RecentActivity() {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div>
          <CardTitle>Actividad Reciente</CardTitle>
          <CardDescription>Tus últimas transacciones</CardDescription>
        </div>
        <Button variant="ghost" size="sm">
          Ver Todo
        </Button>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {transactions.map((transaction) => (
            <div
              key={transaction.id}
              className="flex items-center justify-between rounded-lg border p-3 transition-colors hover:bg-accent"
            >
              <div className="flex items-center gap-3">
                <div
                  className={`flex h-10 w-10 items-center justify-center rounded-full ${
                    transaction.type === 'income'
                      ? 'bg-green-500/10 text-green-500'
                      : 'bg-red-500/10 text-red-500'
                  }`}
                >
                  {transaction.type === 'income' ? (
                    <ArrowUpRight className="h-5 w-5" />
                  ) : (
                    <ArrowDownRight className="h-5 w-5" />
                  )}
                </div>
                <div>
                  <p className="font-medium">{transaction.description}</p>
                  <p className="text-xs text-muted-foreground">{transaction.date}</p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                {transaction.status === 'pending' && (
                  <span className="rounded-full bg-yellow-500/10 px-2 py-1 text-xs font-medium text-yellow-500">
                    Pendiente
                  </span>
                )}
                {transaction.status === 'success' && (
                  <span className="rounded-full bg-green-500/10 px-2 py-1 text-xs font-medium text-green-500">
                    Completado
                  </span>
                )}
                <p
                  className={`font-semibold ${
                    transaction.type === 'income' ? 'text-green-500' : 'text-red-500'
                  }`}
                >
                  {transaction.type === 'income' ? '+' : ''}
                  {formatCurrency(transaction.amount)}
                </p>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
