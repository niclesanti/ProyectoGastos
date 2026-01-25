import axios from 'axios'

// Usar variable de entorno para la URL del backend
// En desarrollo: http://localhost:8080 (desde .env.local o docker-compose)
// En producción: https://tu-backend.cloud.run (desde variables de entorno del hosting)
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export const apiClient = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  withCredentials: true, // Mantener por compatibilidad
})

// Interceptor para agregar el token JWT a todas las peticiones
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Interceptor para manejar errores y extraer mensajes personalizados del backend
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Si el token es inválido o expiró, limpiar localStorage y redirigir
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token')
      window.location.href = '/login'
    }
    
    // Si el backend envió un mensaje personalizado en el formato ExceptionInfo
    if (error.response?.data?.message) {
      error.message = error.response.data.message
    }
    // Si es un error de red o sin respuesta del servidor
    else if (!error.response) {
      error.message = 'No se pudo conectar con el servidor. Verifica tu conexión a internet.'
    }
    // Si es un error genérico de HTTP sin mensaje personalizado
    else if (error.response?.status) {
      const statusMessages: Record<number, string> = {
        400: 'Solicitud inválida. Por favor verifica los datos ingresados.',
        401: 'No tienes autorización. Por favor inicia sesión nuevamente.',
        403: 'No tienes permisos para realizar esta acción.',
        404: 'El recurso solicitado no fue encontrado.',
        500: 'Error interno del servidor. Por favor intenta más tarde.',
        503: 'El servicio no está disponible. Por favor intenta más tarde.',
      }
      error.message = statusMessages[error.response.status] || `Error ${error.response.status}: ${error.response.statusText}`
    }
    
    return Promise.reject(error)
  }
)
