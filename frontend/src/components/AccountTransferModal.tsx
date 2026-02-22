"use client"

import React, { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { useAppStore } from '@/store/app-store'
import { useCuentasBancarias, useTransferenciaCuentas } from '@/features/selectors/api/selector-queries'
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
import { Button } from '@/components/ui/button'
import { toast } from '@/hooks/useToast'
import { MoneyInput } from '@/components/MoneyInput'
import { ArrowDown } from 'lucide-react'
import { cn } from '@/lib/utils'

// Esquema de validación Zod
const transferFormSchema = z.object({
  cuentaOrigen: z.string().min(1, { message: "Por favor, selecciona una cuenta de origen." }),
  cuentaDestino: z.string().min(1, { message: "Por favor, selecciona una cuenta de destino." }),
  monto: z.number()
    .positive("El monto debe ser mayor a 0")
    .refine((val) => {
      const str = val.toString()
      const parts = str.split('.')
      if (parts.length === 1) return parts[0].length <= 12
      return parts[0].length <= 12 && (parts[1]?.length || 0) <= 2
    }, { message: "Máximo 12 dígitos enteros y 2 decimales." }),
}).refine((data) => data.cuentaOrigen !== data.cuentaDestino, {
  message: "La cuenta de origen y destino no pueden ser iguales.",
  path: ["cuentaDestino"],
})

type TransferFormValues = z.infer<typeof transferFormSchema>

interface AccountTransferModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function AccountTransferModal({ open, onOpenChange }: AccountTransferModalProps) {
  const currentWorkspace = useAppStore((state) => state.currentWorkspace)
  const { data: cuentas = [], isLoading: loadingCuentas } = useCuentasBancarias(currentWorkspace?.id)
  
  // Mutation para realizar transferencia
  const transferenciaMutation = useTransferenciaCuentas()

  // Ref para auto-focus en el campo monto
  const montoInputRef = React.useRef<HTMLInputElement>(null)

  // Inicializar el formulario
  const form = useForm<TransferFormValues>({
    resolver: zodResolver(transferFormSchema),
    defaultValues: {
      cuentaOrigen: '',
      cuentaDestino: '',
      monto: null as unknown as number,
    },
  })

  // Reiniciar formulario cuando se abre el modal
  useEffect(() => {
    if (open) {
      form.reset({
        cuentaOrigen: '',
        cuentaDestino: '',
        monto: null as unknown as number,
      })
    }
    
    // Auto-focus en el campo monto después de un breve delay
    if (open) {
      setTimeout(() => {
        montoInputRef.current?.focus()
      }, 100)
    }
  }, [open, form])

  const { refreshDashboard } = useDashboardCache()

  // Manejar envío del formulario
  const onSubmit = async (data: TransferFormValues) => {
    try {
      await transferenciaMutation.mutateAsync({
        idCuentaOrigen: parseInt(data.cuentaOrigen),
        idCuentaDestino: parseInt(data.cuentaDestino),
        monto: data.monto,
      })
      
      // Actualizar todo el dashboard incluyendo stats
      await refreshDashboard()
      
      toast.success('Transferencia realizada exitosamente')
      onOpenChange(false)
    } catch (error) {
      console.error('Error al realizar transferencia:', error)
      toast.error('Error al realizar la transferencia')
    }
  }

  // Manejar errores en el envío
  const handleFormError = () => {
    toast.error('Por favor, revisa los campos obligatorios.')
  }

  // MoneyInput maneja la validación de formato automáticamente

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto p-4 sm:p-6">
        <DialogHeader className="space-y-2">
          <DialogTitle className="text-lg sm:text-xl">Movimiento entre cuentas</DialogTitle>
          <DialogDescription className="text-sm">
            Anotar que realizaste un movimiento de dinero entre dos cuentas bancarias registradas en el sistema.
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit, handleFormError)} className="space-y-3 sm:space-y-4">
            {/* Monto - Premium Design */}
            <FormField
              control={form.control}
              name="monto"
              render={({ field }) => (
                <FormItem className="my-2 mb-3">
                  <FormControl>
                    <div className="relative flex items-center justify-center py-2">
                      {/* Símbolo de moneda fijo a la izquierda */}
                      <span className="text-3xl sm:text-4xl font-medium transition-colors mr-1 text-muted-foreground/60">
                        $
                      </span>
                      
                      {/* Input sin bordes */}
                      <div className="flex-1 max-w-md">
                        <MoneyInput
                          ref={montoInputRef}
                          value={field.value}
                          onChange={field.onChange}
                          min={0}
                          maxDigits={12}
                          maxDecimals={2}
                          placeholder="0.00"
                          showPrefix={false}
                          className="border-none bg-transparent text-left shadow-none text-3xl sm:text-4xl font-semibold h-auto py-2 px-0 focus-visible:ring-0 focus-visible:ring-offset-0 transition-colors duration-200 text-foreground placeholder:text-muted-foreground/30"
                        />
                      </div>
                    </div>
                  </FormControl>
                  <FormMessage className="text-center" />
                </FormItem>
              )}
            />

            {/* Agrupación de cuentas con icono de transferencia */}
            <div className="relative space-y-3">
              {/* Cuenta de origen */}
              <FormField
                control={form.control}
                name="cuentaOrigen"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Cuenta de origen</FormLabel>
                    <Select 
                      onValueChange={field.onChange} 
                      value={field.value}
                      disabled={loadingCuentas}
                    >
                      <FormControl>
                        <SelectTrigger className="h-9">
                          <SelectValue placeholder={loadingCuentas ? "Cargando..." : "Seleccionar cuenta de origen"} />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {cuentas.length === 0 ? (
                          <SelectItem value="empty" disabled>
                            No hay cuentas disponibles - Crear una nueva
                          </SelectItem>
                        ) : (
                          cuentas.map((c) => (
                            <SelectItem 
                              key={c.id} 
                              value={c.id.toString()}
                              disabled={c.id.toString() === form.watch('cuentaDestino')}
                            >
                              {c.nombre} - {c.entidadFinanciera} (${c.saldoActual.toNumber().toFixed(2)})
                            </SelectItem>
                          ))
                        )}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {/* Icono de flecha indicador de transferencia */}
              <div className="absolute right-2 top-1/2 -translate-y-1/2 flex items-center justify-center">
                <div className={cn(
                  "rounded-full p-1.5 transition-colors duration-300",
                  form.watch('cuentaOrigen') && form.watch('cuentaDestino') && 
                  form.watch('cuentaOrigen') !== form.watch('cuentaDestino')
                    ? "bg-emerald-500/10"
                    : "bg-muted/50"
                )}>
                  <ArrowDown 
                    className={cn(
                      "h-4 w-4 transition-colors duration-300",
                      form.watch('cuentaOrigen') && form.watch('cuentaDestino') && 
                      form.watch('cuentaOrigen') !== form.watch('cuentaDestino')
                        ? "text-emerald-500"
                        : "text-muted-foreground"
                    )}
                  />
                </div>
              </div>

              {/* Cuenta Destino */}
              <FormField
                control={form.control}
                name="cuentaDestino"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Cuenta de destino</FormLabel>
                    <Select 
                      onValueChange={field.onChange} 
                      value={field.value}
                      disabled={loadingCuentas}
                    >
                      <FormControl>
                        <SelectTrigger className="h-9">
                          <SelectValue placeholder={loadingCuentas ? "Cargando..." : "Seleccionar cuenta destino"} />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {cuentas.length === 0 ? (
                          <SelectItem value="empty" disabled>
                            No hay cuentas disponibles - Crear una nueva
                          </SelectItem>
                        ) : (
                          cuentas.map((c) => (
                            <SelectItem 
                              key={c.id} 
                              value={c.id.toString()}
                              disabled={c.id.toString() === form.watch('cuentaOrigen')}
                            >
                              {c.nombre} - {c.entidadFinanciera} (${c.saldoActual.toNumber().toFixed(2)})
                            </SelectItem>
                          ))
                        )}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
          </form>
        </Form>

        <DialogFooter className="mt-3 sm:mt-4 flex-row gap-2">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            className="flex-1"
          >
            Cancelar
          </Button>
          <Button 
            type="submit" 
            onClick={form.handleSubmit(onSubmit, handleFormError)}
            disabled={form.formState.isSubmitting || transferenciaMutation.isPending}
            className="flex-1"
          >
            {transferenciaMutation.isPending ? 'Realizando...' : 'Realizar movimiento'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
