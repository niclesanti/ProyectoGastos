import { useState } from 'react'
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from '@/components/ui/sheet'
import { Button } from '@/components/ui/button'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Separator } from '@/components/ui/separator'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Badge } from '@/components/ui/badge'
import { Bell, CheckCheck } from 'lucide-react'
import { useNotificaciones } from '@/hooks/useNotificaciones'
import { NotificationItem } from './NotificationItem'
import type { NotificacionDTOResponse } from '@/types'

interface NotificationCenterProps {
  trigger?: React.ReactNode
}

/**
 * Componente NotificationCenter
 * 
 * Panel lateral completo para gestionar notificaciones.
 * Muestra todas las notificaciones con tabs para filtrar por leídas/no leídas.
 * 
 * Características:
 * - Sheet lateral responsivo (drawer en mobile)
 * - Tabs para filtrar: Todas, No leídas, Leídas
 * - Acciones masivas: Marcar todas como leídas
 * - Vista detallada de cada notificación
 * - Scroll infinito para listas largas
 */
export const NotificationCenter = ({ trigger }: NotificationCenterProps) => {
  const {
    notificaciones,
    unreadCount,
    marcarComoLeida,
    marcarTodasComoLeidas,
    eliminarNotificacion,
  } = useNotificaciones()

  const [open, setOpen] = useState(false)
  const [activeTab, setActiveTab] = useState<'todas' | 'no-leidas' | 'leidas'>('todas')

  // Filtrar notificaciones según el tab activo
  const notificacionesFiltradas = (() => {
    switch (activeTab) {
      case 'no-leidas':
        return notificaciones.filter(n => !n.leida)
      case 'leidas':
        return notificaciones.filter(n => n.leida)
      default:
        return notificaciones
    }
  })()

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

  const defaultTrigger = (
    <Button variant="outline" size="sm" className="gap-2">
      <Bell className="h-4 w-4" />
      Centro de Notificaciones
      {unreadCount > 0 && (
        <Badge variant="destructive" className="ml-1">
          {unreadCount}
        </Badge>
      )}
    </Button>
  )

  return (
    <Sheet open={open} onOpenChange={setOpen}>
      <SheetTrigger asChild>
        {trigger || defaultTrigger}
      </SheetTrigger>
      <SheetContent className="w-full sm:max-w-xl p-0 flex flex-col">
        <SheetHeader className="p-6 pb-4">
          <div className="flex items-center justify-between">
            <div>
              <SheetTitle className="text-2xl">Notificaciones</SheetTitle>
              <SheetDescription>
                Gestiona todas tus notificaciones
              </SheetDescription>
            </div>
            {unreadCount > 0 && (
              <Button
                variant="outline"
                size="sm"
                onClick={handleMarcarTodasComoLeidas}
                className="gap-2"
              >
                <CheckCheck className="h-4 w-4" />
                Marcar todas
              </Button>
            )}
          </div>
        </SheetHeader>

        <Separator />

        <Tabs 
          value={activeTab} 
          onValueChange={(value) => setActiveTab(value as typeof activeTab)}
          className="flex-1 flex flex-col"
        >
          <div className="px-6 pt-4">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="todas" className="gap-2">
                Todas
                <Badge variant="secondary" className="ml-1">
                  {notificaciones.length}
                </Badge>
              </TabsTrigger>
              <TabsTrigger value="no-leidas" className="gap-2">
                No leídas
                {unreadCount > 0 && (
                  <Badge variant="destructive" className="ml-1">
                    {unreadCount}
                  </Badge>
                )}
              </TabsTrigger>
              <TabsTrigger value="leidas">
                Leídas
                <Badge variant="secondary" className="ml-1">
                  {notificaciones.filter(n => n.leida).length}
                </Badge>
              </TabsTrigger>
            </TabsList>
          </div>

          <div className="flex-1 overflow-hidden">
            <TabsContent value="todas" className="m-0 h-full">
              <NotificationList
                notificaciones={notificacionesFiltradas}
                onMarcarLeida={handleMarcarComoLeida}
                onEliminar={handleEliminarNotificacion}
              />
            </TabsContent>
            
            <TabsContent value="no-leidas" className="m-0 h-full">
              <NotificationList
                notificaciones={notificacionesFiltradas}
                onMarcarLeida={handleMarcarComoLeida}
                onEliminar={handleEliminarNotificacion}
                emptyMessage="No tienes notificaciones sin leer"
              />
            </TabsContent>
            
            <TabsContent value="leidas" className="m-0 h-full">
              <NotificationList
                notificaciones={notificacionesFiltradas}
                onMarcarLeida={handleMarcarComoLeida}
                onEliminar={handleEliminarNotificacion}
                emptyMessage="No tienes notificaciones leídas"
              />
            </TabsContent>
          </div>
        </Tabs>
      </SheetContent>
    </Sheet>
  )
}

/**
 * Componente interno para renderizar la lista de notificaciones
 */
interface NotificationListProps {
  notificaciones: NotificacionDTOResponse[]
  onMarcarLeida: (id: number) => void
  onEliminar: (id: number) => void
  emptyMessage?: string
}

const NotificationList = ({
  notificaciones,
  onMarcarLeida,
  onEliminar,
  emptyMessage = 'No tienes notificaciones',
}: NotificationListProps) => {
  if (notificaciones.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-full p-8 text-center">
        <Bell className="h-12 w-12 text-muted-foreground/50 mb-4" />
        <p className="text-muted-foreground">{emptyMessage}</p>
      </div>
    )
  }

  return (
    <ScrollArea className="h-full">
      <div className="flex flex-col">
        {notificaciones.map((notificacion, index) => (
          <div key={notificacion.id}>
            <NotificationItem
              notificacion={notificacion}
              onMarcarLeida={onMarcarLeida}
              onEliminar={onEliminar}
            />
            {index < notificaciones.length - 1 && <Separator />}
          </div>
        ))}
      </div>
    </ScrollArea>
  )
}
