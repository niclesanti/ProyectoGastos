import { apiClient } from '@/lib/api-client'
import type { AgenteIAChatRequest, AgenteIAChatResponse, AgenteIARateLimitStatus } from '@/types'
import { devLog, devError } from '@/utils/logger'

/**
 * Servicio para la interacción con el Agente IA financiero.
 * 
 * Proporciona métodos para enviar mensajes, streaming de respuestas
 * y consultar el estado del rate limit.
 */
export const agenteIAService = {
  /**
   * Envia un mensaje al agente IA sin streaming.
   * Retorna la respuesta completa cuando el agente termina de procesar.
   * 
   * Útil como fallback si SSE falla o para mensajes que no requieren
   * feedback inmediato.
   */
  async enviarMensaje(request: AgenteIAChatRequest): Promise<AgenteIAChatResponse> {
    const { data } = await apiClient.post<AgenteIAChatResponse>('/agente/chat', request)
    return data
  },

  /**
   * Crea una conexión SSE para recibir la respuesta del agente token por token.
   * Esto proporciona una experiencia más fluida mostrando el texto conforme
   * se genera (similar a ChatGPT).
   * 
   * El EventSource debe ser cerrado manualmente cuando ya no se necesite.
   * 
   * Eventos esperados:
   * - 'message': Token individual de texto
   * - 'done': Metadata final (functionsCalled, tokensUsed)
   * - 'error': Error durante procesamiento
   */
  crearConexionSSE(message: string, workspaceId: string): EventSource {
    const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const token = localStorage.getItem('auth_token')
    
    if (!token) {
      devError('❌ Agente IA SSE: No hay token JWT disponible')
      const dummySource = new EventSource('about:blank')
      dummySource.close()
      return dummySource
    }
    
    // Construir URL con parámetros
    const params = new URLSearchParams({
      message: message,
      workspaceId: workspaceId,
      token: token
    })
    
    const url = `${baseURL}/api/agente/chat/stream?${params.toString()}`
    
    devLog('🔗 Agente IA SSE: Creando conexión para mensaje:', message.substring(0, 50) + '...')
    
    try {
      const eventSource = new EventSource(url)
      devLog('✅ Agente IA SSE: EventSource creado, readyState:', eventSource.readyState)
      return eventSource
    } catch (error) {
      devError('❌ Agente IA SSE: Error al crear EventSource:', error)
      throw error
    }
  },

  /**
   * Consulta el estado del rate limit del usuario actual.
   * Retorna cuántos mensajes quedan disponibles en la ventana actual.
   */
  async consultarRateLimit(): Promise<AgenteIARateLimitStatus> {
    const { data } = await apiClient.get<AgenteIARateLimitStatus>('/agente/rate-limit')
    return data
  },
}
