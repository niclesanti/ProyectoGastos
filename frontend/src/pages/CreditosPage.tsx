import { useState } from 'react'
import { useAppStore } from '@/store/app-store'
import { useTarjetas, useCreateTarjeta } from '@/features/selectors/api/selector-queries'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { toast } from 'sonner'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
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
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Badge } from '@/components/ui/badge'
import { Plus, CreditCard as CreditCardIcon, Calendar, AlertCircle } from 'lucide-react'
import { cn } from '@/lib/utils'
import type { TarjetaDTOResponse } from '@/types'

const ENTIDADES_FINANCIERAS = [
  'Banco Credicoop',
  'Banco de Santa Fe',
  'Banco Macro',
  'Banco Patagonia',
  'Banco Santander',
  'BBVA',
  'BNA',
  'Brubank',
  'Galicia',
  'HSBC',
  'ICBC',
  'Lemon Cash',
  'Mercado Pago',
  'Naranja X',
  'Personal Pay',
  'Ualá',
]

const REDES_PAGO = ['VISA', 'Mastercard', 'American Express', 'Cabal']

// Schema de validación con Zod
const tarjetaFormSchema = z.object({
  numeroTarjeta: z.string()
    .min(4, { message: 'Debes ingresar los 4 dígitos de la tarjeta.' })
    .max(4, { message: 'Debe tener exactamente 4 dígitos.' })
    .regex(/^[0-9]{4}$/, { message: 'Solo se permiten dígitos numéricos.' }),
  entidadFinanciera: z.string().min(1, { message: 'Por favor, selecciona una entidad financiera.' }),
  redDePago: z.string().min(1, { message: 'Por favor, selecciona una red de pago.' }),
  diaCierre: z.number()
    .int({ message: 'Debe ser un número entero.' })
    .min(1, { message: 'El día debe ser entre 1 y 29.' })
    .max(29, { message: 'El día debe ser entre 1 y 29.' }),
  diaVencimientoPago: z.number()
    .int({ message: 'Debe ser un número entero.' })
    .min(1, { message: 'El día debe ser entre 1 y 29.' })
    .max(29, { message: 'El día debe ser entre 1 y 29.' }),
})

type TarjetaFormValues = z.infer<typeof tarjetaFormSchema>

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
function CreditCardComponent({ card }: { card: TarjetaDTOResponse }) {
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
function AddCardDialog({ espacioTrabajoId }: { espacioTrabajoId: number }) {
  const [open, setOpen] = useState(false)
  const createTarjetaMutation = useCreateTarjeta()

  const form = useForm<TarjetaFormValues>({
    resolver: zodResolver(tarjetaFormSchema),
    defaultValues: {
      numeroTarjeta: '',
      entidadFinanciera: '',
      redDePago: '',
      diaCierre: 1,
      diaVencimientoPago: 1,
    },
  })

  const onSubmit = async (values: TarjetaFormValues) => {
    try {
      await createTarjetaMutation.mutateAsync({
        ...values,
        espacioTrabajoId,
      })
      
      toast.success('Tarjeta registrada', {
        description: 'La tarjeta se ha agregado correctamente.',
      })
      
      setOpen(false)
      form.reset()
    } catch (error: any) {
      console.error('Error al registrar tarjeta:', error)
      toast.error('Error al registrar tarjeta', {
        description: error?.message || 'Intenta nuevamente o contacta al soporte.',
      })
    }
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
        
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4 py-4">
            {/* Últimos 4 dígitos */}
            <FormField
              control={form.control}
              name="numeroTarjeta"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>
                    Últimos 4 dígitos <span className="text-destructive">*</span>
                  </FormLabel>
                  <FormControl>
                    <Input
                      placeholder="1234"
                      maxLength={4}
                      {...field}
                      onChange={(e) => {
                        const value = e.target.value.replace(/\D/g, '').slice(0, 4)
                        field.onChange(value)
                      }}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Entidad Financiera */}
            <FormField
              control={form.control}
              name="entidadFinanciera"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>
                    Entidad Financiera <span className="text-destructive">*</span>
                  </FormLabel>
                  <Select onValueChange={field.onChange} value={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Seleccionar banco" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {ENTIDADES_FINANCIERAS.map((entidad) => (
                        <SelectItem key={entidad} value={entidad}>
                          {entidad}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Red de Pago */}
            <FormField
              control={form.control}
              name="redDePago"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>
                    Red de Pago <span className="text-destructive">*</span>
                  </FormLabel>
                  <Select onValueChange={field.onChange} value={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Seleccionar red" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {REDES_PAGO.map((red) => (
                        <SelectItem key={red} value={red}>
                          {red}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-2 gap-4">
              {/* Día de Cierre */}
              <FormField
                control={form.control}
                name="diaCierre"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      Día de Cierre <span className="text-destructive">*</span>
                    </FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        placeholder="15"
                        min="1"
                        max="29"
                        value={field.value || ''}
                        onChange={(e) => {
                          const val = e.target.value
                          field.onChange(val === '' ? 1 : parseInt(val, 10))
                        }}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {/* Día de Vencimiento */}
              <FormField
                control={form.control}
                name="diaVencimientoPago"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      Día de Vencimiento <span className="text-destructive">*</span>
                    </FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        placeholder="25"
                        min="1"
                        max="29"
                        value={field.value || ''}
                        onChange={(e) => {
                          const val = e.target.value
                          field.onChange(val === '' ? 1 : parseInt(val, 10))
                        }}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className="rounded-lg bg-muted p-3">
              <p className="text-xs text-muted-foreground">
                Solo almacenamos los últimos 4 dígitos por seguridad. 
                Los días deben estar entre 1 y 29 para evitar conflictos con meses de diferente duración.
              </p>
            </div>

            <div className="flex justify-end gap-3 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => setOpen(false)}
                disabled={form.formState.isSubmitting}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={form.formState.isSubmitting}>
                {form.formState.isSubmitting ? 'Guardando...' : 'Guardar Tarjeta'}
              </Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}

export function CreditosPage() {
  const espacioActual = useAppStore((state) => state.currentWorkspace)
  const { data: tarjetas = [], isLoading } = useTarjetas(espacioActual?.id)

  if (!espacioActual) {
    return (
      <div className="flex items-center justify-center h-96">
        <p className="text-muted-foreground">Selecciona un espacio de trabajo</p>
      </div>
    )
  }

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
        <AddCardDialog espacioTrabajoId={espacioActual.id} />
      </div>

      {/* Grid de Tarjetas */}
      {isLoading ? (
        <div className="flex items-center justify-center h-64">
          <p className="text-muted-foreground">Cargando tarjetas...</p>
        </div>
      ) : tarjetas.length > 0 ? (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {tarjetas.map((card) => (
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
            <AddCardDialog espacioTrabajoId={espacioActual.id} />
          </CardContent>
        </Card>
      )}
    </div>
  )
}
