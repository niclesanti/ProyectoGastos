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
import { Textarea } from '@/components/ui/textarea'
import { Separator } from '@/components/ui/separator'
import { ScrollArea } from '@/components/ui/scroll-area'
import { CalendarIcon, Plus } from 'lucide-react'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import { cn } from '@/lib/utils'
import { toast } from 'sonner'

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
  monto: z.string()
    .min(1, { message: "Por favor, indica el monto de la compra." })
    .refine((val) => {
      const num = parseFloat(val)
      return !isNaN(num) && num > 0
    }, { message: "El monto debe ser mayor a 0." })
    .refine((val) => {
      const parts = val.split('.')
      if (parts.length === 1) return parts[0].length <= 8
      return parts[0].length <= 8 && parts[1].length <= 2
    }, { message: "Máximo 8 dígitos enteros y 2 decimales." }),
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
      monto: '',
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
        monto: '',
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
        montoTotal: parseFloat(data.monto),
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

  // Restringir entrada de monto
  const handleMontoChange = (value: string) => {
    const regex = /^\d{0,8}(\.\d{0,2})?$/
    if (regex.test(value) || value === '') {
      form.setValue('monto', value)
    }
  }

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

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto p-4 sm:p-6">
        <DialogHeader className="space-y-2">
          <DialogTitle className="text-lg sm:text-xl">Compra con crédito</DialogTitle>
          <DialogDescription className="text-sm">
            Anotar que hiciste una compra con tarjeta de crédito.
          </DialogDescription>
        </DialogHeader>

        <ScrollArea className="max-h-[55vh] sm:max-h-[60vh] pr-4 sm:pr-6">
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit, handleFormError)} className="space-y-3 sm:space-y-4">
              {/* Fecha */}
              <FormField
                control={form.control}
                name="fecha"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Fecha</FormLabel>
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
                            {field.value ? format(field.value, 'PPP', { locale: es }) : 'Elegir fecha de compra...'}
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
                    <FormLabel>Cantidad de cuotas</FormLabel>
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

              {/* Monto */}
              <FormField
                control={form.control}
                name="monto"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Monto final a pagar</FormLabel>
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
                          onChange={(e) => handleMontoChange(e.target.value)}
                        />
                      </div>
                    </FormControl>
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
                    <FormLabel>Motivo</FormLabel>
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
            disabled={form.formState.isSubmitting || createCompraMutation.isPending}
          >
            {createCompraMutation.isPending ? 'Guardando...' : 'Guardar compra'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
