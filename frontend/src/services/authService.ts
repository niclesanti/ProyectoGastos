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
      const response = await fetch(`${API_URL}/api/auth/status`, {
        method: 'GET',
        credentials: 'include', // Importante: incluir cookies
        headers: {
          'Content-Type': 'application/json',
        },
      })

      if (!response.ok) {
        return { authenticated: false, user: null }
      }

      const data = await response.json()
      return data
    } catch (error) {
      console.error('Error checking auth status:', error)
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
