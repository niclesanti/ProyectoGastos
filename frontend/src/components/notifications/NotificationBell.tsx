import { Bell } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Separator } from '@/components/ui/separator'
import { useNotificaciones } from '@/hooks/useNotificaciones'
import { NotificationItem } from './NotificationItem'
import { useState } from 'react'
import { cn } from '@/lib/utils'

/**
 * Componente NotificationBell
 * 
 * Icono de campana con badge que muestra el contador de notificaciones no leídas.
 * Al hacer clic, despliega un popover con las notificaciones más recientes.
 * 
 * Características:
 * - Badge animado con contador de no leídas
 * - Popover con scroll para lista de notificaciones
 * - Botón "Marcar todas como leídas"
 * - Enlace a Notification Center para ver todas
 */
export const NotificationBell = () => {
  const {
    notificaciones,
    unreadCount,
    marcarComoLeida,
    marcarTodasComoLeidas,
    eliminarNotificacion,
  } = useNotificaciones()

  const [open, setOpen] = useState(false)

  // Mostrar solo las primeras 5 notificaciones en el popover
  const notificacionesRecientes = notificaciones.slice(0, 5)

  const handleMarcarComoLeida = async (id: number) => {
    try {
      await marcarComoLeida(id)
    } catch (error) {
      console.error('Error al marcar como leída:', error)
    }
  }

  const handleMarcarTodasComoLeidas = async () => {
    try {
      await marcarTodasComoLeidas()
    } catch (error) {
      console.error('Error al marcar todas como leídas:', error)
    }
  }

  const handleEliminarNotificacion = async (id: number) => {
    try {
      await eliminarNotificacion(id)
    } catch (error) {
      console.error('Error al eliminar notificación:', error)
    }
  }

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className="relative"
          aria-label="Notificaciones"
        >
          <Bell className="h-5 w-5" />
          {unreadCount > 0 && (
            <Badge
              variant="destructive"
              className={cn(
                "absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs",
                "animate-in fade-in zoom-in duration-300"
              )}
            >
              {unreadCount > 99 ? '99+' : unreadCount}
            </Badge>
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-80 p-0" align="end">
        <div className="flex items-center justify-between p-4 border-b">
          <h3 className="font-semibold">Notificaciones</h3>
          {unreadCount > 0 && (
            <Button
              variant="ghost"
              size="sm"
              onClick={handleMarcarTodasComoLeidas}
              className="text-xs h-auto py-1"
            >
              Marcar todas como leídas
            </Button>
          )}
        </div>
        
        {notificacionesRecientes.length === 0 ? (
          <div className="p-8 text-center text-sm text-muted-foreground">
            No tienes notificaciones
          </div>
        ) : (
          <>
            <ScrollArea className="h-[400px]">
              <div className="flex flex-col">
                {notificacionesRecientes.map((notificacion, index) => (
                  <div key={notificacion.id}>
                    <NotificationItem
                      notificacion={notificacion}
                      onMarcarLeida={handleMarcarComoLeida}
                      onEliminar={handleEliminarNotificacion}
                      onClick={() => setOpen(false)}
                    />
                    {index < notificacionesRecientes.length - 1 && (
                      <Separator />
                    )}
                  </div>
                ))}
              </div>
            </ScrollArea>
            
            {notificaciones.length > 5 && (
              <>
                <Separator />
                <div className="p-2">
                  <Button
                    variant="ghost"
                    className="w-full text-xs"
                    onClick={() => {
                      setOpen(false)
                      // Aquí puedes agregar navegación al NotificationCenter
                      // Por ejemplo: navigate('/notificaciones')
                    }}
                  >
                    Ver todas las notificaciones ({notificaciones.length})
                  </Button>
                </div>
              </>
            )}
          </>
        )}
      </PopoverContent>
    </Popover>
  )
}
