"use client"

import { useEffect } from 'react'
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
import { Input } from '@/components/ui/input'
import { toast } from 'sonner'

// Esquema de validación Zod
const transferFormSchema = z.object({
  cuentaOrigen: z.string().min(1, { message: "Por favor, selecciona una cuenta de origen." }),
  cuentaDestino: z.string().min(1, { message: "Por favor, selecciona una cuenta de destino." }),
  monto: z.string()
    .min(1, { message: "Por favor, indica el monto a transferir." })
    .refine((val) => {
      const num = parseFloat(val)
      return !isNaN(num) && num > 0
    }, { message: "El monto debe ser mayor a 0." })
    .refine((val) => {
      const parts = val.split('.')
      if (parts.length === 1) return parts[0].length <= 11
      return parts[0].length <= 11 && parts[1].length <= 2
    }, { message: "Máximo 11 dígitos enteros y 2 decimales." }),
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

  // Inicializar el formulario
  const form = useForm<TransferFormValues>({
    resolver: zodResolver(transferFormSchema),
    defaultValues: {
      cuentaOrigen: '',
      cuentaDestino: '',
      monto: '',
    },
  })

  // Reiniciar formulario cuando se abre el modal
  useEffect(() => {
    if (open) {
      form.reset({
        cuentaOrigen: '',
        cuentaDestino: '',
        monto: '',
      })
    }
  }, [open, form])

  const { refreshDashboard } = useDashboardCache()

  // Manejar envío del formulario
  const onSubmit = async (data: TransferFormValues) => {
    try {
      await transferenciaMutation.mutateAsync({
        idCuentaOrigen: parseInt(data.cuentaOrigen),
        idCuentaDestino: parseInt(data.cuentaDestino),
        monto: parseFloat(data.monto),
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

  // Restringir entrada de monto
  const handleMontoChange = (value: string) => {
    const regex = /^\d{0,11}(\.\d{0,2})?$/
    if (regex.test(value) || value === '') {
      form.setValue('monto', value)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Movimiento entre cuentas</DialogTitle>
          <DialogDescription>
            Anotar que realizaste un movimiento de dinero entre dos cuentas bancarias registradas en el sistema.
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit, handleFormError)} className="space-y-4">
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
                      {cuentas.map((c) => (
                        <SelectItem 
                          key={c.id} 
                          value={c.id.toString()}
                          disabled={c.id.toString() === form.watch('cuentaDestino')}
                        >
                          {c.nombre} - {c.entidadFinanciera} (${c.saldoActual.toFixed(2)})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

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
                      {cuentas.map((c) => (
                        <SelectItem 
                          key={c.id} 
                          value={c.id.toString()}
                          disabled={c.id.toString() === form.watch('cuentaOrigen')}
                        >
                          {c.nombre} - {c.entidadFinanciera} (${c.saldoActual.toFixed(2)})
                        </SelectItem>
                      ))}
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
          </form>
        </Form>

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
            disabled={form.formState.isSubmitting || transferenciaMutation.isPending}
          >
            {transferenciaMutation.isPending ? 'Realizando...' : 'Realizar movimiento'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
