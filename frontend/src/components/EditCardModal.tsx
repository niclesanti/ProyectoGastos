import { useState, useEffect } from 'react'
import { useTarjetas, useUpdateTarjeta } from '@/features/selectors/api/selector-queries'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
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
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Edit } from 'lucide-react'

const editCardFormSchema = z.object({
  tarjeta: z.string().min(1, { message: 'Por favor, selecciona una tarjeta.' }),
  diaCierre: z.string().min(1, { message: 'El día de cierre es obligatorio.' }),
  diaVencimientoPago: z.string().min(1, { message: 'El día de vencimiento es obligatorio.' }),
})

type EditCardFormValues = z.infer<typeof editCardFormSchema>

interface EditCardModalProps {
  espacioTrabajoId: string
}

export function EditCardModal({ espacioTrabajoId }: EditCardModalProps) {
  const [open, setOpen] = useState(false)
  const { data: tarjetas = [], isLoading } = useTarjetas(espacioTrabajoId)
  const updateTarjetaMutation = useUpdateTarjeta()

  const form = useForm<EditCardFormValues>({
    resolver: zodResolver(editCardFormSchema),
    mode: 'onSubmit',
    defaultValues: {
      tarjeta: '',
      diaCierre: '',
      diaVencimientoPago: '',
    },
  })

  // Resetear formulario cuando el modal se abre o cierra
  useEffect(() => {
    if (open) {
      form.reset({
        tarjeta: '',
        diaCierre: '',
        diaVencimientoPago: '',
      })
      form.clearErrors()
    }
  }, [open, form])

  // Actualizar días cuando se selecciona una tarjeta
  const handleTarjetaChange = (value: string) => {
    form.setValue('tarjeta', value)
    const tarjeta = tarjetas.find(t => t.id.toString() === value)
    if (tarjeta) {
      form.setValue('diaCierre', tarjeta.diaCierre.toString())
      form.setValue('diaVencimientoPago', tarjeta.diaVencimientoPago.toString())
    }
  }

  const onSubmit = async (values: EditCardFormValues) => {
    try {
      await updateTarjetaMutation.mutateAsync({
        id: parseInt(values.tarjeta),
        diaCierre: parseInt(values.diaCierre),
        diaVencimientoPago: parseInt(values.diaVencimientoPago),
      })
      
      toast.success('Tarjeta modificada', {
        description: 'Los días de cierre y vencimiento se han actualizado correctamente.',
      })
      
      setOpen(false)
    } catch (error: any) {
      console.error('Error al modificar tarjeta:', error)
      toast.error('Error al modificar tarjeta', {
        description: error?.message || 'Intenta nuevamente o contacta al soporte.',
      })
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline">
          <Edit className="mr-2 h-4 w-4" />
          Modificar tarjeta
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[500px] max-h-[90vh] overflow-y-auto p-4 sm:p-6">
        <DialogHeader className="space-y-2">
          <DialogTitle className="text-lg sm:text-xl">Modificar tarjeta de crédito</DialogTitle>
          <DialogDescription className="text-sm">
            Modifica manualmente el próximo día de cierre y el día de vencimiento de la tarjeta.
          </DialogDescription>
        </DialogHeader>
        
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3 sm:space-y-4 py-3 sm:py-4">
            {/* Selector de Tarjeta */}
            <FormField
              control={form.control}
              name="tarjeta"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Tarjeta de crédito</FormLabel>
                  <Select 
                    onValueChange={handleTarjetaChange} 
                    value={field.value}
                    disabled={isLoading}
                  >
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder={isLoading ? "Cargando tarjetas..." : "Seleccionar tarjeta"} />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {tarjetas.length === 0 ? (
                        <div className="p-2 text-sm text-muted-foreground text-center">
                          No hay tarjetas disponibles
                        </div>
                      ) : (
                        tarjetas.map((tarjeta) => (
                          <SelectItem key={tarjeta.id} value={tarjeta.id.toString()}>
                            {tarjeta.entidadFinanciera} - {tarjeta.redDePago} *{tarjeta.numeroTarjeta}
                          </SelectItem>
                        ))
                      )}
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
                    <FormLabel>Día de cierre</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Seleccionar día" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {Array.from({ length: 31 }, (_, i) => i + 1).map((day) => (
                          <SelectItem key={day} value={day.toString()}>
                            {day}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {/* Día de vencimiento */}
              <FormField
                control={form.control}
                name="diaVencimientoPago"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Día de vencimiento</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Seleccionar día" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {Array.from({ length: 31 }, (_, i) => i + 1).map((day) => (
                          <SelectItem key={day} value={day.toString()}>
                            {day}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className="rounded-lg bg-muted p-3">
              <p className="text-xs text-muted-foreground">
                Al modificar estos días, se actualizará el calendario de cierres y vencimientos para esta tarjeta.
              </p>
            </div>

            <div className="flex flex-col sm:flex-row justify-end gap-2 sm:gap-3 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => setOpen(false)}
                disabled={form.formState.isSubmitting}
                className="w-full sm:w-auto"
              >
                Cancelar
              </Button>
              <Button 
                type="submit" 
                disabled={form.formState.isSubmitting || updateTarjetaMutation.isPending}
                className="w-full sm:w-auto"
              >
                {updateTarjetaMutation.isPending ? 'Modificando...' : 'Modificar tarjeta'}
              </Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
