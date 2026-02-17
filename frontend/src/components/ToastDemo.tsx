/**
 * Toast Testing Component
 * 
 * Este componente demuestra todas las variantes del nuevo sistema de toast
 * con Framer Motion + Radix UI.
 * 
 * Para probar: Agrega este componente temporalmente en cualquier página
 */

import { toast } from '@/hooks/useToast'
import { Button } from '@/components/ui/button'

export function ToastDemo() {
  return (
    <div className="fixed bottom-4 left-4 z-50 flex flex-col gap-2 p-4 bg-background/95 backdrop-blur border rounded-lg shadow-lg">
      <h3 className="text-sm font-semibold mb-2">🎨 Test Toasts (Framer Motion)</h3>
      
      <Button
        size="sm"
        variant="outline"
        onClick={() =>
          toast.success('¡Transacción registrada exitosamente!', {
            description: 'Se guardó correctamente en la base de datos',
            action: {
              label: 'Ver',
              onClick: () => console.log('Ver transacción')
            }
          })
        }
      >
        ✅ Success Toast
      </Button>

      <Button
        size="sm"
        variant="outline"
        onClick={() =>
          toast.error('Error al procesar la solicitud', {
            description: 'Por favor, intenta nuevamente'
          })
        }
      >
        ❌ Error Toast
      </Button>

      <Button
        size="sm"
        variant="outline"
        onClick={() =>
          toast.warning('Tu saldo está bajo', {
            description: 'Considera agregar fondos pronto'
          })
        }
      >
        ⚠️ Warning Toast
      </Button>

      <Button
        size="sm"
        variant="outline"
        onClick={() =>
          toast.info('Cierre de tarjeta en 3 días', {
            description: 'Recuerda revisar tus gastos',
            duration: 8000
          })
        }
      >
        ℹ️ Info Toast (8s)
      </Button>

      <Button
        size="sm"
        variant="outline"
        onClick={() => {
          toast.success('Toast 1')
          setTimeout(() => toast.info('Toast 2'), 200)
          setTimeout(() => toast.warning('Toast 3'), 400)
          setTimeout(() => toast.error('Toast 4 (Queue)'), 600)
        }}
      >
        🎯 Test Queue (Max 3)
      </Button>
    </div>
  )
}
