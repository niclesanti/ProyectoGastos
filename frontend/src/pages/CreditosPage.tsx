import { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Badge } from '@/components/ui/badge'
import { Plus, CreditCard as CreditCardIcon, Calendar, AlertCircle } from 'lucide-react'
import { cn } from '@/lib/utils'

interface CreditCard {
  id: string
  numeroTarjeta: string
  entidadFinanciera: string
  redDePago: string
  diaCierre: number
  diaVencimientoPago: number
}

const mockCards: CreditCard[] = [
  {
    id: '1',
    numeroTarjeta: '1234',
    entidadFinanciera: 'Banco Galicia',
    redDePago: 'Visa',
    diaCierre: 15,
    diaVencimientoPago: 25,
  },
  {
    id: '2',
    numeroTarjeta: '5678',
    entidadFinanciera: 'Banco Santander',
    redDePago: 'Mastercard',
    diaCierre: 10,
    diaVencimientoPago: 20,
  },
  {
    id: '3',
    numeroTarjeta: '9012',
    entidadFinanciera: 'BBVA',
    redDePago: 'Visa',
    diaCierre: 5,
    diaVencimientoPago: 15,
  },
]

// Función para calcular días hasta el próximo cierre
const calculateDaysUntilClosure = (diaCierre: number): number => {
  const today = new Date()
  const currentDay = today.getDate()
  const currentMonth = today.getMonth()
  const currentYear = today.getFullYear()

  let closureDate = new Date(currentYear, currentMonth, diaCierre)
  
  if (currentDay >= diaCierre) {
    closureDate = new Date(currentYear, currentMonth + 1, diaCierre)
  }

  const diffTime = closureDate.getTime() - today.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  return diffDays
}

