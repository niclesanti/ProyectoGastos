"use client"

import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { useAppStore } from '@/store/app-store'
import { useAuth } from '@/contexts/AuthContext'
import { 
  useMotivos, 
  useContactos, 
  useCuentasBancarias,
  useCreateTransaccion,
  useCreateMotivo,
  useCreateContacto,
  useCreateCuentaBancaria
} from '@/features/selectors/api/selector-queries'
import { useDashboardCache } from '@/hooks'
import { useQueryClient } from '@tanstack/react-query'
import { format as formatDate } from 'date-fns'
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
import {
  Tabs,
  TabsList,
  TabsTrigger,
} from '@/components/ui/tabs'
import { Calendar } from '@/components/ui/calendar'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Separator } from '@/components/ui/separator'
import { CalendarIcon, Plus } from 'lucide-react'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import { cn } from '@/lib/utils'
import { toast } from 'sonner'

// Validación Zod para el formulario principal
const transactionFormSchema = z.object({
  tipo: z.string().min(1, { message: "Por favor, selecciona un tipo de transacción." }),
  fecha: z.date({ 
    message: "Debes seleccionar una fecha válida."
  }).refine((date) => date <= new Date(), {
    message: "La fecha debe ser en el pasado o presente.",
  }),
  monto: z.string()
    .min(1, { message: "Por favor, indica el monto de la operación." })
    .refine((val) => {
      const num = parseFloat(val)
      return !isNaN(num) && num > 0
    }, { message: "El monto debe ser mayor a 0." })
    .refine((val) => {
      const parts = val.split('.')
      if (parts.length === 1) return parts[0].length <= 11
      return parts[0].length <= 11 && parts[1].length <= 2
    }, { message: "Máximo 11 dígitos enteros y 2 decimales." }),
  motivo: z.string().min(1, { message: "Debes asignar un motivo a la transacción." }),
  cuenta: z.string().optional(),
  contacto: z.string().optional(),
  descripcion: z.string()
    .max(100, { message: "La descripción no puede exceder los 100 caracteres." })
    .regex(/^[a-zA-Z0-9,()_\-/\s]*$/, { 
      message: "Solo se permiten letras, números, coma, paréntesis, guiones y barra." 
    })
    .optional()
    .or(z.literal('')),
})

// Validación para nuevo motivo
const newMotivoSchema = z.object({
  nombre: z.string()
    .min(1, { message: "El nombre del motivo es obligatorio." })
    .max(50, { message: "Máximo 50 caracteres." })
    .regex(/^[a-zA-Z0-9,()_\-/\s]*$/, { 
      message: "Solo se permiten letras, números, coma, paréntesis, guiones y barra." 
    }),
})

