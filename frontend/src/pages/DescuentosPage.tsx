import { useState, useMemo } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { useAppStore } from '@/store/app-store'
import { useDescuentos, useCreateDescuento, useDeleteDescuento } from '@/features/selectors/api/selector-queries'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { toast } from '@/hooks/useToast'
import { Card } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { Label } from '@/components/ui/label'
import { Slider } from '@/components/ui/slider'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import {
  ResponsiveModal,
  ResponsiveModalContent,
  ResponsiveModalHeader,
  ResponsiveModalTitle,
  ResponsiveModalDescription,
  ResponsiveModalScrollArea,
  ResponsiveModalFooter,
} from '@/components/ui/responsive-modal'
import { DeleteConfirmDialog } from '@/components/DeleteConfirmDialog'
import { MoneyInput } from '@/components/MoneyInput'
import { useIsMobile } from '@/hooks/use-mobile'
import {
  BadgePercent,
  BadgeDollarSign,
  Plus,
  Trash2,
  MapPin,
  Check,
  ChevronsUpDown,
  Repeat,
  Calendar,
  Tag,
} from 'lucide-react'
import { WorkspacePlaceholder } from '@/features/dashboard/WorkspacePlaceholder'
import { cn } from '@/lib/utils'
import type { DescuentoDTOResponse } from '@/types'

// ============================================================
// Constantes
// ============================================================

const DIAS_SEMANA = [
  { label: 'L', value: 'Lunes' },
  { label: 'M', value: 'Martes' },
  { label: 'X', value: 'Miércoles' },
  { label: 'J', value: 'Jueves' },
  { label: 'V', value: 'Viernes' },
  { label: 'S', value: 'Sábado' },
  { label: 'D', value: 'Domingo' },
]

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

const MODOS_PAGO = ['Débito', 'Crédito', 'QR', 'Efectivo']

const CIUDADES_ARGENTINA = [
  'Avellaneda, Santa Fe',
  'Bahía Blanca, Buenos Aires',
  'Ciudad Autónoma de Buenos Aires, CABA',
  'Comodoro Rivadavia, Chubut',
  'Córdoba, Córdoba',
  'Corrientes, Corrientes',
  'Formosa, Formosa',
  'La Plata, Buenos Aires',
  'La Rioja, La Rioja',
  'Mar del Plata, Buenos Aires',
  'Mendoza, Mendoza',
  'Neuquén, Neuquén',
  'Oro Verde, Entre Ríos',
  'Paraná, Entre Ríos',
  'Posadas, Misiones',
  'Rawson, Chubut',
  'Reconquista, Santa Fe',
  'Resistencia, Chaco',
  'Río Cuarto, Córdoba',
  'Río Gallegos, Santa Cruz',
  'Rosario, Santa Fe',
  'Salta, Salta',
  'San Carlos de Bariloche, Río Negro',
  'San Fernando del Valle de Catamarca, Catamarca',
  'San Juan, San Juan',
  'San Luis, San Luis',
  'San Miguel de Tucumán, Tucumán',
  'San Salvador de Jujuy, Jujuy',
  'Santa Fe, Santa Fe',
  'Santa Rosa, La Pampa',
  'Santiago del Estero, Santiago del Estero',
  'Ushuaia, Tierra del Fuego',
  'Viedma, Río Negro',
]

// ============================================================
// Schema de validación
// ============================================================

const descuentoFormSchema = z.object({
  dia: z.string().min(1, { message: 'Seleccioná un día de la semana.' }),
  localidad: z.string().optional(),
  banco: z.string().min(1, { message: 'Seleccioná un banco.' }),
  modo: z.boolean(),
  porcentaje: z.number().min(0).max(100),
  comercio: z
    .string()
    .min(1, { message: 'El comercio es obligatorio.' })
    .max(50, { message: 'Máximo 50 caracteres.' })
    .regex(/^[a-zA-Z0-9,()_\-/\s]*$/, {
      message: 'Solo se permiten letras, números, coma, paréntesis, guiones y barra.',
    }),
  modoPago: z.string().min(1, { message: 'Seleccioná un modo de pago.' }),
  topeReintegro: z.number().nullable().optional(),
  esSemanal: z.boolean(),
  comentario: z
    .string()
    .max(100, { message: 'Máximo 100 caracteres.' })
    .regex(/^[a-zA-Z0-9,()_\-/\s]*$/, {
      message: 'Solo se permiten letras, números, coma, paréntesis, guiones y barra.',
    })
    .optional(),
})