// Componente de tarjeta visual
function CreditCardComponent({ card }: { card: CreditCard }) {
  const daysUntilClosure = calculateDaysUntilClosure(card.diaCierre)
  
  // Color dinámico según días hasta cierre
  const getClosureBadgeColor = (days: number) => {
    if (days === 0) return 'bg-red-500/20 text-red-400 border-red-500/30'
    if (days <= 5) return 'bg-amber-500/20 text-amber-400 border-amber-500/30'
    return 'bg-blue-500/20 text-blue-400 border-blue-500/30'
  }
  
  // Colores según red de pago
  const getCardColor = (red: string) => {
    switch (red.toLowerCase()) {
      case 'visa':
        return 'from-blue-900 to-blue-700'
      case 'mastercard':
        return 'from-gray-900 to-gray-700'
      case 'amex':
        return 'from-green-900 to-green-700'
      default:
        return 'from-zinc-900 to-zinc-700'
    }
  }

  return (
    <Card className="overflow-hidden hover:shadow-lg transition-shadow">
      <div className={cn('h-48 p-6 flex flex-col justify-between bg-gradient-to-br', getCardColor(card.redDePago))}>
        <div className="flex items-start justify-between">
          <div className="text-white/80 text-sm font-medium">
            {card.redDePago}
          </div>
          <div className={cn('backdrop-blur-sm px-3 py-1 rounded-full border', getClosureBadgeColor(daysUntilClosure))}>
            <p className="text-xs font-medium">
              {daysUntilClosure === 0 ? 'Cierra hoy' : 
               daysUntilClosure === 1 ? 'Cierra mañana' :
               `Cierra en ${daysUntilClosure} días`}
            </p>
          </div>
        </div>

        <div>
          <div className="flex items-center gap-2 mb-3">
            <CreditCardIcon className="h-8 w-8 text-white/60" />
            <p className="text-white/80 text-lg tracking-wider">
              **** **** **** {card.numeroTarjeta}
            </p>
          </div>
          <p className="text-white font-semibold text-lg">
            {card.entidadFinanciera}
          </p>
        </div>
      </div>

      <CardContent className="pt-4">
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <Badge variant="outline" className="flex items-center gap-1">
              <Calendar className="h-3 w-3" />
              Cierre: {card.diaCierre}
            </Badge>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant="outline" className="flex items-center gap-1">
              <AlertCircle className="h-3 w-3" />
              Vto: {card.diaVencimientoPago}
            </Badge>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

// Modal de registro
function AddCardDialog() {
  const [open, setOpen] = useState(false)
  const [numeroTarjeta, setNumeroTarjeta] = useState('')
  const [entidadFinanciera, setEntidadFinanciera] = useState('')
  const [redDePago, setRedDePago] = useState('')
  const [diaCierre, setDiaCierre] = useState('')
  const [diaVencimientoPago, setDiaVencimientoPago] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    console.log({
      numeroTarjeta,
      entidadFinanciera,
      redDePago,
      diaCierre: parseInt(diaCierre),
      diaVencimientoPago: parseInt(diaVencimientoPago),
    })
    setOpen(false)
    // Resetear formulario
    setNumeroTarjeta('')
    setEntidadFinanciera('')
    setRedDePago('')
    setDiaCierre('')
    setDiaVencimientoPago('')
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Agregar Tarjeta
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Agregar Tarjeta de Crédito</DialogTitle>
          <DialogDescription>
            Registra una nueva tarjeta para controlar cierres y vencimientos.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-4 py-4">
            {/* Últimos 4 dígitos */}
            <div className="space-y-1.5">
              <Label htmlFor="numeroTarjeta" className="text-sm">
                Últimos 4 dígitos <span className="text-destructive">*</span>
              </Label>
              <Input
                id="numeroTarjeta"
                placeholder="1234"
                value={numeroTarjeta}
                onChange={(e) => {
                  const value = e.target.value.replace(/\D/g, '').slice(0, 4)
                  setNumeroTarjeta(value)
                }}
                maxLength={4}
                required
                className="h-9"
              />
            </div>

            {/* Entidad Financiera */}
            <div className="space-y-1.5">
              <Label htmlFor="entidadFinanciera" className="text-sm">
                Entidad Financiera <span className="text-destructive">*</span>
              </Label>
              <Select value={entidadFinanciera} onValueChange={setEntidadFinanciera} required>
                <SelectTrigger id="entidadFinanciera" className="h-9">
                  <SelectValue placeholder="Seleccionar banco" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Banco Nación">Banco Nación</SelectItem>
                  <SelectItem value="Banco Provincia">Banco Provincia</SelectItem>
                  <SelectItem value="Banco Ciudad">Banco Ciudad</SelectItem>
                  <SelectItem value="Banco Galicia">Banco Galicia</SelectItem>
                  <SelectItem value="Banco Santander">Banco Santander</SelectItem>
                  <SelectItem value="BBVA">BBVA</SelectItem>
                  <SelectItem value="Banco Macro">Banco Macro</SelectItem>
                  <SelectItem value="ICBC">ICBC</SelectItem>
                  <SelectItem value="Banco Supervielle">Banco Supervielle</SelectItem>
                  <SelectItem value="HSBC">HSBC</SelectItem>
                  <SelectItem value="Banco Patagonia">Banco Patagonia</SelectItem>
                  <SelectItem value="Banco Comafi">Banco Comafi</SelectItem>
                  <SelectItem value="Banco Itaú">Banco Itaú</SelectItem>
                  <SelectItem value="Brubank">Brubank</SelectItem>
                  <SelectItem value="Ualá">Ualá</SelectItem>
                  <SelectItem value="Mercado Pago">Mercado Pago</SelectItem>
                  <SelectItem value="Naranja X">Naranja X</SelectItem>
                  <SelectItem value="Tarjeta Nevada">Tarjeta Nevada</SelectItem>
                  <SelectItem value="Otro">Otro</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Red de Pago */}
            <div className="space-y-1.5">
              <Label htmlFor="redDePago" className="text-sm">
                Red de Pago <span className="text-destructive">*</span>
              </Label>
              <Select value={redDePago} onValueChange={setRedDePago} required>
                <SelectTrigger id="redDePago" className="h-9">
                  <SelectValue placeholder="Seleccionar red" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Visa">Visa</SelectItem>
                  <SelectItem value="Mastercard">Mastercard</SelectItem>
                  <SelectItem value="Amex">American Express</SelectItem>
                  <SelectItem value="Cabal">Cabal</SelectItem>
                  <SelectItem value="Naranja">Naranja</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="grid grid-cols-2 gap-4">
              {/* Día de Cierre */}
              <div className="space-y-1.5">
                <Label htmlFor="diaCierre" className="text-sm">
                  Día de Cierre <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="diaCierre"
                  type="number"
                  placeholder="15"
                  value={diaCierre}
                  onChange={(e) => {
                    const value = parseInt(e.target.value)
                    if (value >= 1 && value <= 29) {
                      setDiaCierre(e.target.value)
                    } else if (e.target.value === '') {
                      setDiaCierre('')
                    }
                  }}
                  min="1"
                  max="29"
                  required
                  className="h-9"
                />
                <p className="text-xs text-muted-foreground">1-29</p>
              </div>

              {/* Día de Vencimiento */}
              <div className="space-y-1.5">
                <Label htmlFor="diaVencimiento" className="text-sm">
                  Día de Vencimiento <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="diaVencimiento"
                  type="number"
                  placeholder="25"
                  value={diaVencimientoPago}
                  onChange={(e) => {
                    const value = parseInt(e.target.value)
                    if (value >= 1 && value <= 29) {
                      setDiaVencimientoPago(e.target.value)
                    } else if (e.target.value === '') {
                      setDiaVencimientoPago('')
                    }
                  }}
                  min="1"
                  max="29"
                  required
                  className="h-9"
                />
                <p className="text-xs text-muted-foreground">1-29</p>
              </div>
            </div>

            <div className="rounded-lg bg-muted p-3">
              <p className="text-xs text-muted-foreground">
                <strong>Nota:</strong> Solo almacenamos los últimos 4 dígitos por seguridad. 
                Los días deben estar entre 1 y 29 para evitar conflictos con meses de diferente duración.
              </p>
            </div>
          </div>
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => setOpen(false)}
            >
              Cancelar
            </Button>
            <Button type="submit">Guardar Tarjeta</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export function CreditosPage() {
  const [cards] = useState<CreditCard[]>(mockCards)

  return (
    <div className="space-y-6 pt-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Tarjetas de crédito</h2>
          <p className="text-muted-foreground">
            Gestiona tus tarjetas y controla cierres y vencimientos
          </p>
        </div>
        <AddCardDialog />
      </div>

      {/* Grid de Tarjetas */}
      {cards.length > 0 ? (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {cards.map((card) => (
            <CreditCardComponent key={card.id} card={card} />
          ))}
        </div>
      ) : (
        /* Empty State */
        <Card>
          <CardContent className="py-16 text-center">
            <div className="mx-auto w-16 h-16 rounded-full bg-muted flex items-center justify-center mb-4">
              <CreditCardIcon className="h-8 w-8 text-muted-foreground" />
            </div>
            <h3 className="text-lg font-semibold mb-2">No tienes tarjetas registradas</h3>
            <p className="text-sm text-muted-foreground mb-6">
              Agrégalas para controlar tus cierres y vencimientos
            </p>
            <AddCardDialog />
          </CardContent>
        </Card>
      )}
    </div>
  )
}
