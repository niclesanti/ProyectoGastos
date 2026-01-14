"use client"

import React, { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { useAppStore } from '@/store/app-store'
import { useAuth } from '@/contexts/AuthContext'
import { useDashboardCache } from '@/hooks'
import { 
  useTarjetas, 
  useCuentasBancarias, 
  useResumenesTarjeta,
  useCreateCuentaBancaria,
  usePagarResumenTarjeta
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
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import { ChevronDown, ChevronUp } from 'lucide-react'
import { Badge } from '@/components/ui/badge'

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
  saldoActual: z.string()
    .refine((val) => {
      if (!val || val.trim() === '') return true
      // Validar que solo contenga números y punto decimal
      const validFormat = /^\d+(\.\d{1,2})?$/.test(val)
      if (!validFormat) return false
      const num = parseFloat(val)
      return !isNaN(num) && num >= 0
    }, { message: "El saldo debe ser un número válido (0 o mayor)." })
    .refine((val) => {
      if (!val || val.trim() === '') return true
      const parts = val.split('.')
      if (parts.length === 1) return parts[0].length <= 11
      return parts[0].length <= 11 && parts[1].length <= 2
    }, { message: "Máximo 11 dígitos enteros y 2 decimales." }),
  entidadFinanciera: z.string().min(1, { message: "Debes seleccionar una entidad financiera." }),
})

