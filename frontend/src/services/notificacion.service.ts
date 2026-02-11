import { apiClient } from '@/lib/api-client'
import type { NotificacionDTOResponse } from '@/types'
import { devLog, devError } from '@/utils/logger'

/**
 * Servicio para la gestión de notificaciones.
 * 
 * Proporciona métodos para obtener, marcar como leídas y eliminar
 * notificaciones del usuario autenticado.
 */
export const notificacionService = {
  /**
   * Obtiene todas las notificaciones del usuario autenticado.
   * Retorna máximo las 50 más recientes ordenadas por fecha descendente.
   */
  async obtenerNotificaciones(): Promise<NotificacionDTOResponse[]> {
    const { data } = await apiClient.get<NotificacionDTOResponse[]>('/notificaciones')
    return data
  },

  /**
   * Cuenta las notificaciones no leídas del usuario.
   * Útil para mostrar el badge en el bell icon.
   */
  async contarNoLeidas(): Promise<number> {
    const { data } = await apiClient.get<{ count: number }>('/notificaciones/no-leidas/count')
    return data.count
  },

  /**
   * Marca una notificación específica como leída.
   */
  async marcarComoLeida(id: number): Promise<void> {
    await apiClient.put(`/notificaciones/${id}/leer`)
  },

  /**
   * Marca todas las notificaciones del usuario como leídas.
   */
  async marcarTodasComoLeidas(): Promise<void> {
    await apiClient.put('/notificaciones/marcar-todas-leidas')
  },

  /**
   * Elimina una notificación específica.
   */
  async eliminarNotificacion(id: number): Promise<void> {
    await apiClient.delete(`/notificaciones/${id}`)
  },

  /**
   * Crea una conexión SSE para recibir notificaciones en tiempo real.
   * Retorna un EventSource que debe ser cerrado manualmente cuando ya no se necesite.
   * 
   * Usa Query Parameter para enviar el token JWT porque es más confiable que headers
   * con EventSource (no requiere polyfill y tiene mejor compatibilidad con navegadores).
   */
  crearConexionSSE(): EventSource {
    const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const token = localStorage.getItem('auth_token')
    
    if (!token) {
      devError('❌ SSE: No hay token JWT disponible')
      // Retornar un EventSource dummy que falle inmediatamente
      const dummySource = new EventSource('about:blank')
      dummySource.close()
      return dummySource
    }
    
    // Construir URL con token como query parameter
    // Esto es más confiable que usar headers personalizados en SSE
    const url = `${baseURL}/api/notificaciones/stream?token=${encodeURIComponent(token)}`
    
    devLog('🔗 SSE: Creando conexión a:', `${baseURL}/api/notificaciones/stream`)
    devLog('🔑 SSE: Token presente:', token.substring(0, 20) + '...')
    
    try {
      // Usar EventSource nativo (no necesitamos polyfill con query params)
      const eventSource = new EventSource(url)
      
      devLog('✅ SSE: EventSource creado, readyState:', eventSource.readyState)
      
      return eventSource
    } catch (error) {
      devError('❌ SSE: Error al crear EventSource:', error)
      throw error
    }
  },
}
