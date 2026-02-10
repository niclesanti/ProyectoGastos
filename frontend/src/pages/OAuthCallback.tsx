import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { authLog } from '@/utils/secureLogger'
import { Skeleton } from '@/components/ui/skeleton'

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
      authLog.info('Procesando callback de OAuth2...')
      
      // Obtener el token JWT de los parámetros de la URL
      const token = searchParams.get('token')
      
      if (token) {
        // ✅ SEGURIDAD: NO loguear el token completo
        authLog.success('Token JWT recibido, almacenando...')
        // Guardar el token en localStorage
        localStorage.setItem('auth_token', token)
        
        authLog.info('Refrescando autenticación...')
        await refreshAuth()
        
        authLog.info('Redirigiendo al dashboard...')
        navigate('/', { replace: true })
      } else {
        authLog.error('No se recibió token en la URL')
        navigate('/login?error=true', { replace: true })
      }
    }

    handleCallback()
  }, [navigate, refreshAuth, searchParams])

  return (
    <div className="min-h-screen flex flex-col bg-background">
      <div className="border-b">
        <div className="flex items-center justify-between px-4 py-3">
          <Skeleton className="h-8 w-32" />
          <Skeleton className="h-8 w-8 rounded-full" />
        </div>
      </div>
      <div className="flex flex-1">
        <div className="hidden lg:flex w-64 border-r flex-col p-4 space-y-4">
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
          <div className="flex-1" />
          <Skeleton className="h-10 w-full" />
        </div>
        <div className="flex-1 p-4 sm:p-6 lg:p-8 space-y-6">
          <div className="grid grid-cols-2 gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            <Skeleton className="h-80" />
            <Skeleton className="h-80" />
          </div>
          <Skeleton className="h-96" />
        </div>
      </div>
    </div>
  )
}