// Esquema de validación principal
const cardPaymentFormSchema = z.object({
  tarjeta: z.string().min(1, { message: "Por favor, selecciona una tarjeta de crédito." }),
  resumen: z.string().min(1, { message: "Debes seleccionar un resumen pendiente." }),
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
  const { user } = useAuth()
  const { refreshDashboard } = useDashboardCache()
  
  // Cargar datos con TanStack Query
  const { data: tarjetas = [], isLoading: loadingTarjetas } = useTarjetas(currentWorkspace?.id)
  const { data: cuentas = [], isLoading: loadingCuentas } = useCuentasBancarias(currentWorkspace?.id)
  
  // Estados para resúmenes y tarjeta seleccionada
  const [selectedTarjetaId, setSelectedTarjetaId] = useState<number | undefined>()
  const { data: resumenes = [], isLoading: loadingResumenes } = useResumenesTarjeta(selectedTarjetaId)
  
  // Mutations
  const createCuentaMutation = useCreateCuentaBancaria()
  const pagarResumenMutation = usePagarResumenTarjeta()

  // Estados para mostrar/ocultar formularios de creación
  const [showNewCuenta, setShowNewCuenta] = useState(false)
  const [expandedResumen, setExpandedResumen] = useState<number | null>(null)

  // Formulario para nueva cuenta
  const newCuentaForm = useForm<z.infer<typeof newCuentaSchema>>({
    resolver: zodResolver(newCuentaSchema),
    defaultValues: { nombre: '', saldoActual: '', entidadFinanciera: '' },
  })

  // Formulario principal
  const form = useForm<CardPaymentFormValues>({
    resolver: zodResolver(cardPaymentFormSchema),
    defaultValues: {
      tarjeta: '',
      resumen: '',
      fecha: new Date(),
      cuenta: 'none',
    },
  })

  // Reiniciar formularios cuando se abre el modal
  useEffect(() => {
    if (open) {
      form.reset({
        tarjeta: '',
        resumen: '',
        fecha: new Date(),
        cuenta: 'none',
      })
      setShowNewCuenta(false)
      setExpandedResumen(null)
      setSelectedTarjetaId(undefined)
      newCuentaForm.reset()
    }
  }, [open, form, newCuentaForm])

  // Manejar cambio de tarjeta
  const handleTarjetaChange = (value: string) => {
    form.setValue('tarjeta', value)
    form.setValue('resumen', '') // Resetear resumen seleccionado
    setSelectedTarjetaId(parseInt(value))
    setExpandedResumen(null)
  }

  // Manejar guardado de nueva cuenta
  const handleSaveNewCuenta = async () => {
    const isValid = await newCuentaForm.trigger()
    if (!isValid) {
      toast.error('Por favor, corrige los errores en el formulario de cuenta.')
      return
    }

    const data = newCuentaForm.getValues()
    const saldoValue = data.saldoActual && data.saldoActual.trim() !== '' 
      ? parseFloat(data.saldoActual) 
      : 0
    
    console.log('Datos de cuenta a enviar:', {
      nombre: data.nombre,
      entidadFinanciera: data.entidadFinanciera,
      saldoActual: saldoValue,
      saldoOriginal: data.saldoActual,
      idEspacioTrabajo: currentWorkspace!.id,
    })

    try {
      await createCuentaMutation.mutateAsync({
        nombre: data.nombre,
        entidadFinanciera: data.entidadFinanciera,
        saldoActual: saldoValue,
        idEspacioTrabajo: currentWorkspace!.id,
      })
      
      toast.success('Cuenta bancaria creada exitosamente')
      
      setShowNewCuenta(false)
      newCuentaForm.reset()
    } catch (error) {
      console.error('Error al crear cuenta:', error)
      toast.error('Error al crear la cuenta bancaria')
    }
  }

  // Obtener resumen seleccionado
  const resumenSeleccionado = resumenes.find(r => r.id.toString() === form.watch('resumen'))

  // Obtener cuenta seleccionada
  const cuentaId = form.watch('cuenta')
  const cuentaSeleccionada = cuentaId && cuentaId !== 'none' ? cuentas.find(c => c.id.toString() === cuentaId) : null

  // const { refreshDashboard } = useDashboardCache() // Para usar cuando se implemente el pago

  // Función para obtener el estilo del badge según el estado
  const getEstadoBadge = (estado: string, fechaVencimiento: string) => {
    const hoy = new Date()
    const vencimiento = new Date(fechaVencimiento)
    
    if (estado === 'PAGADO') {
      return { variant: 'default' as const, text: 'Pagado' }
    }
    if (estado === 'VENCIDO' || vencimiento < hoy) {
      return { variant: 'destructive' as const, text: 'Vencido' }
    }
    return { variant: 'secondary' as const, text: 'Pendiente' }
  }

  // Manejar envío del formulario principal
  const onSubmit = async (data: CardPaymentFormValues) => {
    if (!currentWorkspace || !user) {
      toast.error('Error: No hay usuario o espacio de trabajo activo')
      return
    }

    if (!resumenSeleccionado) {
      toast.error('Debes seleccionar un resumen para pagar.')
      return
    }

    // Validar saldo de cuenta si se especificó
    if (cuentaSeleccionada) {
      if (cuentaSeleccionada.saldoActual < resumenSeleccionado.montoTotal) {
        toast.error(
          `Saldo insuficiente. Disponible: $${cuentaSeleccionada.saldoActual.toFixed(2)}, necesitas: $${resumenSeleccionado.montoTotal.toFixed(2)}`
        )
        return
      }
    }

    try {
      const pagoRequest = {
        idResumen: resumenSeleccionado.id,
        fecha: format(data.fecha, 'yyyy-MM-dd'),
        monto: resumenSeleccionado.montoTotal,
        nombreCompletoAuditoria: user.nombre,
        idEspacioTrabajo: currentWorkspace.id,
        idCuentaBancaria: data.cuenta && data.cuenta !== 'none' ? parseInt(data.cuenta) : undefined,
      }

      await pagarResumenMutation.mutateAsync(pagoRequest)
      
      // Actualizar el caché del dashboard
      await refreshDashboard()
      
      toast.success(
        `Resumen pagado exitosamente. Total: $${resumenSeleccionado.montoTotal.toFixed(2)}`
      )
      onOpenChange(false)
    } catch (error: any) {
      console.error('Error al pagar resumen:', error)
      
      // Manejar errores específicos del backend
      if (error?.response?.data?.message) {
        toast.error(error.response.data.message)
      } else if (error?.response?.status === 400) {
        toast.error('Datos inválidos. Verifica la información ingresada.')
      } else if (error?.response?.status === 404) {
        toast.error('Resumen no encontrado.')
      } else {
        toast.error('Error al procesar el pago. Intenta nuevamente.')
      }
    }
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

  // Restringir entrada de saldo de cuenta
  const handleSaldoCuentaChange = (value: string) => {
    const regex = /^\d{0,11}(\.\d{0,2})?$/
    if (regex.test(value) || value === '') {
      newCuentaForm.setValue('saldoActual', value)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl lg:max-w-4xl max-h-[90vh] overflow-y-auto p-4 sm:p-6">
        <DialogHeader className="space-y-2">
          <DialogTitle className="text-lg sm:text-xl">Pagar resumen tarjeta</DialogTitle>
          <DialogDescription className="text-sm">
            Selecciona el resumen pendiente a pagar. Puedes ver detalles de qué cuotas incluye el resumen.
          </DialogDescription>
        </DialogHeader>

        <ScrollArea className="max-h-[60vh] sm:max-h-[65vh] pr-4 sm:pr-6">
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit, handleFormError)} className="space-y-3 sm:space-y-4">
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
                        {tarjetas.length === 0 ? (
                          <SelectItem value="empty" disabled>
                            No hay tarjetas disponibles - Crear una nueva
                          </SelectItem>
                        ) : (
                          tarjetas.map((t) => (
                            <SelectItem key={t.id} value={t.id.toString()}>
                              {t.redDePago} - {t.numeroTarjeta} - {t.entidadFinanciera}
                            </SelectItem>
                          ))
                        )}
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
                            name="saldoActual"
                            render={({ field }) => (
                              <FormItem>
                                <FormControl>
                                  <div className="relative">
                                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                                      $
                                    </span>
                                    <Input
                                      {...field}
                                      type="text"
                                      inputMode="decimal"
                                      placeholder="0.00"
                                      className="h-9 pl-7"
                                      onChange={(e) => handleSaldoCuentaChange(e.target.value)}
                                    />
                                  </div>
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

              {/* Data Table Resúmenes Pendientes */}
              {selectedTarjetaId && (
                <div className="space-y-2">
                  <h3 className="text-lg font-semibold">Resúmenes pendientes</h3>
                  {loadingResumenes ? (
                    <div className="text-center py-8 text-muted-foreground">Cargando resúmenes...</div>
                  ) : resumenes.length === 0 ? (
                    <div className="text-center py-8 text-muted-foreground">
                      ¡Felicidades! No hay resúmenes pendientes para esta tarjeta.
                    </div>
                  ) : (
                    <>
                      <FormField
                        control={form.control}
                        name="resumen"
                        render={({ field }) => (
                          <FormItem>
                            <FormControl>
                              <RadioGroup
                                onValueChange={field.onChange}
                                value={field.value}
                                className="space-y-0"
                              >
                                <div className="rounded-md border">
                                  <Table>
                                    <TableHeader>
                                      <TableRow>
                                        <TableHead className="w-[50px]"></TableHead>
                                        <TableHead>Período</TableHead>
                                        <TableHead>Vencimiento</TableHead>
                                        <TableHead className="text-right">Monto</TableHead>
                                        <TableHead>Estado</TableHead>
                                        <TableHead className="w-[50px]"></TableHead>
                                      </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                      {resumenes.map((resumen) => {
                                        const estadoBadge = getEstadoBadge(resumen.estado, resumen.fechaVencimiento)
                                        const isExpanded = expandedResumen === resumen.id
                                        const isSelected = field.value === resumen.id.toString()
                                        
                                        return (
                                          <React.Fragment key={resumen.id}>
                                            <TableRow 
                                              className={cn(
                                                "cursor-pointer transition-colors",
                                                isSelected && "bg-muted/50"
                                              )}
                                            >
                                              <TableCell>
                                                <RadioGroupItem value={resumen.id.toString()} />
                                              </TableCell>
                                              <TableCell>
                                                {format(new Date(resumen.anio, resumen.mes - 1), 'MMMM yyyy', { locale: es })}
                                              </TableCell>
                                              <TableCell>
                                                {format(new Date(resumen.fechaVencimiento), 'PPP', { locale: es })}
                                              </TableCell>
                                              <TableCell className="text-right font-mono font-medium tabular-nums">
                                                ${resumen.montoTotal.toFixed(2)}
                                              </TableCell>
                                              <TableCell>
                                                <Badge variant={estadoBadge.variant}>
                                                  {estadoBadge.text}
                                                </Badge>
                                              </TableCell>
                                              <TableCell>
                                                <Button
                                                  type="button"
                                                  variant="ghost"
                                                  size="sm"
                                                  className="h-8 w-8 p-0"
                                                  onClick={() => setExpandedResumen(isExpanded ? null : resumen.id)}
                                                >
                                                  {isExpanded ? (
                                                    <ChevronUp className="h-4 w-4" />
                                                  ) : (
                                                    <ChevronDown className="h-4 w-4" />
                                                  )}
                                                </Button>
                                              </TableCell>
                                            </TableRow>
                                            {isExpanded && (
                                              <TableRow>
                                                <TableCell colSpan={6} className="bg-muted/50 p-0">
                                                  <div className="p-4 space-y-2">
                                                    <p className="text-sm font-medium">Detalle de cuotas incluidas:</p>
                                                    {resumen.cuotas && resumen.cuotas.length > 0 ? (
                                                      <div className="space-y-1">
                                                        {resumen.cuotas.map((cuota) => (
                                                          <div key={cuota.id} className="flex justify-between text-sm">
                                                            <span className="text-muted-foreground">
                                                              {cuota.descripcion} - Cuota {cuota.numeroCuota}/{cuota.totalCuotas} - {cuota.motivo}
                                                            </span>
                                                            <span className="font-mono font-medium tabular-nums">
                                                              ${cuota.montoCuota.toFixed(2)}
                                                            </span>
                                                          </div>
                                                        ))}
                                                      </div>
                                                    ) : (
                                                      <p className="text-sm text-muted-foreground">
                                                        No hay cuotas disponibles para este resumen.
                                                      </p>
                                                    )}
                                                  </div>
                                                </TableCell>
                                              </TableRow>
                                            )}
                                          </React.Fragment>
                                        )
                                      })}
                                    </TableBody>
                                  </Table>
                                </div>
                              </RadioGroup>
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />

                      {/* Total a pagar */}
                      {resumenSeleccionado && (
                        <div className="bg-muted/50 rounded-lg p-4 border mt-4">
                          <div className="flex justify-between items-center">
                            <div>
                              <p className="text-sm text-muted-foreground">Total a pagar</p>
                              <p className="text-xs text-muted-foreground">
                                Resumen {format(new Date(resumenSeleccionado.anio, resumenSeleccionado.mes - 1), 'MMMM yyyy', { locale: es })}
                              </p>
                            </div>
                            <p className="text-3xl font-bold tabular-nums">
                              ${resumenSeleccionado.montoTotal.toFixed(2)}
                            </p>
                          </div>
                          {cuentaSeleccionada && (
                            <p className="text-xs text-muted-foreground mt-2">
                              Saldo restante en cuenta: ${(cuentaSeleccionada.saldoActual - resumenSeleccionado.montoTotal).toFixed(2)}
                            </p>
                          )}
                        </div>
                      )}
                    </>
                  )}
                </div>
              )}
            </form>
          </Form>
        </ScrollArea>

        <DialogFooter className="mt-4 gap-3">
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
            disabled={pagarResumenMutation.isPending || !selectedTarjetaId || !resumenSeleccionado}
          >
            {pagarResumenMutation.isPending ? 'Procesando pago...' : 'Pagar resumen'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
