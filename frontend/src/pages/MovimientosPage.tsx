import { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { formatCurrency, formatDate } from '@/lib/utils'
import { Plus, Search, Filter, ArrowUpRight, ArrowDownRight } from 'lucide-react'

const mockTransactions = [
  {
    id: 1,
    type: 'INGRESO',
    description: 'Salario Mensual',
    amount: 45000,
    date: '2024-12-01',
    category: 'Trabajo',
  },
  {
    id: 2,
    type: 'GASTO',
    description: 'Supermercado',
    amount: 8500,
    date: '2024-12-05',
    category: 'Alimentación',
  },
  {
    id: 3,
    type: 'GASTO',
    description: 'Netflix',
    amount: 1200,
    date: '2024-12-10',
    category: 'Entretenimiento',
  },
  {
    id: 4,
    type: 'INGRESO',
    description: 'Freelance',
    amount: 15000,
    date: '2024-12-12',
    category: 'Trabajo',
  },
  {
    id: 5,
    type: 'GASTO',
    description: 'Gasolina',
    amount: 6000,
    date: '2024-12-15',
    category: 'Transporte',
  },
]

export function MovimientosPage() {
  const [filter, setFilter] = useState('all')
  const [searchTerm, setSearchTerm] = useState('')

  const filteredTransactions = mockTransactions.filter((transaction) => {
    const matchesFilter = filter === 'all' || transaction.type === filter
    const matchesSearch =
      transaction.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
      transaction.category.toLowerCase().includes(searchTerm.toLowerCase())
    return matchesFilter && matchesSearch
  })

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Movimientos</h2>
          <p className="text-muted-foreground">
            Gestiona tus ingresos y gastos
          </p>
        </div>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Nueva Transacción
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Todas las Transacciones</CardTitle>
              <CardDescription>
                {filteredTransactions.length} transacciones encontradas
              </CardDescription>
            </div>
            
            <div className="flex gap-2">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Buscar..."
                  className="pl-10 w-64"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              
              <Select value={filter} onValueChange={setFilter}>
                <SelectTrigger className="w-40">
                  <Filter className="mr-2 h-4 w-4" />
                  <SelectValue placeholder="Filtrar" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">Todos</SelectItem>
                  <SelectItem value="INGRESO">Ingresos</SelectItem>
                  <SelectItem value="GASTO">Gastos</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {filteredTransactions.map((transaction) => (
              <div
                key={transaction.id}
                className="flex items-center justify-between rounded-lg border p-4 transition-colors hover:bg-accent"
              >
                <div className="flex items-center gap-4">
                  <div
                    className={`flex h-12 w-12 items-center justify-center rounded-full ${
                      transaction.type === 'INGRESO'
                        ? 'bg-green-500/10 text-green-500'
                        : 'bg-red-500/10 text-red-500'
                    }`}
                  >
                    {transaction.type === 'INGRESO' ? (
                      <ArrowUpRight className="h-6 w-6" />
                    ) : (
                      <ArrowDownRight className="h-6 w-6" />
                    )}
                  </div>
                  <div>
                    <p className="font-semibold">{transaction.description}</p>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <span>{transaction.category}</span>
                      <span>•</span>
                      <span>{formatDate(transaction.date)}</span>
                    </div>
                  </div>
                </div>
                <div className="text-right">
                  <p
                    className={`text-lg font-bold ${
                      transaction.type === 'INGRESO' ? 'text-green-500' : 'text-red-500'
                    }`}
                  >
                    {transaction.type === 'INGRESO' ? '+' : '-'}
                    {formatCurrency(transaction.amount)}
                  </p>
                </div>
              </div>
            ))}
          </div>

          {filteredTransactions.length === 0 && (
            <div className="py-12 text-center">
              <p className="text-muted-foreground">No se encontraron transacciones</p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
