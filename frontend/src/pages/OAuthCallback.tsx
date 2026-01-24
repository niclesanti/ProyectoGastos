import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'

/**
 * Página de callback después de autenticación OAuth2.
 * Espera a que la sesión se establezca completamente antes de redirigir.
 */
export function OAuthCallback() {
  const navigate = useNavigate()
  const { refreshAuth } = useAuth()

  useEffect(() => {
    const handleCallback = async () => {
      console.log('🔄 [OAuthCallback] Procesando callback de OAuth2...')
      
      // Esperar un momento para asegurar que la cookie se procesó
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      console.log('🔄 [OAuthCallback] Refrescando autenticación...')
      await refreshAuth()
      
      console.log('➡️  [OAuthCallback] Redirigiendo al dashboard...')
      navigate('/', { replace: true })
    }

    handleCallback()
  }, [navigate, refreshAuth])

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
