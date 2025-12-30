"use client"

import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { useAppStore } from '@/store/app-store'
import { useDashboardCache } from '@/hooks'
import { 
  useTarjetas, 
  useCuentasBancarias, 
  useCuotasTarjeta,
  useCreateCuentaBancaria 
} from '@/features/selectors/api/selector-queries'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Calendar } from '@/components/ui/calendar'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import { ScrollArea } from '@/components/ui/scroll-area'
import { CalendarIcon, Plus } from 'lucide-react'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import { cn } from '@/lib/utils'
import { toast } from 'sonner'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Checkbox } from '@/components/ui/checkbox'

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
  'UalÃ¡',
]

// Esquema de validación para nueva cuenta
const newCuentaSchema = z.object({
  nombre: z.string()
    .min(1, { message: "El nombre de la cuenta es obligatorio." })
    .max(50, { message: "El nombre no puede exceder los 50 caracteres." })
    .regex(/^[a-zA-Z0-9,()_\-/\s]*$/, { 
      message: "Solo se permiten letras, números, comas, paréntesis, guiones y barras." 
    }),
  entidadFinanciera: z.string().min(1, { message: "Debes seleccionar una entidad financiera." }),
})

// Esquema de validación principal
const cardPaymentFormSchema = z.object({
  tarjeta: z.string().min(1, { message: "Por favor, selecciona una tarjeta de crédito." }),
  fecha: z.date().refine((date) => date <= new Date(), {
    message: "La fecha no puede ser futura.",
  }),
  cuenta: z.string().optional(),
})

type CardPaymentFormValues = z.infer<typeof cardPaymentFormSchema>

interface CardPaymentModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function CardPaymentModal({ open, onOpenChange }: CardPaymentModalProps) {
  const currentWorkspace = useAppStore((state) => state.currentWorkspace)
  
  // Cargar datos con TanStack Query
  const { data: tarjetas = [], isLoading: loadingTarjetas } = useTarjetas(currentWorkspace?.id)
  const { data: cuentas = [], isLoading: loadingCuentas } = useCuentasBancarias(currentWorkspace?.id)
  
  // Estados para cuotas y tarjeta seleccionada
  const [selectedTarjetaId, setSelectedTarjetaId] = useState<number | undefined>()
  const { data: cuotas = [], isLoading: loadingCuotas } = useCuotasTarjeta(selectedTarjetaId)
  
  // Mutation para crear cuenta
  const createCuentaMutation = useCreateCuentaBancaria()

  // Estados para mostrar/ocultar formularios de creación
  const [showNewCuenta, setShowNewCuenta] = useState(false)
  const [selectedCuotas, setSelectedCuotas] = useState<Set<number>>(new Set())

  // Formulario para nueva cuenta
  const newCuentaForm = useForm<z.infer<typeof newCuentaSchema>>({
    resolver: zodResolver(newCuentaSchema),
    defaultValues: { nombre: '', entidadFinanciera: '' },
  })

  // Formulario principal
  const form = useForm<CardPaymentFormValues>({
    resolver: zodResolver(cardPaymentFormSchema),
    defaultValues: {
      tarjeta: '',
      fecha: new Date(),
      cuenta: 'none',
    },
  })

  // Reiniciar formularios cuando se abre el modal
  useEffect(() => {
    if (open) {
      form.reset({
        tarjeta: '',
        fecha: new Date(),
        cuenta: 'none',
      })
      setShowNewCuenta(false)
      setSelectedCuotas(new Set())
      setSelectedTarjetaId(undefined)
      newCuentaForm.reset()
    }
  }, [open, form, newCuentaForm])

  // Manejar cambio de tarjeta
  const handleTarjetaChange = (value: string) => {
    form.setValue('tarjeta', value)
    setSelectedTarjetaId(parseInt(value))
    setSelectedCuotas(new Set())
  }

  // Manejar guardado de nueva cuenta
  const handleSaveNewCuenta = async () => {
    const isValid = await newCuentaForm.trigger()
    if (!isValid) {
      toast.error('Por favor, corrige los errores en el formulario de cuenta.')
      return
    }

    const data = newCuentaForm.getValues()
    try {
      await createCuentaMutation.mutateAsync({
        nombre: data.nombre,
        entidadFinanciera: data.entidadFinanciera,
        idEspacioTrabajo: currentWorkspace!.id,
      })
      
      toast.success('Cuenta bancaria creada exitosamente')
      
      // Esperar un momento para que se actualice el cache
      setTimeout(() => {
        // Seleccionar la última cuenta (la recién creada)
        if (cuentas.length > 0) {
          const ultimaCuenta = cuentas[cuentas.length - 1]
          form.setValue('cuenta', ultimaCuenta.id.toString())
        }
      }, 100)
      
      setShowNewCuenta(false)
      newCuentaForm.reset()
    } catch (error) {
      console.error('Error al crear cuenta:', error)
      toast.error('Error al crear la cuenta bancaria')
    }
  }

