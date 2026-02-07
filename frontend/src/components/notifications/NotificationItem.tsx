import { X, Check, 
  CreditCard, 
  CalendarClock, 
  UserPlus, 
  Users, 
  Bell 
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import type { NotificacionDTOResponse, TipoNotificacion } from '@/types'

interface NotificationItemProps {
  notificacion: NotificacionDTOResponse
  onMarcarLeida: (id: number) => void
  onEliminar: (id: number) => void
  onClick?: () => void
}

/**
 * Obtiene el icono apropiado según el tipo de notificación.
 */
const getNotificationIcon = (tipo: TipoNotificacion) => {
  const iconMap: Record<TipoNotificacion, React.ElementType> = {
    CIERRE_TARJETA: CreditCard,
    VENCIMIENTO_RESUMEN: CalendarClock,
    INVITACION_ESPACIO: UserPlus,
    MIEMBRO_AGREGADO: Users,
    SISTEMA: Bell,
  }
  
  return iconMap[tipo] || Bell
}

/**
 * Obtiene el color del icono según el tipo de notificación.
 */
const getNotificationColor = (tipo: TipoNotificacion): string => {
  const colorMap: Record<TipoNotificacion, string> = {
    CIERRE_TARJETA: 'text-blue-500',
    VENCIMIENTO_RESUMEN: 'text-orange-500',
    INVITACION_ESPACIO: 'text-green-500',
    MIEMBRO_AGREGADO: 'text-purple-500',
    SISTEMA: 'text-gray-500',
  }
  
  return colorMap[tipo] || 'text-gray-500'
}

/**
 * Componente NotificationItem
 * 
 * Representa una notificación individual en la lista.
 * Muestra el icono, mensaje, tiempo transcurrido y acciones (marcar como leída, eliminar).
 */
export const NotificationItem = ({
  notificacion,
  onMarcarLeida,
  onEliminar,
  onClick,
}: NotificationItemProps) => {
  const Icon = getNotificationIcon(notificacion.tipo)
  const iconColor = getNotificationColor(notificacion.tipo)

  const handleMarcarLeida = (e: React.MouseEvent) => {
    e.stopPropagation()
    onMarcarLeida(notificacion.id)
  }

  const handleEliminar = (e: React.MouseEvent) => {
    e.stopPropagation()
    onEliminar(notificacion.id)
  }

  return (
    <div
      className={cn(
        "p-4 hover:bg-accent transition-colors cursor-pointer group relative",
        !notificacion.leida && "bg-accent/50"
      )}
      onClick={onClick}
    >
      <div className="flex gap-3">
        {/* Icono */}
        <div className={cn("flex-shrink-0 mt-0.5", iconColor)}>
          <Icon className="h-5 w-5" />
        </div>
        
        {/* Contenido */}
        <div className="flex-1 min-w-0">
          <p className={cn(
            "text-sm break-words",
            !notificacion.leida && "font-medium"
          )}>
            {notificacion.mensaje}
          </p>
        </div>
        
        {/* Indicador de no leída */}
        {!notificacion.leida && (
          <div className="flex-shrink-0">
            <div className="w-2 h-2 bg-blue-500 rounded-full" />
          </div>
        )}
      </div>
      
      {/* Acciones (se muestran al hacer hover) */}
      <div className="absolute top-2 right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
        {!notificacion.leida && (
          <Button
            variant="ghost"
            size="icon"
            className="h-6 w-6"
            onClick={handleMarcarLeida}
            title="Marcar como leída"
          >
            <Check className="h-3 w-3" />
          </Button>
        )}
        <Button
          variant="ghost"
          size="icon"
          className="h-6 w-6"
          onClick={handleEliminar}
          title="Eliminar"
        >
          <X className="h-3 w-3" />
        </Button>
      </div>
    </div>
  )
}