type DescuentoFormValues = z.infer<typeof descuentoFormSchema>

// ============================================================
// Componente Card del Descuento
// ============================================================

interface DiscountCardProps {
  descuento: DescuentoDTOResponse
  onDelete: (id: number) => void
}

function DiscountCard({ descuento, onDelete }: DiscountCardProps) {
  const [confirmOpen, setConfirmOpen] = useState(false)

  return (
    <>
      <motion.div
        layout
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.95 }}
        transition={{ type: 'spring', stiffness: 350, damping: 28 }}
      >
        <Card className="border-border/60 bg-card hover:border-border transition-colors">
          <div className="p-5">
            {/* Header: comercio + papelera */}
            <div className="flex items-start justify-between gap-3 mb-3">
              <p className="text-lg font-bold tracking-tight text-foreground leading-tight">
                {descuento.comercio}
              </p>
              <Button
                variant="ghost"
                size="icon"
                className="h-7 w-7 shrink-0 text-muted-foreground/50 hover:text-destructive hover:bg-destructive/10"
                onClick={() => setConfirmOpen(true)}
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </div>

            {/* Porcentaje */}
            <div className="flex items-baseline gap-2">
              <span className="text-4xl font-extrabold text-primary leading-none">
                {descuento.porcentaje}
              </span>
              <span className="text-xs font-medium text-muted-foreground">
                DE DESCUENTO
              </span>
            </div>

            {/* Badges */}
            <div className="flex flex-wrap gap-2 mt-4">
              <Badge variant="secondary">{descuento.banco}</Badge>
              <Badge variant="secondary">{descuento.modoPago}</Badge>
              {descuento.modo && (
                <Badge className="bg-emerald-500/15 text-emerald-500 border border-emerald-500/20 hover:bg-emerald-500/15">
                  MODO
                </Badge>
              )}
            </div>

            <Separator className="my-4" />

            {/* Detalles */}
            <div className="flex flex-col space-y-2">
              <div className="flex items-center gap-2.5 text-sm text-muted-foreground">
                {descuento.esSemanal
                  ? <Repeat className="w-4 h-4 shrink-0" />
                  : <Calendar className="w-4 h-4 shrink-0" />}
                <span>{descuento.esSemanal ? 'Semanal' : 'Una vez al mes'}</span>
              </div>

              {descuento.topeReintegro && (
                <div className="flex items-center gap-2.5 text-sm text-muted-foreground">
                  <BadgeDollarSign className="w-4 h-4 shrink-0" />
                  <span>Tope: ${Number(descuento.topeReintegro).toLocaleString('es-AR')}</span>
                </div>
              )}

              {descuento.localidad && (
                <div className="flex items-center gap-2.5 text-sm text-muted-foreground">
                  <MapPin className="w-4 h-4 shrink-0" />
                  <span className="truncate">{descuento.localidad}</span>
                </div>
              )}

              {descuento.comentario && (
                <p className="text-xs text-muted-foreground/70 italic line-clamp-2 pt-1">
                  {descuento.comentario}
                </p>
              )}
            </div>
          </div>
        </Card>
      </motion.div>

      <DeleteConfirmDialog
        open={confirmOpen}
        onOpenChange={setConfirmOpen}
        onConfirm={() => onDelete(descuento.id)}
        title="Eliminar descuento"
        description={`¿Estás seguro de que querés eliminar el descuento de ${descuento.comercio} (${descuento.porcentaje})? Esta acción no se puede deshacer.`}
      />
    </>
  )
}

// ============================================================
// Modal de Agregar Descuento
// ============================================================

interface AddDiscountModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  espacioTrabajoId: string
}

