import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import { Skeleton } from "@/components/ui/skeleton"
import { useAuth } from "@/contexts/AuthContext"

export function LoginPage() {
  const { login, isAuthenticated, isLoading } = useAuth()
  const navigate = useNavigate()
  const [isAuthenticating, setIsAuthenticating] = useState(() => {
    return sessionStorage.getItem('isAuthenticating') === 'true'
  })

  useEffect(() => {
    if (isAuthenticated && !isLoading) {
      sessionStorage.removeItem('isAuthenticating')
      navigate('/', { replace: true })
    }
  }, [isAuthenticated, isLoading, navigate])

  useEffect(() => {
    if (!isLoading && !isAuthenticated && isAuthenticating) {
      const timer = setTimeout(() => {
        const stillAuthenticating = sessionStorage.getItem('isAuthenticating') === 'true'
        if (stillAuthenticating && !isAuthenticated) {
          sessionStorage.removeItem('isAuthenticating')
          setIsAuthenticating(false)
        }
      }, 5000)
      
      return () => clearTimeout(timer)
    }
  }, [isLoading, isAuthenticated, isAuthenticating])

  const handleGoogleLogin = () => {
    sessionStorage.setItem('isAuthenticating', 'true')
    setIsAuthenticating(true)
    login()
  }

  if (isLoading || isAuthenticating) {
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

  return (
    <div className="min-h-screen flex flex-col bg-black">
      {/* Header con Logo */}
      <header className="absolute top-0 left-0 p-4 sm:p-6">
        <div className="flex items-center gap-2">
          <img 
            src="/logo.png" 
            alt="Finanzas" 
            className="w-5 h-5"
          />
          <span className="text-white font-medium text-sm">Finanzas</span>
        </div>
      </header>

      {/* Contenido Principal Centrado */}
      <main className="flex-1 flex items-center justify-center px-4">
        <div className="w-full max-w-[320px] space-y-6">
          {/* Título */}
          <div className="text-center space-y-2">
            <h1 className="text-2xl font-semibold text-white">
              Bienvenido a Finanzas
            </h1>
            <p className="text-sm text-zinc-500">
              Gestiona tus movimientos de forma simple y profesional.
            </p>
          </div>

          {/* Botón de Google */}
          <div className="space-y-4">
            <Button 
              onClick={handleGoogleLogin}
              className="w-full h-10 text-sm font-normal bg-zinc-900 hover:bg-zinc-800 text-white border border-zinc-800"
            >
              <svg 
                className="mr-2 h-4 w-4" 
                viewBox="0 0 24 24"
              >
                <path
                  d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                  fill="#4285F4"
                />
                <path
                  d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                  fill="#34A853"
                />
                <path
                  d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                  fill="#FBBC05"
                />
                <path
                  d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                  fill="#EA4335"
                />
              </svg>
              Continuar con Google
            </Button>

            <Separator className="bg-zinc-800" />

            {/* Textos de seguridad */}
            <div className="space-y-1">
              <p className="text-[11px] text-zinc-600 text-center leading-relaxed">
                Acceso seguro mediante protocolos estándar de Google
              </p>
              <p className="text-[11px] text-zinc-600 text-center leading-relaxed">
                Tus datos son privados y seguros
              </p>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="pb-6 pt-4 text-center">
        <p className="text-[11px] text-zinc-600">
          © 2026 Finanzas App
        </p>
      </footer>
    </div>
  )
}