  // Manejar selección de cuotas
  const toggleCuota = (cuotaId: number) => {
    setSelectedCuotas(prev => {
      const newSet = new Set(prev)
      if (newSet.has(cuotaId)) {
        newSet.delete(cuotaId)
      } else {
        newSet.add(cuotaId)
      }
      return newSet
    })
  }

  // Calcular total de cuotas seleccionadas
  const total = cuotas
    .filter(c => selectedCuotas.has(c.id))
    .reduce((sum, cuota) => sum + cuota.montoCuota, 0)

  const { refreshDashboard } = useDashboardCache()

  // Manejar envío del formulario principal
  const onSubmit = async (data: CardPaymentFormValues) => {
    if (selectedCuotas.size === 0) {
      toast.error('Debes seleccionar al menos una cuota para pagar.')
      return
    }

    // TODO: Implementar lógica de pago
    toast.info('Función no implementada aún.')
    console.log({
      ...data,
      cuotasSeleccionadas: Array.from(selectedCuotas),
      total,
    })
    
    // Cuando se implemente, agregar:
    // await refreshDashboard()
  }

  // Manejar errores en el envío
  const handleFormError = () => {
    toast.error('Por favor, revisa los campos obligatorios.')
  }

  // Filtrar caracteres no permitidos en nombre de cuenta
  const handleCuentaNombreChange = (value: string) => {
    const filtered = value.replace(/[^a-zA-Z0-9,()_\-/\s]/g, '')
    if (filtered.length <= 50) {
      newCuentaForm.setValue('nombre', filtered)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl">
        <DialogHeader>
          <DialogTitle>Pagar resumen tarjeta</DialogTitle>
          <DialogDescription>
            Selecciona las cuotas pendientes que deseas pagar del resumen de tu tarjeta.
          </DialogDescription>
        </DialogHeader>

        <ScrollArea className="max-h-[65vh] pr-4">
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit, handleFormError)} className="space-y-4">
              {/* Tarjeta */}
              <FormField
                control={form.control}
                name="tarjeta"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Tarjeta de crédito</FormLabel>
                    <Select 
                      onValueChange={handleTarjetaChange} 
                      value={field.value}
                      disabled={loadingTarjetas}
                    >
                      <FormControl>
                        <SelectTrigger className="h-9">
                          <SelectValue placeholder={loadingTarjetas ? "Cargando..." : "Seleccionar tarjeta..."} />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {tarjetas.map((t) => (
                          <SelectItem key={t.id} value={t.id.toString()}>
                            {t.redDePago} - {t.numeroTarjeta} - {t.entidadFinanciera}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {/* Fecha */}
              <FormField
                control={form.control}
                name="fecha"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Fecha de pago</FormLabel>
                    <Popover>
                      <PopoverTrigger asChild>
                        <FormControl>
                          <Button
                            variant="outline"
                            className={cn(
                              'w-full h-9 justify-start text-left font-normal',
                              !field.value && 'text-muted-foreground'
                            )}
                          >
                            <CalendarIcon className="mr-2 h-4 w-4" />
                            {field.value ? format(field.value, 'PPP', { locale: es }) : 'Seleccionar fecha'}
                          </Button>
                        </FormControl>
                      </PopoverTrigger>
                      <PopoverContent className="w-auto p-0" align="start">
                        <Calendar
                          mode="single"
                          selected={field.value}
                          onSelect={field.onChange}
                          disabled={(date) => date > new Date()}
                          initialFocus
                        />
                      </PopoverContent>
                    </Popover>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <Separator />

              {/* Cuenta Bancaria */}
              <FormField
                control={form.control}
                name="cuenta"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      Cuenta bancaria <span className="text-zinc-500 text-xs">(opcional)</span>
                    </FormLabel>
                    <div className="flex gap-2">
                      <Select 
                        onValueChange={field.onChange} 
                        value={field.value}
                        disabled={loadingCuentas}
                      >
                        <FormControl>
                          <SelectTrigger className="flex-1 h-9">
                            <SelectValue placeholder={loadingCuentas ? "Cargando..." : "Sin cuenta bancaria"} />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="none">Sin cuenta bancaria</SelectItem>
                          {cuentas.map((c) => (
                            <SelectItem key={c.id} value={c.id.toString()}>
                              {c.nombre} - {c.entidadFinanciera} (${c.saldoActual.toFixed(2)})
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="h-9 w-9 p-0"
                        onClick={() => setShowNewCuenta(!showNewCuenta)}
                      >
                        <Plus className="h-4 w-4" />
                      </Button>
                    </div>
                    <FormMessage />

                    {showNewCuenta && (
                      <div className="space-y-2 pt-2 border-l-2 border-muted pl-3">
                        <Form {...newCuentaForm}>
                          <FormField
                            control={newCuentaForm.control}
                            name="nombre"
                            render={({ field }) => (
                              <FormItem>
                                <FormControl>
                                  <Input
                                    {...field}
                                    placeholder="Nombre de la cuenta (máx. 50 caracteres)"
                                    className="h-9"
                                    onChange={(e) => handleCuentaNombreChange(e.target.value)}
                                  />
                                </FormControl>
                                <FormMessage />
                              </FormItem>
                            )}
                          />
                          <FormField
                            control={newCuentaForm.control}
                            name="entidadFinanciera"
                            render={({ field }) => (
                              <FormItem>
                                <Select onValueChange={field.onChange} value={field.value}>
                                  <FormControl>
                                    <SelectTrigger className="h-9">
                                      <SelectValue placeholder="Seleccionar entidad" />
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
                        </Form>
                        <div className="flex justify-end gap-2">
                          <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            className="h-8"
                            onClick={() => {
                              setShowNewCuenta(false)
                              newCuentaForm.reset()
                            }}
                          >
                            Cancelar
                          </Button>
                          <Button
                            type="button"
                            size="sm"
                            className="h-8"
                            onClick={handleSaveNewCuenta}
                            disabled={createCuentaMutation.isPending}
                          >
                            {createCuentaMutation.isPending ? 'Guardando...' : 'Guardar'}
                          </Button>
                        </div>
                      </div>
                    )}
                  </FormItem>
                )}
              />

              {/* Data Table Cuotas Pendientes */}
              {selectedTarjetaId && (
                <div className="space-y-2">
                  <h3 className="text-lg font-semibold">Cuotas pendientes</h3>
                  {loadingCuotas ? (
                    <div className="text-center py-8 text-muted-foreground">Cargando cuotas...</div>
                  ) : cuotas.length === 0 ? (
                    <div className="text-center py-8 text-muted-foreground">
                      No hay cuotas pendientes para esta tarjeta.
                    </div>
                  ) : (
                    <>
                      <div className="rounded-md border">
                        <Table>
                          <TableHeader>
                            <TableRow>
                              <TableHead className="w-[50px]"></TableHead>
                              <TableHead>Cuota</TableHead>
                              <TableHead>Vencimiento</TableHead>
                              <TableHead className="text-right">Monto</TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {cuotas.map((cuota) => (
                              <TableRow key={cuota.id}>
                                <TableCell>
                                  <Checkbox
                                    checked={selectedCuotas.has(cuota.id)}
                                    onCheckedChange={() => toggleCuota(cuota.id)}
                                  />
                                </TableCell>
                                <TableCell>{cuota.numeroCuota}</TableCell>
                                <TableCell>
                                  {format(new Date(cuota.fechaVencimiento), 'PPP', { locale: es })}
                                </TableCell>
                                <TableCell className="text-right font-mono font-medium tabular-nums">
                                  ${cuota.montoCuota.toFixed(2)}
                                </TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      </div>

                      {/* Total */}
                      {selectedCuotas.size > 0 && (
                        <div className="flex justify-end items-center gap-2 pt-2 border-t">
                          <span className="text-sm font-medium">
                            Total seleccionado ({selectedCuotas.size} {selectedCuotas.size === 1 ? 'cuota' : 'cuotas'}):
                          </span>
                          <span className="text-lg font-bold">
                            ${total.toFixed(2)}
                          </span>
                        </div>
                      )}
                    </>
                  )}
                </div>
              )}
            </form>
          </Form>
        </ScrollArea>

        <DialogFooter className="mt-4">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
          >
            Cancelar
          </Button>
          <Button 
            type="submit" 
            onClick={form.handleSubmit(onSubmit, handleFormError)}
            disabled={form.formState.isSubmitting || !selectedTarjetaId || selectedCuotas.size === 0}
          >
            Pagar resumen
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