function AddDiscountModal({ open, onOpenChange, espacioTrabajoId }: AddDiscountModalProps) {
  const createDescuentoMutation = useCreateDescuento()
  const [localidadPopoverOpen, setLocalidadPopoverOpen] = useState(false)

  const form = useForm<DescuentoFormValues>({
    resolver: zodResolver(descuentoFormSchema),
    defaultValues: {
      dia: '',
      localidad: '',
      banco: '',
      modo: false,
      porcentaje: 30,
      comercio: '',
      modoPago: '',
      topeReintegro: null,
      esSemanal: true,
      comentario: '',
    },
  })

  const porcentajeValue = form.watch('porcentaje')

  const onSubmit = async (values: DescuentoFormValues) => {
    try {
      await createDescuentoMutation.mutateAsync({
        dia: values.dia,
        localidad: values.localidad || undefined,
        banco: values.banco,
        modo: values.modo,
        porcentaje: `${values.porcentaje}%`,
        comercio: values.comercio,
        modoPago: values.modoPago,
        topeReintegro: values.topeReintegro != null ? String(Math.round(values.topeReintegro)) : undefined,
        esSemanal: values.esSemanal,
        comentario: values.comentario || undefined,
        idEspacioTrabajo: espacioTrabajoId,
      })
      toast.success('Descuento agregado', {
        description: `El descuento de ${values.comercio} fue guardado correctamente.`,
      })
      form.reset()
      onOpenChange(false)
    } catch (error: unknown) {
      console.error('Error al crear descuento:', error)
      toast.error('Error al guardar', {
        description: 'Ocurrió un error al guardar el descuento. Intentá nuevamente.',
      })
    }
  }

  const handleOpenChange = (newOpen: boolean) => {
    if (!newOpen) form.reset()
    onOpenChange(newOpen)
  }

  return (
    <ResponsiveModal open={open} onOpenChange={handleOpenChange}>
      <ResponsiveModalContent className="max-w-lg">
        <ResponsiveModalHeader>
          <ResponsiveModalTitle>Agregar descuento</ResponsiveModalTitle>
          <ResponsiveModalDescription>
            Registrá un descuento disponible con tu banco o tarjeta.
          </ResponsiveModalDescription>
        </ResponsiveModalHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="flex flex-col flex-1 min-h-0">
            <ResponsiveModalScrollArea>
              <div className="space-y-5 py-1">

                {/* Día de la semana */}
                <FormField
                  control={form.control}
                  name="dia"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Día de la semana</FormLabel>
                      <FormControl>
                        <ToggleGroup
                          type="single"
                          value={field.value}
                          onValueChange={(val) => {
                            if (val) field.onChange(val)
                          }}
                          className="flex flex-wrap gap-1.5 justify-start"
                        >
                          {DIAS_SEMANA.map((dia) => (
                            <ToggleGroupItem
                              key={dia.value}
                              value={dia.value}
                              aria-label={dia.value}
                              className="h-9 w-9 rounded-full data-[state=on]:bg-primary data-[state=on]:text-primary-foreground font-semibold"
                            >
                              {dia.label}
                            </ToggleGroupItem>
                          ))}
                        </ToggleGroup>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                {/* Comercio */}
                <FormField
                  control={form.control}
                  name="comercio"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Comercio</FormLabel>
                      <FormControl>
                        <Input
                          {...field}
                          placeholder="Ej: Carrefour, Disco, YPF..."
                          maxLength={50}
                          onChange={(e) => {
                            const filtered = e.target.value.replace(/[^a-zA-Z0-9,()_\-/\s]/g, '')
                            field.onChange(filtered)
                          }}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                {/* Banco */}
                <FormField
                  control={form.control}
                  name="banco"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Banco</FormLabel>
                      <Select onValueChange={field.onChange} value={field.value}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="Seleccioná un banco" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {ENTIDADES_FINANCIERAS.map((banco) => (
                            <SelectItem key={banco} value={banco}>
                              {banco}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                {/* Modo de pago */}
                <FormField
                  control={form.control}
                  name="modoPago"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Modo de pago</FormLabel>
                      <Select onValueChange={field.onChange} value={field.value}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="Seleccioná el modo de pago" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {MODOS_PAGO.map((modo) => (
                            <SelectItem key={modo} value={modo}>
                              {modo}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                {/* Porcentaje de descuento */}
                <FormField
                  control={form.control}
                  name="porcentaje"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Porcentaje de descuento</FormLabel>
                      <div className="flex items-center gap-4">
                        <FormControl>
                          <Slider
                            min={0}
                            max={100}
                            step={5}
                            value={[field.value]}
                            onValueChange={([val]) => field.onChange(val)}
                            className="flex-1"
                          />
                        </FormControl>
                        <span className="text-2xl font-extrabold text-primary w-16 text-center shrink-0">
                          {porcentajeValue}%
                        </span>
                      </div>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                {/* Tope de reintegro */}
                <FormField
                  control={form.control}
                  name="topeReintegro"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Tope de reintegro (opcional)</FormLabel>
                      <FormControl>
                        <MoneyInput
                          value={field.value ?? null}
                          onChange={field.onChange}
                          placeholder="0"
                          maxDecimals={0}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                {/* Localidad */}
                <FormField
                  control={form.control}
                  name="localidad"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Localidad (opcional)</FormLabel>
                      <Popover open={localidadPopoverOpen} onOpenChange={setLocalidadPopoverOpen}>
                        <PopoverTrigger asChild>
                          <FormControl>
                            <Button
                              variant="outline"
                              role="combobox"
                              className={cn(
                                'w-full justify-between font-normal',
                                !field.value && 'text-muted-foreground'
                              )}
                            >
                              {field.value || 'Buscar localidad...'}
                              <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                          </FormControl>
                        </PopoverTrigger>
                        <PopoverContent className="w-full p-0" align="start">
                          <Command>
                            <CommandInput placeholder="Buscar ciudad..." />
                            <CommandList>
                              <CommandEmpty>No se encontró la ciudad.</CommandEmpty>
                              <CommandGroup>
                                {CIUDADES_ARGENTINA.map((ciudad) => (
                                  <CommandItem
                                    key={ciudad}
                                    value={ciudad}
                                    onSelect={(val) => {
                                      field.onChange(val === field.value ? '' : val)
                                      setLocalidadPopoverOpen(false)
                                    }}
                                  >
                                    <Check
                                      className={cn(
                                        'mr-2 h-4 w-4',
                                        field.value === ciudad ? 'opacity-100' : 'opacity-0'
                                      )}
                                    />
                                    {ciudad}
                                  </CommandItem>
                                ))}
                              </CommandGroup>
                            </CommandList>
                          </Command>
                        </PopoverContent>
                      </Popover>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                {/* Switches */}
                <div className="grid grid-cols-2 gap-4">
                  <Controller
                    control={form.control}
                    name="modo"
                    render={({ field }) => (
                      <div className="flex flex-col gap-2 rounded-lg border border-border/60 p-3">
                        <Label htmlFor="switch-modo" className="text-sm font-medium cursor-pointer">
                          App MODO
                        </Label>
                        <Switch
                          id="switch-modo"
                          checked={field.value}
                          onCheckedChange={field.onChange}
                        />
                      </div>
                    )}
                  />

                  <Controller
                    control={form.control}
                    name="esSemanal"
                    render={({ field }) => (
                      <div className="flex flex-col gap-2 rounded-lg border border-border/60 p-3">
                        <Label htmlFor="switch-semanal" className="text-sm font-medium cursor-pointer">
                          Repetición semanal
                        </Label>
                        <Switch
                          id="switch-semanal"
                          checked={field.value}
                          onCheckedChange={field.onChange}
                        />
                      </div>
                    )}
                  />
                </div>

                {/* Comentario */}
                <FormField
                  control={form.control}
                  name="comentario"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Comentario (opcional)</FormLabel>
                      <FormControl>
                        <Textarea
                          {...field}
                          placeholder="Ej: Solo en góndola de lácteos..."
                          maxLength={100}
                          rows={2}
                          className="resize-none"
                          onChange={(e) => {
                            const filtered = e.target.value.replace(/[^a-zA-Z0-9,()_\-/\s]/g, '')
                            field.onChange(filtered)
                          }}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
            </ResponsiveModalScrollArea>

            <ResponsiveModalFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => handleOpenChange(false)}
                disabled={createDescuentoMutation.isPending}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={createDescuentoMutation.isPending}>
                {createDescuentoMutation.isPending ? 'Registrando...' : 'Registrar'}
              </Button>
            </ResponsiveModalFooter>
          </form>
        </Form>
      </ResponsiveModalContent>
    </ResponsiveModal>
  )
}

// ============================================================
// Página Principal
// ============================================================

export function DescuentosPage() {
  const espacioActual = useAppStore((state) => state.currentWorkspace)
  const { data: descuentos = [], isLoading } = useDescuentos(espacioActual?.id)
  const deleteDescuentoMutation = useDeleteDescuento()
  const isMobile = useIsMobile()
  const [addModalOpen, setAddModalOpen] = useState(false)

  const handleDelete = async (id: number) => {
    try {
      await deleteDescuentoMutation.mutateAsync(id)
      toast.success('Descuento eliminado', {
        description: 'El descuento fue eliminado correctamente.',
      })
    } catch (error) {
      console.error('Error al eliminar descuento:', error)
      toast.error('Error al eliminar', {
        description: 'Ocurrió un error. Intentá nuevamente.',
      })
    }
  }

  // Agrupar descuentos por día
  const descuentosPorDia = useMemo(() => {
    const mapa: Record<string, DescuentoDTOResponse[]> = {}
    DIAS_SEMANA.forEach(({ value }) => {
      mapa[value] = []
    })
    descuentos.forEach((d) => {
      if (mapa[d.dia]) {
        mapa[d.dia].push(d)
      }
    })
    return mapa
  }, [descuentos])

  if (!espacioActual) {
    return (
      <WorkspacePlaceholder
        icon={<BadgePercent className="h-16 w-16 text-muted-foreground/50" />}
        description="Para gestionar tus descuentos, primero elegí un espacio de trabajo en el menú lateral o crea uno nuevo."
      />
    )
  }

  return (
    <div className="space-y-4 sm:space-y-6 pt-4 sm:pt-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl sm:text-3xl font-bold tracking-tight">
            Descuentos
          </h2>
          <p className="text-sm sm:text-base text-muted-foreground">
            Gestiona tus descuentos bancarios organizados por día de la semana
          </p>
        </div>
        <Button onClick={() => setAddModalOpen(true)} className="shrink-0">
          <Plus className="mr-2 h-4 w-4" />
          Agregar descuento
        </Button>
      </div>

      {/* Contenido */}
      {isLoading ? (
        <div className="flex gap-4 overflow-x-auto pb-4">
          {Array.from({ length: 7 }).map((_, i) => (
            <div key={i} className="min-w-[280px] w-[280px] shrink-0 space-y-3">
              <div className="h-4 w-24 rounded bg-muted animate-pulse" />
              <div className="h-48 rounded-xl bg-muted animate-pulse" />
            </div>
          ))}
        </div>
      ) : isMobile ? (
        // ---- MOBILE: Tabs por día ----
        <Tabs defaultValue="Lunes" className="w-full">
          <div className="overflow-x-auto">
            <TabsList className="w-full grid grid-cols-7 h-auto">
              {DIAS_SEMANA.map(({ label, value }) => (
                <TabsTrigger key={value} value={value} className="text-xs px-1 py-1.5">
                  {label}
                  {descuentosPorDia[value].length > 0 && (
                    <span className="ml-1 text-[10px] text-primary font-bold">
                      {descuentosPorDia[value].length}
                    </span>
                  )}
                </TabsTrigger>
              ))}
            </TabsList>
          </div>

          {DIAS_SEMANA.map(({ value }) => (
            <TabsContent key={value} value={value} className="mt-4">
              <AnimatePresence mode="sync">
                {descuentosPorDia[value].length > 0 ? (
                  <div className="space-y-3">
                    {descuentosPorDia[value].map((d) => (
                      <DiscountCard key={d.id} descuento={d} onDelete={handleDelete} />
                    ))}
                  </div>
                ) : (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="flex flex-col items-center justify-center p-6 border-2 border-dashed border-border/50 rounded-xl bg-muted/10 min-h-[150px] gap-2 text-center"
                  >
                    <Tag className="w-8 h-8 text-muted-foreground/50" />
                    <p className="text-sm text-muted-foreground/60">Sin descuentos</p>
                  </motion.div>
                )}
              </AnimatePresence>
            </TabsContent>
          ))}
        </Tabs>
      ) : (
        // ---- DESKTOP: Scroll horizontal, columna fija por día ----
        <div className="flex gap-4 overflow-x-auto pb-4">
          {DIAS_SEMANA.map(({ value: dia }) => (
            <div key={dia} className="min-w-[280px] w-[280px] shrink-0 flex flex-col gap-3">
              {/* Header de columna */}
              <div className="flex items-center gap-2 px-1">
                <p className="text-sm font-semibold uppercase tracking-wider text-muted-foreground">
                  {dia}
                </p>
                {descuentosPorDia[dia].length > 0 && (
                  <span className="inline-flex items-center justify-center rounded-full bg-primary/15 text-primary text-xs font-bold w-5 h-5 shrink-0">
                    {descuentosPorDia[dia].length}
                  </span>
                )}
              </div>

              {/* Cards del día */}
              <AnimatePresence mode="sync">
                {descuentosPorDia[dia].length > 0 ? (
                  descuentosPorDia[dia].map((d) => (
                    <DiscountCard key={d.id} descuento={d} onDelete={handleDelete} />
                  ))
                ) : (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="flex flex-col items-center justify-center p-6 border-2 border-dashed border-border/50 rounded-xl bg-muted/10 min-h-[150px] gap-2"
                  >
                    <Tag className="w-8 h-8 text-muted-foreground/50" />
                    <p className="text-xs text-muted-foreground/60">Sin descuentos</p>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          ))}
        </div>
      )}

      {/* Modal de agregar */}
      <AddDiscountModal
        open={addModalOpen}
        onOpenChange={setAddModalOpen}
        espacioTrabajoId={espacioActual.id}
      />
    </div>
  )
}
