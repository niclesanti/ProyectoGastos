"use client"

import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { useAppStore } from '@/store/app-store'
import { useAuth } from '@/contexts/AuthContext'
import { 
  useMotivos, 
  useContactos, 
  useTarjetas,
  useCreateMotivo,
  useCreateContacto,
  useCreateCompraCredito
} from '@/features/selectors/api/selector-queries'
import { useDashboardCache } from '@/hooks'
import {
  ResponsiveModal,
  ResponsiveModalContent,
  ResponsiveModalDescription,
  ResponsiveModalHeader,
  ResponsiveModalTitle,
  ResponsiveModalFooter,
  ResponsiveModalScrollArea,
} from '@/components/ui/responsive-modal'
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
import { Textarea } from '@/components/ui/textarea'
import { Separator } from '@/components/ui/separator'
import { CalendarIcon, Plus } from 'lucide-react'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import { cn } from '@/lib/utils'
import { toast } from '@/hooks/useToast'
import { MoneyInput } from '@/components/MoneyInput'

// Esquema de validación para nuevo motivo
const newMotivoSchema = z.object({
  motivo: z.string()
    .min(1, { message: "El nombre del motivo es obligatorio." })
    .max(50, { message: "El motivo no puede exceder los 50 caracteres." })
    .regex(/^[a-zA-Z0-9,()_\-/\s]*$/, { 
      message: "Solo se permiten letras, números, comas, paréntesis, guiones y barras." 
    }),
})

// Esquema de validación para nuevo comercio
const newComercioSchema = z.object({
  nombre: z.string()
    .min(1, { message: "El nombre del comercio es obligatorio." })
    .max(50, { message: "El comercio no puede exceder los 50 caracteres." })
    .regex(/^[a-zA-Z0-9,()_\-/\s]*$/, { 
      message: "Solo se permiten letras, números, comas, paréntesis, guiones y barras." 
    }),
})

// Esquema de validación principal
const creditPurchaseFormSchema = z.object({
  fecha: z.date().refine((date) => date <= new Date(), {
    message: "La fecha no puede ser futura.",
  }),
  tarjeta: z.string().min(1, { message: "Por favor, selecciona una tarjeta de crédito." }),
  cuotas: z.string().min(1, { message: "Por favor, selecciona la cantidad de cuotas." }),
  monto: z.number()
    .positive("El monto debe ser mayor a 0")
    .refine((val) => {
      const str = val.toString()
      const parts = str.split('.')
      if (parts.length === 1) return parts[0].length <= 12
      return parts[0].length <= 12 && (parts[1]?.length || 0) <= 2
    }, { message: "Máximo 12 dígitos enteros y 2 decimales." }),
  motivo: z.string().min(1, { message: "Debes asignar un motivo a la compra." }),
  comercio: z.string().optional(),
  descripcion: z.string()
    .max(100, { message: "La descripción no puede exceder los 100 caracteres." })
    .regex(/^[a-zA-Z0-9,()_\-/\s]*$/, { 
      message: "Solo se permiten letras, números, comas, paréntesis, guiones y barras." 
    })
    .optional()
    .or(z.literal('')),
})

type CreditPurchaseFormValues = z.infer<typeof creditPurchaseFormSchema>

interface CreditPurchaseModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function CreditPurchaseModal({ open, onOpenChange }: CreditPurchaseModalProps) {
  const currentWorkspace = useAppStore((state) => state.currentWorkspace)
  const { user } = useAuth()
  
  // Cargar datos con TanStack Query
  const { data: motivos = [], isLoading: loadingMotivos } = useMotivos(currentWorkspace?.id)
  const { data: comercios = [], isLoading: loadingComercios } = useContactos(currentWorkspace?.id)
  const { data: tarjetas = [], isLoading: loadingTarjetas } = useTarjetas(currentWorkspace?.id)
  
  // Mutations
  const createMotivoMutation = useCreateMotivo()
  const createComercioMutation = useCreateContacto()
  const createCompraMutation = useCreateCompraCredito()

  // Estados para mostrar/ocultar formularios de creación
  const [showNewMotivo, setShowNewMotivo] = useState(false)
  const [showNewComercio, setShowNewComercio] = useState(false)

  // Formularios para subfunciones
  const newMotivoForm = useForm<z.infer<typeof newMotivoSchema>>({
    resolver: zodResolver(newMotivoSchema),
    defaultValues: { motivo: '' },
  })

  const newComercioForm = useForm<z.infer<typeof newComercioSchema>>({
    resolver: zodResolver(newComercioSchema),
    defaultValues: { nombre: '' },
  })

