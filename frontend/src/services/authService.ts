import { authLog, sanitizeToken } from '@/utils/secureLogger'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export interface User {
  id: string  // UUID
  nombre: string
  email: string
  fotoPerfil: string
}

export interface AuthStatus {
  authenticated: boolean
  user: User | null
  token?: string
}

class AuthService {
  /**
   * Redirige al usuario al flujo de autenticación OAuth2 de Google
   */
  loginWithGoogle(): void {
    window.location.href = `${API_URL}/oauth2/authorization/google`
  }

  /**
   * Verifica el estado de autenticación del usuario
   */
  async checkAuthStatus(): Promise<AuthStatus> {
    try {
      authLog.info('Verificando estado de autenticación...')
      authLog.info('API URL:', { url: API_URL })
      
      const token = localStorage.getItem('auth_token')
      authLog.info('Token en localStorage:', { status: token ? 'Presente' : 'Ausente' })
      
      const headers: HeadersInit = {
        'Content-Type': 'application/json',
      }
      
      // Si hay token, agregarlo al header Authorization
      if (token) {
        headers['Authorization'] = `Bearer ${token}`
      }
      
      const response = await fetch(`${API_URL}/api/auth/status`, {
        method: 'GET',
        credentials: 'include', // Mantener por compatibilidad
        headers,
      })

      authLog.info('Response status:', { status: response.status })

      if (!response.ok) {
        authLog.info('Usuario no autenticado', { status: response.status })
        // Limpiar token inválido
        localStorage.removeItem('auth_token')
        return { authenticated: false, user: null }
      }

      const data = await response.json()
      
      // ✅ SEGURIDAD: Sanitizar token antes de loguear
      authLog.success('Usuario autenticado')
      authLog.info('Datos de usuario:', {
        authenticated: data.authenticated,
        user: data.user,
        token: sanitizeToken(data.token)
      })
      
      // Si el backend devuelve un nuevo token, actualizarlo
      if (data.token) {
        localStorage.setItem('auth_token', data.token)
      }
      
      return data
    } catch (error) {
      authLog.error('Error verificando estado de autenticación:', error)
      return { authenticated: false, user: null }
    }
  }

  /**
   * Obtiene los datos del usuario autenticado
   */
  async getCurrentUser(): Promise<User | null> {
    try {
      const response = await fetch(`${API_URL}/usuario/me`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
      })

      if (!response.ok) {
        return null
      }

      return await response.json()
    } catch (error) {
      console.error('Error fetching current user:', error)
      return null
    }
  }

  /**
   * Cierra la sesión del usuario
   */
  async logout(): Promise<void> {
    try {
      // Limpiar el token de localStorage
      localStorage.removeItem('auth_token')
      
      await fetch(`${API_URL}/logout`, {
        method: 'POST',
        credentials: 'include',
      })
      
      // Redirigir al login
      window.location.href = '/login'
    } catch (error) {
      console.error('Error during logout:', error)
      // Aún así redirigir al login
      window.location.href = '/login'
    }
  }
}

export const authService = new AuthService()
