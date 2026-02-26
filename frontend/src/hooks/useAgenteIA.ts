import { useEffect, useRef, useCallback, useState } from 'react'
import { useAppStore } from '@/store/app-store'
import { agenteIAService } from '@/services/agente-ia.service'
import { toast } from '@/hooks/useToast'
import { devLog, devError } from '@/utils/logger'

/**
 * Hook personalizado para la gestión del Agente IA.
 * 
 * Características:
 * - Conexión SSE para streaming de respuestas en tiempo real
 * - Gestión de historial de conversación por workspace
 * - Manejo de estados (idle, thinking, streaming, error)
 * - Reconexión automática en caso de error
 * - Limpieza automática al desmontar
 */
export const useAgenteIA = () => {
  const eventSourceRef = useRef<EventSource | null>(null)
  const isConnectingRef = useRef<boolean>(false)
  const mensajeIdStreamingRef = useRef<string | null>(null)
  
  const [tokensDisponibles, setTokensDisponibles] = useState<number | null>(null)
  
  const currentWorkspace = useAppStore(state => state.currentWorkspace)
  const agenteEstado = useAppStore(state => state.agenteEstado)
  const {
    loadConversacionAgente,
    agregarMensajeUsuario,
    iniciarRespuestaAgente,
    appendTokenRespuesta,
    finalizarRespuestaAgente,
    setAgenteEstado,
    limpiarConversacionAgente,
  } = useAppStore()
  
  // Obtener mensajes del workspace actual
  const mensajes = currentWorkspace 
    ? loadConversacionAgente(currentWorkspace.id)
    : []

  /**
   * Cierra la conexión SSE si existe.
   */
  const cerrarConexion = useCallback(() => {
    if (eventSourceRef.current) {
      devLog('🔌 Agente IA SSE: Cerrando conexión...')
      eventSourceRef.current.close()
      eventSourceRef.current = null
      isConnectingRef.current = false
      mensajeIdStreamingRef.current = null
    }
  }, [])

  /**
   * Envía un mensaje al agente IA y recibe respuesta via SSE.
   */
  const enviarMensaje = useCallback(async (mensaje: string) => {
    if (!currentWorkspace) {
      toast.error('No hay un espacio de trabajo seleccionado')
      return
    }
    
    if (!mensaje.trim()) {
      return
    }
    
    if (agenteEstado === 'streaming' || agenteEstado === 'thinking') {
      devLog('⚠️ Agente IA: Ya hay una conversación en curso, ignorando...')
      return
    }
    
    // Cerrar conexión previa si existe
    cerrarConexion()
    
    try {
      // Agregar mensaje del usuario al historial
      agregarMensajeUsuario(currentWorkspace.id, mensaje)
      
      // Cambiar estado a thinking
      setAgenteEstado('thinking')
      
      devLog('📤 Agente IA: Enviando mensaje:', mensaje.substring(0, 50) + '...')
      
      // Crear mensaje del agente (vacío inicialmente)
      const mensajeIdAgente = iniciarRespuestaAgente(currentWorkspace.id)
      mensajeIdStreamingRef.current = mensajeIdAgente
      
      // Crear conexión SSE
      const eventSource = agenteIAService.crearConexionSSE(mensaje, currentWorkspace.id)
      eventSourceRef.current = eventSource
      isConnectingRef.current = true
      
      // Listener para cuando se abre la conexión
      eventSource.onopen = () => {
        devLog('✅ Agente IA SSE: Conexión abierta')
        isConnectingRef.current = false
        setAgenteEstado('streaming')
      }
      
      // Listener para tokens individuales del LLM.
      // El backend envía cada token como evento 'token' con el texto JSON-encoded.
      // JSON encoding es necesario para preservar los espacios iniciales de cada token
      // (la spec SSE descarta el espacio inicial del campo data:, y los tokens del LLM
      // suelen empezar con espacio, p.ej. " gastos", " están").
      eventSource.addEventListener('token', (event) => {
        try {
          const token: string = JSON.parse(event.data)
          if (mensajeIdStreamingRef.current) {
            appendTokenRespuesta(currentWorkspace.id, mensajeIdStreamingRef.current, token)
          }
        } catch {
          // Fallback: usar el dato crudo si el JSON parse falla
          if (mensajeIdStreamingRef.current) {
            appendTokenRespuesta(currentWorkspace.id, mensajeIdStreamingRef.current, event.data)
          }
        }
      })
      
      // Listener para metadata final (cuando termina el streaming)
      eventSource.addEventListener('done', (event) => {
        try {
          const metadata = JSON.parse(event.data)
          devLog('✅ Agente IA SSE: Streaming completado', metadata)
          
          if (mensajeIdStreamingRef.current) {
            finalizarRespuestaAgente(
              currentWorkspace.id,
              mensajeIdStreamingRef.current,
              metadata.functionsCalled,
              metadata.tokensUsed
            )
          }
          
          cerrarConexion()
          setAgenteEstado('idle')
        } catch (error) {
          devError('❌ Agente IA SSE: Error al parsear metadata:', error)
        }
      })
      
      // Listener para errores de streaming (enviados por el servidor)
      eventSource.addEventListener('error-message', (event) => {
        devError('❌ Agente IA SSE: Error del servidor:', event.data)
        toast.error('Error al procesar tu mensaje: ' + event.data)
        cerrarConexion()
        setAgenteEstado('error')
      })
      
      // Listener para errores de conexión
      eventSource.onerror = (error) => {
        devError('❌ Agente IA SSE: Error en conexión:', {
          error,
          readyState: eventSource.readyState,
        })
        
        isConnectingRef.current = false
        
        if (eventSource.readyState === EventSource.CLOSED) {
          devError('❌ Agente IA SSE: Conexión cerrada por el servidor')
          
          const token = localStorage.getItem('auth_token')
          if (!token) {
            devError('❌ Agente IA SSE: No hay token JWT')
            toast.error('Sesión expirada. Redirigiendo a login...')
            window.location.href = '/login'
            return
          }
        }
        
        // Cerrar y limpiar
        cerrarConexion()
        setAgenteEstado('error')
        
        // Mostrar error al usuario
        toast.error('Error de conexión con el agente IA. Intenta nuevamente.')
      }
      
    } catch (error) {
      devError('❌ Agente IA: Error al enviar mensaje:', error)
      setAgenteEstado('error')
      
      if (error instanceof Error && error.message.includes('429')) {
        toast.error('Has excedido el límite de mensajes. Espera un momento e intenta nuevamente.')
      } else {
        toast.error('Error al comunicarse con el agente IA')
      }
    }
  }, [
    currentWorkspace,
    agenteEstado,
    agregarMensajeUsuario,
    iniciarRespuestaAgente,
    appendTokenRespuesta,
    finalizarRespuestaAgente,
    setAgenteEstado,
    cerrarConexion,
  ])

  /**
   * Limpia la conversación actual.
   */
  const limpiarConversacion = useCallback(() => {
    if (!currentWorkspace) return
    
    cerrarConexion()
    limpiarConversacionAgente(currentWorkspace.id)
    setAgenteEstado('idle')
    
    toast.success('Conversación reiniciada')
  }, [currentWorkspace, cerrarConexion, limpiarConversacionAgente, setAgenteEstado])

  /**
   * Consulta el rate limit disponible.
   */
  const consultarRateLimit = useCallback(async () => {
    try {
      const { tokensRemaining } = await agenteIAService.consultarRateLimit()
      setTokensDisponibles(tokensRemaining)
      return tokensRemaining
    } catch (error) {
      devError('❌ Agente IA: Error al consultar rate limit:', error)
      return null
    }
  }, [])

  /**
   * Limpia conexiones al cambiar de workspace.
   */
  useEffect(() => {
    return () => {
      cerrarConexion()
    }
  }, [currentWorkspace?.id, cerrarConexion])

  /**
   * Limpieza al desmontar el componente.
   */
  useEffect(() => {
    return () => {
      cerrarConexion()
    }
  }, [cerrarConexion])

  return {
    mensajes,
    estado: agenteEstado,
    enviarMensaje,
    limpiarConversacion,
    tokensDisponibles,
    consultarRateLimit,
  }
}