// Validación para nueva cuenta bancaria
const newCuentaSchema = z.object({
  nombre: z.string()
    .min(1, { message: "El nombre de la cuenta es obligatorio." })
    .max(50, { message: "Máximo 50 caracteres." })
    .regex(/^[a-zA-Z0-9,()_\-/\s]*$/, { 
      message: "Solo se permiten letras, números, coma, paréntesis, guiones y barra." 
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
  entidad: z.string().min(1, { message: "Debes seleccionar una entidad financiera." }),
})

// Validación para nuevo contacto
const newContactoSchema = z.object({
  nombre: z.string()
    .min(1, { message: "El nombre del contacto es obligatorio." })
    .max(50, { message: "Máximo 50 caracteres." })
    .regex(/^[a-zA-Z0-9,()_\-/\s]*$/, { 
      message: "Solo se permiten letras, números, coma, paréntesis, guiones y barra." 
    }),
})

type TransactionFormValues = z.infer<typeof transactionFormSchema>

// Lista de entidades financieras
const ENTIDADES_FINANCIERAS = [
  "Banco Credicoop",
  "Banco de Santa Fe",
  "Banco Macro",
  "Banco Patagonia",
  "Banco Santander",
  "BBVA",
  "BNA",
  "Brubank",
  "Galicia",
  "HSBC",
  "ICBC",
  "Lemon Cash",
  "Mercado Pago",
  "Naranja X",
  "Personal Pay",
  "Ualá",
]

interface TransactionModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function TransactionModal({ open, onOpenChange }: TransactionModalProps) {
  const currentWorkspace = useAppStore((state) => state.currentWorkspace)
  const { user } = useAuth()
  const queryClient = useQueryClient()
  
  // Cargar datos con TanStack Query
  const { data: motivos = [], isLoading: loadingMotivos } = useMotivos(currentWorkspace?.id)
  const { data: contactos = [], isLoading: loadingContactos } = useContactos(currentWorkspace?.id)
  const { data: cuentas = [], isLoading: loadingCuentas } = useCuentasBancarias(currentWorkspace?.id)
  
  // Mutations
  const createTransaccionMutation = useCreateTransaccion()
  const createMotivoMutation = useCreateMotivo()
  const createContactoMutation = useCreateContacto()
  const createCuentaMutation = useCreateCuentaBancaria()
  
  // Estados para mostrar/ocultar formularios de creación
  const [showNewMotivo, setShowNewMotivo] = useState(false)
  const [showNewCuenta, setShowNewCuenta] = useState(false)
  const [showNewContacto, setShowNewContacto] = useState(false)
  
  // Estados para los nuevos valores
  const [newMotivo, setNewMotivo] = useState('')
  const [newMotivoError, setNewMotivoError] = useState('')
  const [newCuentaNombre, setNewCuentaNombre] = useState('')
  const [newCuentaSaldo, setNewCuentaSaldo] = useState('')
  const [newCuentaEntidad, setNewCuentaEntidad] = useState('')
  const [newCuentaError, setNewCuentaError] = useState('')
  const [newContacto, setNewContacto] = useState('')
  const [newContactoError, setNewContactoError] = useState('')

  // Inicializar el formulario con React Hook Form y Zod
  const form = useForm<TransactionFormValues>({
    resolver: zodResolver(transactionFormSchema),
    defaultValues: {
      tipo: 'gasto', // Por defecto Gasto
      fecha: new Date(), // Fecha actual por defecto
      monto: '',
      motivo: '',
      cuenta: 'none',
      contacto: 'none',
      descripcion: '',
    },
  })

  // Reiniciar formulario cuando se abre el modal
  useEffect(() => {
    if (open) {
      form.reset({
        tipo: 'gasto', // Por defecto Gasto
        fecha: new Date(),
        monto: '',
        motivo: '',
        cuenta: 'none',
        contacto: 'none',
        descripcion: '',
      })
      setShowNewMotivo(false)
      setShowNewCuenta(false)
      setShowNewContacto(false)
      setNewMotivo('')
      setNewCuentaNombre('')
      setNewCuentaSaldo('')
      setNewCuentaEntidad('')
      setNewContacto('')
      setNewMotivoError('')
      setNewCuentaError('')
      setNewContactoError('')
    }
  }, [open, form])

  const { refreshDashboard } = useDashboardCache()

  // Manejar el envío del formulario
  const onSubmit = async (data: TransactionFormValues) => {
    if (!currentWorkspace || !user) {
      toast.error('Error: No hay usuario o espacio de trabajo activo')
      return
    }

    try {
      const transaccionData = {
        tipo: data.tipo.toUpperCase(), // Convertir a GASTO o INGRESO
        fecha: formatDate(data.fecha, 'yyyy-MM-dd'),
        monto: parseFloat(data.monto),
        descripcion: data.descripcion || undefined,
        nombreCompletoAuditoria: user.nombre,
        idEspacioTrabajo: currentWorkspace.id,
        idMotivo: parseInt(data.motivo),
        idContacto: data.contacto && data.contacto !== 'none' ? parseInt(data.contacto) : undefined,
        idCuentaBancaria: data.cuenta && data.cuenta !== 'none' ? parseInt(data.cuenta) : undefined,
      }

      await createTransaccionMutation.mutateAsync(transaccionData)
      
      // Actualizar el caché del dashboard
      await refreshDashboard()
      
      // Invalidar caché de workspaces para actualizar el saldo en la sidebar
      queryClient.invalidateQueries({ queryKey: ['workspaces'] })
      
      toast.success('Transacción registrada exitosamente')
      onOpenChange(false)
    } catch (error: any) {
      console.error('Error al registrar transacción:', error)
      toast.error(error?.message || 'Error al registrar la transacción')
    }
  }

  // Manejar errores en el envío
  const handleFormError = () => {
    toast.error('Por favor, revisa los campos obligatorios.')
  }

  // Validar y guardar nuevo motivo
  const handleSaveNewMotivo = async () => {
    if (!currentWorkspace) {
      toast.error('Error: No hay espacio de trabajo activo')
      return
    }

    const result = newMotivoSchema.safeParse({ nombre: newMotivo })
    if (!result.success) {
      setNewMotivoError(result.error.issues[0].message)
      return
    }

    try {
      const nuevoMotivo = await createMotivoMutation.mutateAsync({
        motivo: newMotivo,
        idEspacioTrabajo: currentWorkspace.id
      })
      
      toast.success('Motivo guardado correctamente')
      
      // Seleccionar automáticamente el motivo recién creado
      form.setValue('motivo', nuevoMotivo.id.toString())
      
      setShowNewMotivo(false)
      setNewMotivo('')
      setNewMotivoError('')
    } catch (error: any) {
      console.error('Error al guardar motivo:', error)
      toast.error(error?.message || 'Error al guardar el motivo')
    }
  }

  // Validar y guardar nueva cuenta
  const handleSaveNewCuenta = async () => {
    if (!currentWorkspace) {
      toast.error('Error: No hay espacio de trabajo activo')
      return
    }

    const result = newCuentaSchema.safeParse({ 
      nombre: newCuentaNombre,
      saldoActual: newCuentaSaldo || '0',
      entidad: newCuentaEntidad 
    })
    if (!result.success) {
      setNewCuentaError(result.error.issues[0].message)
      return
    }

    const saldoValue = newCuentaSaldo && newCuentaSaldo.trim() !== '' 
      ? parseFloat(newCuentaSaldo) 
      : 0

    console.log('Datos de cuenta a enviar:', {
      nombre: newCuentaNombre,
      entidadFinanciera: newCuentaEntidad,
      saldoActual: saldoValue,
      saldoOriginal: newCuentaSaldo,
      idEspacioTrabajo: currentWorkspace.id
    })

    try {
      await createCuentaMutation.mutateAsync({
        nombre: newCuentaNombre,
        entidadFinanciera: newCuentaEntidad,
        saldoActual: saldoValue,
        idEspacioTrabajo: currentWorkspace.id
      })
      
      toast.success('Cuenta guardada correctamente')
      setShowNewCuenta(false)
      setNewCuentaNombre('')
      setNewCuentaSaldo('')
      setNewCuentaEntidad('')
      setNewCuentaError('')
    } catch (error: any) {
      console.error('Error al guardar cuenta:', error)
      toast.error(error?.message || 'Error al guardar la cuenta')
    }
  }

  // Validar y guardar nuevo contacto
  const handleSaveNewContacto = async () => {
    if (!currentWorkspace) {
      toast.error('Error: No hay espacio de trabajo activo')
      return
    }

    const result = newContactoSchema.safeParse({ nombre: newContacto })
    if (!result.success) {
      setNewContactoError(result.error.issues[0].message)
      return
    }

    try {
      const nuevoContacto = await createContactoMutation.mutateAsync({
        nombre: newContacto,
        idEspacioTrabajo: currentWorkspace.id
      })
      
      toast.success('Contacto guardado correctamente')
      
      // Seleccionar automáticamente el contacto recién creado
      form.setValue('contacto', nuevoContacto.id.toString())
      
      setShowNewContacto(false)
      setNewContacto('')
      setNewContactoError('')
    } catch (error: any) {
      console.error('Error al guardar contacto:', error)
      toast.error(error?.message || 'Error al guardar el contacto')
    }
  }

  // Restringir entrada de monto
  const handleMontoChange = (value: string) => {
    // Permitir solo números, un punto y máximo 2 decimales
    const regex = /^\d{0,11}(\.\d{0,2})?$/
    if (regex.test(value) || value === '') {
      form.setValue('monto', value)
    }
  }

  // Restringir entrada de saldo de cuenta
  const handleSaldoCuentaChange = (value: string) => {
    const regex = /^\d{0,11}(\.\d{0,2})?$/
    if (regex.test(value) || value === '') {
      setNewCuentaSaldo(value)
      setNewCuentaError('')
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto p-4 sm:p-6">
        <DialogHeader className="space-y-2">
          <DialogTitle className="text-lg sm:text-xl">Registrar una transacción</DialogTitle>
          <DialogDescription className="text-sm">
            Anotar una nueva salida o ingreso de dinero con todos sus detalles.
          </DialogDescription>
        </DialogHeader>

        <ScrollArea className="max-h-[55vh] sm:max-h-[60vh] pr-4 sm:pr-6">
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit, handleFormError)} className="space-y-3 sm:space-y-4">
              {/* Tipo */}
              <FormField
                control={form.control}
                name="tipo"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Tipo de transacción</FormLabel>
                    <FormControl>
                      <Tabs 
                        value={field.value} 
                        onValueChange={field.onChange}
                        className="w-full"
                      >
                        <TabsList className="grid w-full grid-cols-2 h-9">
                          <TabsTrigger value="gasto" className="text-sm">
                            Gasto
                          </TabsTrigger>
                          <TabsTrigger value="ingreso" className="text-sm">
                            Ingreso
                          </TabsTrigger>
                        </TabsList>
                      </Tabs>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {/* Fecha */}
              <FormField
                control={form.control}
                name="fecha"
                render={({ field }) => (
                  <FormItem className="flex flex-col">
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
                            {field.value ? (
                              format(field.value, 'PPP', { locale: es })
                            ) : (
                              'Elegir fecha de transacción...'
                            )}
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

              {/* Monto */}
              <FormField
                control={form.control}
                name="monto"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Monto</FormLabel>
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
                      <div className="space-y-2 pt-2">
                        <Input
                          placeholder="Nombre del nuevo motivo"
                          className="h-9"
                          value={newMotivo}
                          maxLength={50}
                          onChange={(e) => {
                            // Filtrar caracteres no permitidos
                            const filtered = e.target.value.replace(/[^a-zA-Z0-9,()_\-/\s]/g, '')
                            setNewMotivo(filtered)
                            setNewMotivoError('')
                          }}
                        />
                        {newMotivoError && (
                          <p className="text-sm font-medium text-destructive">{newMotivoError}</p>
                        )}
                        <div className="flex justify-end gap-2">
                          <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            className="h-8"
                            onClick={() => {
                              setShowNewMotivo(false)
                              setNewMotivo('')
                              setNewMotivoError('')
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

              {/* Separador entre campos obligatorios y opcionales */}
              <Separator className="my-6" />

              {/* Cuenta Bancaria */}
              <FormField
                control={form.control}
                name="cuenta"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      Cuenta bancaria <span className="text-zinc-500 text-xs font-normal">(opcional)</span>
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
                              {c.nombre} - {c.entidadFinanciera}
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
                      <div className="space-y-2 pt-2">
                        <Input
                          placeholder="Nombre de la cuenta"
                          className="h-9"
                          value={newCuentaNombre}
                          maxLength={50}
                          onChange={(e) => {
                            // Filtrar caracteres no permitidos
                            const filtered = e.target.value.replace(/[^a-zA-Z0-9,()_\-/\s]/g, '')
                            setNewCuentaNombre(filtered)
                            setNewCuentaError('')
                          }}
                        />
                        <div className="relative">
                          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                            $
                          </span>
                          <Input
                            type="text"
                            inputMode="decimal"
                            placeholder="0.00"
                            className="h-9 pl-7"
                            value={newCuentaSaldo}
                            onChange={(e) => handleSaldoCuentaChange(e.target.value)}
                          />
                        </div>
                        <Select value={newCuentaEntidad} onValueChange={(val) => {
                          setNewCuentaEntidad(val)
                          setNewCuentaError('')
                        }}>
                          <SelectTrigger className="h-9">
                            <SelectValue placeholder="Seleccionar entidad" />
                          </SelectTrigger>
                          <SelectContent>
                            {ENTIDADES_FINANCIERAS.map((entidad) => (
                              <SelectItem key={entidad} value={entidad}>
                                {entidad}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                        {newCuentaError && (
                          <p className="text-sm font-medium text-destructive">{newCuentaError}</p>
                        )}
                        <div className="flex justify-end gap-2">
                          <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            className="h-8"
                            onClick={() => {
                              setShowNewCuenta(false)
                              setNewCuentaNombre('')
                              setNewCuentaSaldo('')
                              setNewCuentaEntidad('')
                              setNewCuentaError('')
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

              {/* Contacto */}
              <FormField
                control={form.control}
                name="contacto"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      {form.watch('tipo') === 'gasto' 
                        ? 'Comercio / Destinatario' 
                        : 'Contacto origen / Emisor'
                      } <span className="text-zinc-500 text-xs font-normal">(opcional)</span>
                    </FormLabel>
                    <div className="flex gap-2">
                      <Select 
                        onValueChange={field.onChange} 
                        value={field.value} 
                        disabled={loadingContactos}
                      >
                        <FormControl>
                          <SelectTrigger className="flex-1 h-9">
                            <SelectValue placeholder={loadingContactos ? "Cargando..." : "Sin contacto"} />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="none">Sin contacto</SelectItem>
                          {contactos.map((c) => (
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
                        onClick={() => setShowNewContacto(!showNewContacto)}
                      >
                        <Plus className="h-4 w-4" />
                      </Button>
                    </div>
                    <FormMessage />
                    
                    {showNewContacto && (
                      <div className="space-y-2 pt-2">
                        <Input
                          placeholder="Ingrese el nuevo contacto"
                          className="h-9"
                          value={newContacto}
                          maxLength={50}
                          onChange={(e) => {
                            // Filtrar caracteres no permitidos
                            const filtered = e.target.value.replace(/[^a-zA-Z0-9,()_\-/\s]/g, '')
                            setNewContacto(filtered)
                            setNewContactoError('')
                          }}
                        />
                        {newContactoError && (
                          <p className="text-sm font-medium text-destructive">{newContactoError}</p>
                        )}
                        <div className="flex justify-end gap-2">
                          <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            className="h-8"
                            onClick={() => {
                              setShowNewContacto(false)
                              setNewContacto('')
                              setNewContactoError('')
                            }}
                          >
                            Cancelar
                          </Button>
                          <Button
                            type="button"
                            size="sm"
                            className="h-8"
                            onClick={handleSaveNewContacto}
                            disabled={createContactoMutation.isPending}
                          >
                            {createContactoMutation.isPending ? 'Guardando...' : 'Guardar'}
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
                      Descripción <span className="text-zinc-500 text-xs font-normal">(opcional)</span>
                    </FormLabel>
                    <FormControl>
                      <Textarea
                        {...field}
                        placeholder="Agregar notas adicionales sobre esta transacción..."
                        rows={3}
                        maxLength={100}
                        className="resize-none"
                        onChange={(e) => {
                          // Filtrar caracteres no permitidos
                          const filtered = e.target.value.replace(/[^a-zA-Z0-9,()_\-/\s]/g, '')
                          field.onChange(filtered)
                        }}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </form>
          </Form>
        </ScrollArea>

        <DialogFooter className="mt-3 sm:mt-4 flex-col sm:flex-row gap-2">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            className="w-full sm:w-auto"
          >
            Cancelar
          </Button>
          <Button 
            type="submit" 
            onClick={form.handleSubmit(onSubmit, handleFormError)}
            disabled={form.formState.isSubmitting || createTransaccionMutation.isPending}
            className="w-full sm:w-auto"
          >
            {createTransaccionMutation.isPending ? 'Guardando...' : 'Guardar transacción'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
