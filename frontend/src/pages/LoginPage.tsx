import { useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/contexts/AuthContext"

export function LoginPage() {
  const { login, isAuthenticated, isLoading } = useAuth()
  const navigate = useNavigate()

  // Si el usuario ya está autenticado, redirigir al dashboard
  useEffect(() => {
    if (isAuthenticated && !isLoading) {
      navigate('/', { replace: true })
    }
  }, [isAuthenticated, isLoading, navigate])

  const handleGoogleLogin = () => {
    login()
  }

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="text-center space-y-4">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
          <p className="text-muted-foreground">Cargando...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-background p-4 relative overflow-hidden">
      {/* Background glow effect */}
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-indigo-950/20 via-transparent to-transparent" />
      
      {/* Grid pattern */}
      <div className="absolute inset-0 bg-[linear-gradient(to_right,#27272a_1px,transparent_1px),linear-gradient(to_bottom,#27272a_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_80%_50%_at_50%_50%,#000_70%,transparent_110%)] opacity-20" />
      
      <div className="relative z-10 w-full max-w-sm">
        <Card className="backdrop-blur-sm bg-card/95 border-zinc-800/50 shadow-2xl">
          <CardHeader className="space-y-4 flex flex-col items-center text-center pb-4">
            <div className="w-12 h-12 flex items-center justify-center">
              <img 
                src="/logo.png" 
                alt="Logo" 
                className="w-full h-full object-contain"
              />
            </div>
            <div className="space-y-2">
              <CardTitle className="text-2xl font-bold">
                Inicia sesión o suscríbete
              </CardTitle>
              <CardDescription className="text-base text-zinc-400">
                ¡Lleva el registro de tus finanzas personales a otro nivel!
              </CardDescription>
            </div>
          </CardHeader>
          <CardContent className="space-y-3">
            <Button 
              onClick={handleGoogleLogin}
              variant="outline" 
              className="w-full h-11 text-base border-zinc-800 hover:bg-zinc-900 hover:border-zinc-700 transition-all duration-200"
            >
              <svg 
                className="mr-2 h-5 w-5" 
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
            <p className="text-[10px] text-zinc-500 text-center leading-relaxed px-2">
              Tu información está protegida por encriptación de grado bancario
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Footer */}
      <footer className="relative z-10 mt-8 flex items-center gap-4 text-xs text-zinc-500">
        <a href="#" className="hover:text-zinc-300 transition-colors">
          Política de privacidad
        </a>
        <span className="text-zinc-700">•</span>
        <a href="#" className="hover:text-zinc-300 transition-colors">
          Términos de servicio
        </a>
        <span className="text-zinc-700">•</span>
        <a href="#" className="hover:text-zinc-300 transition-colors">
          Soporte
        </a>
      </footer>
    </div>
  )
}
