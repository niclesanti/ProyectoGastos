import { SidebarTrigger } from '@/components/ui/sidebar'
import { Separator } from '@/components/ui/separator'
import { useLocation, useNavigate } from 'react-router-dom'
import { NotificationBell } from '@/components/notifications'
import { Button } from '@/components/ui/button'
import { BrainCircuit } from 'lucide-react'
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from '@/components/ui/breadcrumb'

const routeTitles: Record<string, string> = {
  '/': 'Panel de datos',
  '/movimientos': 'Movimientos',
  '/creditos': 'Tarjetas de Crédito',
  '/agente-ia': 'Agente IA',
  '/configuracion': 'Configuración',
}

export function Header() {
  const location = useLocation()
  const navigate = useNavigate()
  const title = routeTitles[location.pathname] || 'Panel de datos'
  const isOnAgente = location.pathname === '/agente-ia'

  return (
    <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
      <SidebarTrigger className="-ml-1" />
      <Separator orientation="vertical" className="mr-2 h-4" />
      <div className="flex flex-1 items-center justify-between">
        <Breadcrumb>
          <BreadcrumbList>
            <BreadcrumbItem>
              <BreadcrumbLink href="/">App</BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator />
            <BreadcrumbItem>
              <BreadcrumbPage>{title}</BreadcrumbPage>
            </BreadcrumbItem>
          </BreadcrumbList>
        </Breadcrumb>

        <div className="flex items-center gap-1">
          {/* Acceso rápido al Agente IA */}
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate('/agente-ia')}
            disabled={isOnAgente}
            className="text-muted-foreground hover:text-violet-400 disabled:text-violet-400 disabled:opacity-100"
            title="Finanzas Copilot"
          >
            <BrainCircuit className="h-5 w-5" />
          </Button>
          {/* Sistema de Notificaciones en Tiempo Real */}
          <NotificationBell />
        </div>
      </div>
    </header>
  )
}
