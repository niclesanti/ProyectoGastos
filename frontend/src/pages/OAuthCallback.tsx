import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'

/**
 * Página de callback después de autenticación OAuth2.
 * Captura el token JWT de la URL y lo almacena en localStorage.
 */
export function OAuthCallback() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { refreshAuth } = useAuth()

  useEffect(() => {
    const handleCallback = async () => {
      console.log('🔄 [OAuthCallback] Procesando callback de OAuth2...')
      
      // Obtener el token JWT de los parámetros de la URL
      const token = searchParams.get('token')
      
      if (token) {
        console.log('✅ [OAuthCallback] Token JWT recibido, almacenando...')
        // Guardar el token en localStorage
        localStorage.setItem('auth_token', token)
        
        console.log('🔄 [OAuthCallback] Refrescando autenticación...')
        await refreshAuth()
        
        console.log('➡️  [OAuthCallback] Redirigiendo al dashboard...')
        navigate('/', { replace: true })
      } else {
        console.error('❌ [OAuthCallback] No se recibió token en la URL')
        navigate('/login?error=true', { replace: true })
      }
    }

    handleCallback()
  }, [navigate, refreshAuth, searchParams])

  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <div className="text-center space-y-4">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
        <p className="text-muted-foreground">Completando autenticación...</p>
        <p className="text-sm text-muted-foreground">Por favor espera un momento</p>
      </div>
    </div>
  )
}
