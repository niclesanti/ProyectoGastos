import { useEffect, useRef, useCallback } from 'react'
import { useAppStore } from '@/store/app-store'
import { notificacionService } from '@/services/notificacion.service'
import { toast } from 'sonner'
import type { NotificacionDTOResponse, TipoNotificacion } from '@/types'

/**
 * Hook personalizado para la gestión de notificaciones.
 * 
 * Características:
 * - Conexión SSE para notificaciones en tiempo real
 * - Caché inteligente de 5 minutos
 * - Actualización automática del estado
 * - Toasts para notificaciones críticas
 * - Limpieza automática al desmontar
 */
export const useNotificaciones = () => {
  const eventSourceRef = useRef<EventSource | null>(null)
  const isConnectingRef = useRef<boolean>(false)
  
  const {
    notificaciones,
    unreadCount,
    loadNotificaciones,
    loadUnreadCount,
    marcarComoLeida,
    marcarTodasComoLeidas,
    eliminarNotificacion,
    agregarNotificacion,
    invalidateNotificaciones,
  } = useAppStore()

  /**
   * Determina si una notificación debe mostrarse como toast.
   * Solo se muestran notificaciones críticas o de alta prioridad.
   */
  const shouldShowToast = (tipo: TipoNotificacion): boolean => {
    const tiposToast: string[] = [
      'CIERRE_TARJETA',
      'VENCIMIENTO_RESUMEN',
      'INVITACION_ESPACIO',
    ]
    return tiposToast.includes(tipo as string)
  }

  /**
   * Maneja la recepción de una nueva notificación via SSE.
   */
  const handleNuevaNotificacion = useCallback((notificacion: NotificacionDTOResponse) => {
    // Agregar al store
    agregarNotificacion(notificacion)
    
    // Mostrar toast si es una notificación crítica
    if (shouldShowToast(notificacion.tipo)) {
      toast.info(notificacion.mensaje, {
        duration: 5000,
        action: {
          label: 'Ver',
          onClick: () => {
            // Este callback puede ser sobrescrito por el componente que use el hook
            console.log('Ver notificación:', notificacion.id)
          },
        },
      })
    }
  }, [agregarNotificacion])

  /**
   * Establece la conexión SSE para recibir notificaciones en tiempo real.
   */
  const conectarSSE = useCallback(() => {
    // Evitar múltiples conexiones simultáneas
    if (eventSourceRef.current?.readyState === EventSource.OPEN) {
      console.log('⚠️ SSE: Ya existe una conexión abierta, ignorando...')
      return
    }

    // Evitar múltiples intentos de conexión simultáneos
    if (isConnectingRef.current) {
      console.log('⚠️ SSE: Ya hay un intento de conexión en progreso, ignorando...')
      return
    }

    console.log('🔄 SSE: Iniciando conexión...')
    isConnectingRef.current = true
    
    try {
      const eventSource = notificacionService.crearConexionSSE()
      eventSourceRef.current = eventSource

      // Listener para cuando se abre la conexión
      eventSource.onopen = () => {
        console.log('✅ SSE: Conexión abierta exitosamente')
        isConnectingRef.current = false
      }

      // Confirmación de conexión (evento personalizado del servidor)
      eventSource.addEventListener('connected', (event) => {
        console.log('✅ SSE: Confirmación de conexión recibida del servidor:', event.data)
      })

      // Manejar notificaciones (evento con nombre "notification" enviado por el backend)
      eventSource.addEventListener('notification', (event) => {
        console.log('📨 SSE: Notificación recibida:', event.data)
        try {
          const notificacion: NotificacionDTOResponse = JSON.parse(event.data)
          console.log('🔔 SSE: Notificación procesada:', notificacion.tipo, notificacion.mensaje)
          handleNuevaNotificacion(notificacion)
        } catch (error) {
          console.error('❌ SSE: Error al parsear notificación:', error)
        }
      })

      // Manejar errores
      eventSource.onerror = (error) => {
        console.error('❌ SSE: Error en conexión:', {
          error,
          readyState: eventSource.readyState,
          url: eventSource.url
        })
        
        // Resetear flag de conexión
        isConnectingRef.current = false
        
        // Estados: 0=CONNECTING, 1=OPEN, 2=CLOSED
        if (eventSource.readyState === EventSource.CLOSED) {
          console.error('❌ SSE: Conexión cerrada por el servidor')
          
          const token = localStorage.getItem('auth_token')
          if (!token) {
            console.error('❌ SSE: No hay token JWT. Redirigiendo a login...')
            window.location.href = '/login'
            return
          }
        }
        
        // Cerrar y limpiar
        eventSource.close()
        eventSourceRef.current = null
        
        // Reintentar conexión después de 5 segundos
        console.log('🔄 SSE: Reintentando conexión en 5 segundos...')
        setTimeout(() => {
          conectarSSE()
        }, 5000)
      }

    } catch (error) {
      console.error('❌ SSE: Error al crear conexión:', error)
      isConnectingRef.current = false
    }
  }, [handleNuevaNotificacion])

  /**
   * Cierra la conexión SSE.
   */
  const desconectarSSE = useCallback(() => {
    if (eventSourceRef.current) {
      console.log('🔌 SSE: Cerrando conexión...')
      eventSourceRef.current.close()
      eventSourceRef.current = null
    }
    isConnectingRef.current = false
  }, [])

  /**
   * Carga inicial de notificaciones y establecimiento de SSE.
   */
  useEffect(() => {
    // Cargar notificaciones iniciales
    loadNotificaciones()
    
    // Establecer conexión SSE
    conectarSSE()

    // Cleanup: cerrar conexión al desmontar
    return () => {
      console.log('🔄 SSE: Limpiando conexión al desmontar componente...')
      isConnectingRef.current = false
      desconectarSSE()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []) // Array vacío para ejecutar solo una vez al montar

  return {
    notificaciones,
    unreadCount,
    loadNotificaciones,
    loadUnreadCount,
    marcarComoLeida,
    marcarTodasComoLeidas,
    eliminarNotificacion,
    invalidateNotificaciones,
    conectarSSE,
    desconectarSSE,
  }
}