  // Formulario principal
  const form = useForm<CreditPurchaseFormValues>({
    resolver: zodResolver(creditPurchaseFormSchema),
    defaultValues: {
      fecha: new Date(),
      tarjeta: '',
      cuotas: '',
      monto: null as unknown as number,
      motivo: '',
      comercio: 'none',
      descripcion: '',
    },
  })

  // Reiniciar formularios cuando se abre el modal
  useEffect(() => {
    if (open) {
      form.reset({
        fecha: new Date(),
        tarjeta: '',
        cuotas: '',
        monto: null as unknown as number,
        motivo: '',
        comercio: 'none',
        descripcion: '',
      })
      setShowNewMotivo(false)
      setShowNewComercio(false)
      newMotivoForm.reset()
      newComercioForm.reset()
    }
  }, [open, form, newMotivoForm, newComercioForm])

  // Manejar guardado de nuevo motivo
  const handleSaveNewMotivo = async () => {
    const isValid = await newMotivoForm.trigger()
    if (!isValid) {
      toast.error('Por favor, corrige los errores en el formulario de motivo.')
      return
    }

    const data = newMotivoForm.getValues()
    try {
      const nuevoMotivo = await createMotivoMutation.mutateAsync({
        motivo: data.motivo,
        idEspacioTrabajo: currentWorkspace!.id,
      })
      
      toast.success('Motivo creado exitosamente')
      form.setValue('motivo', nuevoMotivo.id.toString())
      setShowNewMotivo(false)
      newMotivoForm.reset()
    } catch (error: any) {
      console.error('Error al crear motivo:', error)
      toast.error(error?.message || 'Error al crear el motivo')
    }
  }

  // Manejar guardado de nuevo comercio
  const handleSaveNewComercio = async () => {
    const isValid = await newComercioForm.trigger()
    if (!isValid) {
      toast.error('Por favor, corrige los errores en el formulario de comercio.')
      return
    }

    const data = newComercioForm.getValues()
    try {
      const nuevoComercio = await createComercioMutation.mutateAsync({
        nombre: data.nombre,
        idEspacioTrabajo: currentWorkspace!.id,
      })
      
      toast.success('Comercio creado exitosamente')
      form.setValue('comercio', nuevoComercio.id.toString())
      setShowNewComercio(false)
      newComercioForm.reset()
    } catch (error: any) {
      console.error('Error al crear comercio:', error)
      toast.error(error?.message || 'Error al crear el comercio')
    }
  }

  const { refreshDashboard } = useDashboardCache()

  // Manejar envío del formulario principal
  const onSubmit = async (data: CreditPurchaseFormValues) => {
    if (!currentWorkspace || !user) return

    try {
      const compraData = {
        fechaCompra: format(data.fecha, 'yyyy-MM-dd'),
        montoTotal: data.monto,
        cantidadCuotas: parseInt(data.cuotas),
        descripcion: data.descripcion && data.descripcion.trim() !== '' ? data.descripcion : undefined,
        nombreCompletoAuditoria: user.nombre,
        espacioTrabajoId: currentWorkspace.id,
        motivoId: parseInt(data.motivo),
        comercioId: data.comercio && data.comercio !== 'none' ? parseInt(data.comercio) : undefined,
        tarjetaId: parseInt(data.tarjeta),
      }

      await createCompraMutation.mutateAsync(compraData)
      
      // Actualizar todo el dashboard incluyendo stats
      await refreshDashboard()
      
      toast.success('Compra con crédito registrada exitosamente')
      onOpenChange(false)
    } catch (error: any) {
      console.error('Error al registrar compra:', error)
      toast.error(error?.message || 'Error al registrar la compra')
    }
  }

  // Manejar errores en el envío
  const handleFormError = () => {
    toast.error('Por favor, revisa los campos obligatorios.')
  }

  // MoneyInput maneja la validación de formato automáticamente

  // Filtrar caracteres no permitidos en motivo
  const handleMotivoInputChange = (value: string) => {
    const filtered = value.replace(/[^a-zA-Z0-9,()_\-/\s]/g, '')
    if (filtered.length <= 50) {
      newMotivoForm.setValue('motivo', filtered)
    }
  }

  // Filtrar caracteres no permitidos en comercio
  const handleComercioInputChange = (value: string) => {
    const filtered = value.replace(/[^a-zA-Z0-9,()_\-/\s]/g, '')
    if (filtered.length <= 50) {
      newComercioForm.setValue('nombre', filtered)
    }
  }

  // Filtrar caracteres no permitidos en descripción
  const handleDescripcionChange = (value: string) => {
    const filtered = value.replace(/[^a-zA-Z0-9,()_\-/\s]/g, '')
    if (filtered.length <= 100) {
      form.setValue('descripcion', filtered)
    }
  }

  // Helper para formatear la fecha de manera compacta
  const formatDateLabel = (date: Date | undefined): string => {
    if (!date) return 'Hoy'
    
    const today = new Date()
    const isToday = date.toDateString() === today.toDateString()
    
    if (isToday) return 'Hoy'
    
    // Formato corto: "21 Feb" o "15 Ene"
    return format(date, 'd MMM', { locale: es })
  }

  return (
    <ResponsiveModal open={open} onOpenChange={onOpenChange}>
      <ResponsiveModalContent className="max-w-lg">
        <ResponsiveModalHeader className="space-y-2">
          <ResponsiveModalTitle className="text-lg sm:text-xl">Registrar compra con crédito</ResponsiveModalTitle>
          <ResponsiveModalDescription className="text-sm">
            Anotar que hiciste una compra con determinada tarjeta de crédito.
          </ResponsiveModalDescription>
        </ResponsiveModalHeader>

        <ResponsiveModalScrollArea>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit, handleFormError)} className="space-y-3 sm:space-y-4">
              {/* Metadata: hint del monto y fecha */}
              <div className="flex items-center justify-between">
                <p className="text-xs text-muted-foreground">
                  Monto total a pagar (con intereses)
                </p>
                <FormField
                  control={form.control}
                  name="fecha"
                  render={({ field }) => (
                    <FormItem className="flex items-center gap-1.5">
                      <Popover>
                        <PopoverTrigger asChild>
                          <FormControl>
                            <Button
                              variant="ghost"
                              size="sm"
                              className={cn(
                                'h-7 px-2 text-xs font-normal text-muted-foreground hover:text-foreground transition-colors',
                                !field.value && 'text-muted-foreground'
                              )}
                            >
                              <CalendarIcon className="mr-1.5 h-3 w-3" />
                              {formatDateLabel(field.value)}
                            </Button>
                          </FormControl>
                        </PopoverTrigger>
                        <PopoverContent className="w-auto p-0" align="end">
                          <Calendar
                            mode="single"
                            selected={field.value}
                            onSelect={field.onChange}
                            disabled={(date) => date > new Date()}
                            initialFocus
                            locale={es}
                          />
                        </PopoverContent>
                      </Popover>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              {/* Monto - Premium Design */}
              <FormField
                control={form.control}
                name="monto"
                render={({ field }) => (
                  <FormItem>
                    <FormControl>
                      <div className="relative flex items-center justify-center py-1">
                        {/* Símbolo de moneda fijo a la izquierda */}
                        <span className="text-3xl sm:text-4xl font-medium transition-colors mr-1 text-muted-foreground/60">
                          $
                        </span>
                        
                        {/* Input sin bordes */}
                        <div className="flex-1 max-w-md">
                          <MoneyInput
                            value={field.value}
                            onChange={field.onChange}
                            min={0}
                            maxDigits={12}
                            maxDecimals={2}
                            placeholder="0.00"
                            showPrefix={false}
                            className={cn(
                              "border-none bg-transparent text-left shadow-none",
                              "text-3xl sm:text-4xl font-semibold",
                              "h-auto py-2 px-0",
                              "focus-visible:ring-0 focus-visible:ring-offset-0",
                              "transition-colors duration-200",
                              "text-foreground placeholder:text-muted-foreground/30"
                            )}
                          />
                        </div>
                      </div>
                    </FormControl>
                    <FormMessage className="text-center" />
                  </FormItem>
                )}
              />

              {/* Tarjeta */}
              <FormField
                control={form.control}
                name="tarjeta"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Tarjeta</FormLabel>
                    <Select 
                      onValueChange={field.onChange} 
                      value={field.value}
                      disabled={loadingTarjetas}
                    >
                      <FormControl>
                        <SelectTrigger className="h-9">
                          <SelectValue placeholder={loadingTarjetas ? "Cargando..." : "Seleccionar tarjeta de crédito..."} />
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

              {/* Cantidad de cuotas */}
              <FormField
                control={form.control}
                name="cuotas"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Cuotas</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger className="h-9">
                          <SelectValue placeholder="Elegir cantidad de cuotas..." />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="1">1 cuota</SelectItem>
                        <SelectItem value="3">3 cuotas</SelectItem>
                        <SelectItem value="6">6 cuotas</SelectItem>
                        <SelectItem value="9">9 cuotas</SelectItem>
                        <SelectItem value="12">12 cuotas</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />



              {/* Motivo */}
              <FormField
                control={form.control}
                name="motivo"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Motivo / Categoría</FormLabel>
                    <div className="flex gap-2">
                      <Select 
                        onValueChange={field.onChange} 
                        value={field.value}
                        disabled={loadingMotivos}
                      >
                        <FormControl>
                          <SelectTrigger className="flex-1 h-9">
                            <SelectValue placeholder={loadingMotivos ? "Cargando..." : "Seleccionar motivo..."} />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {motivos.length === 0 ? (
                            <SelectItem value="empty" disabled>
                              No hay motivos disponibles - Crear uno nuevo
                            </SelectItem>
                          ) : (
                            motivos.map((m) => (
                              <SelectItem key={m.id} value={m.id.toString()}>
                                {m.motivo}
                              </SelectItem>
                            ))
                          )}
                        </SelectContent>
                      </Select>
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="h-9 w-9 p-0"
                        onClick={() => setShowNewMotivo(!showNewMotivo)}
                      >
                        <Plus className="h-4 w-4" />
                      </Button>
                    </div>
                    <FormMessage />

                    {showNewMotivo && (
                      <div className="space-y-2 pt-2 border-l-2 border-muted pl-3">
                        <Form {...newMotivoForm}>
                          <FormField
                            control={newMotivoForm.control}
                            name="motivo"
                            render={({ field }) => (
                              <FormItem>
                                <FormControl>
                                  <Input
                                    {...field}
                                    placeholder="Nombre del nuevo motivo (máx. 50 caracteres)"
                                    className="h-9"
                                    onChange={(e) => handleMotivoInputChange(e.target.value)}
                                  />
                                </FormControl>
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
                              setShowNewMotivo(false)
                              newMotivoForm.reset()
                            }}
                          >
                            Cancelar
                          </Button>
                          <Button
                            type="button"
                            size="sm"
                            className="h-8"
                            onClick={handleSaveNewMotivo}
                            disabled={createMotivoMutation.isPending}
                          >
                            {createMotivoMutation.isPending ? 'Guardando...' : 'Guardar'}
                          </Button>
                        </div>
                      </div>
                    )}
                  </FormItem>
                )}
              />

              <Separator />

              {/* Comercio */}
              <FormField
                control={form.control}
                name="comercio"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      Comercio <span className="text-zinc-500 text-xs">(opcional)</span>
                    </FormLabel>
                    <div className="flex gap-2">
                      <Select 
                        onValueChange={field.onChange} 
                        value={field.value}
                        disabled={loadingComercios}
                      >
                        <FormControl>
                          <SelectTrigger className="flex-1 h-9">
                            <SelectValue placeholder={loadingComercios ? "Cargando..." : "Sin comercio"} />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="none">Ninguno</SelectItem>
                          {comercios.map((c) => (
                            <SelectItem key={c.id} value={c.id.toString()}>
                              {c.nombre}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="h-9 w-9 p-0"
                        onClick={() => setShowNewComercio(!showNewComercio)}
                      >
                        <Plus className="h-4 w-4" />
                      </Button>
                    </div>
                    <FormMessage />

                    {showNewComercio && (
                      <div className="space-y-2 pt-2 border-l-2 border-muted pl-3">
                        <Form {...newComercioForm}>
                          <FormField
                            control={newComercioForm.control}
                            name="nombre"
                            render={({ field }) => (
                              <FormItem>
                                <FormControl>
                                  <Input
                                    {...field}
                                    placeholder="Nombre del nuevo comercio (máx. 50 caracteres)"
                                    className="h-9"
                                    onChange={(e) => handleComercioInputChange(e.target.value)}
                                  />
                                </FormControl>
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
                              setShowNewComercio(false)
                              newComercioForm.reset()
                            }}
                          >
                            Cancelar
                          </Button>
                          <Button
                            type="button"
                            size="sm"
                            className="h-8"
                            onClick={handleSaveNewComercio}
                            disabled={createComercioMutation.isPending}
                          >
                            {createComercioMutation.isPending ? 'Guardando...' : 'Guardar'}
                          </Button>
                        </div>
                      </div>
                    )}
                  </FormItem>
                )}
              />

              {/* Descripción */}
              <FormField
                control={form.control}
                name="descripcion"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      Descripción <span className="text-zinc-500 text-xs">(opcional)</span>
                    </FormLabel>
                    <FormControl>
                      <Textarea
                        {...field}
                        placeholder="Agregar notas adicionales sobre esta compra (máx. 100 caracteres)..."
                        rows={3}
                        className="resize-none"
                        onChange={(e) => handleDescripcionChange(e.target.value)}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </form>
          </Form>
        </ResponsiveModalScrollArea>

        <ResponsiveModalFooter className="mt-3 sm:mt-4 flex-row gap-2">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            className="flex-1"
          >
            Cancelar
          </Button>
          <Button 
            type="button"
            onClick={form.handleSubmit(onSubmit, handleFormError)}
            disabled={form.formState.isSubmitting || createCompraMutation.isPending}
            className="flex-1"
          >
            {createCompraMutation.isPending ? 'Guardando...' : 'Registrar'}
          </Button>
        </ResponsiveModalFooter>
      </ResponsiveModalContent>
    </ResponsiveModal>
  )
}
