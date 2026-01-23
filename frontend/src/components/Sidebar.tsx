import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import {
  LayoutDashboard,
  ArrowLeftRight,
  CreditCard,
  Settings,
  ChevronsUpDown,
  LogOut,
  User,
  Plus,
  TrendingDown,
  ArrowRightLeft,
  Receipt,
} from 'lucide-react'
import { useAuth } from '@/contexts/AuthContext'
import { useWorkspaces } from '@/features/workspaces/api/workspace-queries'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
  SidebarSeparator,
} from '@/components/ui/sidebar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useAppStore } from '@/store/app-store'
import { TransactionModal } from '@/components/TransactionModal'
import { AccountTransferModal } from '@/components/AccountTransferModal'
import { CreditPurchaseModal } from '@/components/CreditPurchaseModal'
import { CardPaymentModal } from '@/components/CardPaymentModal'

const navigation = [
  { name: 'Panel de datos', href: '/', icon: LayoutDashboard },
  { name: 'Movimientos', href: '/movimientos', icon: ArrowLeftRight },
  { name: 'Tarjetas de Crédito', href: '/creditos', icon: CreditCard },
  { name: 'Configuración', href: '/configuracion', icon: Settings },
]

export function AppSidebar() {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const { currentWorkspace, setCurrentWorkspace } = useAppStore()
  
  // Cargar espacios de trabajo desde el backend con TanStack Query
  const { data: workspaces = [], isLoading: isLoadingWorkspaces } = useWorkspaces(user?.id?.toString())
  
  const [transactionModalOpen, setTransactionModalOpen] = useState(false)
  const [accountTransferModalOpen, setAccountTransferModalOpen] = useState(false)
  const [creditPurchaseModalOpen, setCreditPurchaseModalOpen] = useState(false)
  const [cardPaymentModalOpen, setCardPaymentModalOpen] = useState(false)

  const getUserInitials = () => {
    if (!user?.nombre) return 'U'
    return user.nombre
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2)
  }

  return (
    <>
      <Sidebar collapsible="icon">
      <SidebarHeader>
        {/* Workspace Switcher */}
        <SidebarMenu>
          <SidebarMenuItem>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <SidebarMenuButton
                  size="lg"
                  className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                >
                  <div className="flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
                    {currentWorkspace?.nombre.charAt(0).toUpperCase() || 'P'}
                  </div>
                  <div className="grid flex-1 text-left text-sm leading-tight">
                    <span className="truncate font-semibold">
                      {currentWorkspace?.nombre || 'Personal'}
                    </span>
                    <span className="truncate text-xs">Espacio de trabajo</span>
                  </div>
                  <ChevronsUpDown className="ml-auto" />
                </SidebarMenuButton>
              </DropdownMenuTrigger>
              <DropdownMenuContent
                className="w-[--radix-dropdown-menu-trigger-width] min-w-56 rounded-lg"
                align="start"
                side="bottom"
                sideOffset={4}
              >
                <DropdownMenuLabel className="text-xs text-muted-foreground">
                  Espacios de trabajo
                </DropdownMenuLabel>
                {isLoadingWorkspaces ? (
                  <DropdownMenuItem disabled className="gap-2 p-2">
                    Cargando espacios...
                  </DropdownMenuItem>
                ) : workspaces.length === 0 ? (
                  <DropdownMenuItem disabled className="gap-2 p-2">
                    No hay espacios disponibles
                  </DropdownMenuItem>
                ) : (
                  workspaces.map((workspace) => (
                    <DropdownMenuItem
                      key={workspace.id}
                      onClick={() => setCurrentWorkspace({
                        id: workspace.id,
                        nombre: workspace.nombre,
                        saldo: workspace.saldo,
                        usuarioAdmin: { 
                          id: workspace.usuarioAdminId,
                          nombre: '',
                          email: ''
                        }
                      })}
                      className="gap-2 p-2"
                    >
                      <div className="flex size-6 items-center justify-center rounded-sm border">
                        {workspace.nombre.charAt(0).toUpperCase()}
                      </div>
                      <div className="flex flex-col">
                        <span>{workspace.nombre}</span>
                        <span className="text-xs text-muted-foreground">${workspace.saldo.toFixed(2)}</span>
                      </div>
                    </DropdownMenuItem>
                  ))
                )}
                <DropdownMenuSeparator />
                <DropdownMenuItem className="gap-2 p-2" onClick={() => navigate('/configuracion')}>
                  <div className="flex size-6 items-center justify-center rounded-md border bg-background">
                    <Plus className="size-4" />
                  </div>
                  <div className="font-medium text-muted-foreground">
                    Nuevo espacio
                  </div>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupContent>
            <SidebarMenu>
              <SidebarSeparator className="my-2" />

              {/* Botón Nuevo Registro */}
              <SidebarMenuItem>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <SidebarMenuButton
                      size="default"
                      className="bg-sidebar-primary text-sidebar-primary-foreground hover:bg-sidebar-primary hover:text-sidebar-primary-foreground data-[state=open]:bg-sidebar-primary data-[state=open]:text-sidebar-primary-foreground disabled:opacity-50 disabled:cursor-not-allowed"
                      tooltip={currentWorkspace ? "Nuevo registro" : "Selecciona un espacio de trabajo"}
                      disabled={!currentWorkspace}
                    >
                      <Plus className="size-4" />
                      <span className="font-semibold">Nuevo registro</span>
                    </SidebarMenuButton>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent
                    className="w-[--radix-dropdown-menu-trigger-width] min-w-56 rounded-lg"
                    align="start"
                    side="bottom"
                    sideOffset={4}
                  >
                    <DropdownMenuLabel className="text-xs text-muted-foreground">
                      Tipo de registro
                    </DropdownMenuLabel>
                    <DropdownMenuItem
                      className="gap-2 p-2"
                      onClick={() => setTransactionModalOpen(true)}
                    >
                      <TrendingDown className="size-4 text-red-500" />
                      <div className="flex flex-col">
                        <span className="font-medium">Gastos/Ingresos</span>
                        <span className="text-xs text-muted-foreground">
                          Registrar transacción
                        </span>
                      </div>
                    </DropdownMenuItem>
                    <DropdownMenuItem 
                      className="gap-2 p-2"
                      onClick={() => setAccountTransferModalOpen(true)}
                    >
                      <ArrowRightLeft className="size-4" />
                      <div className="flex flex-col">
                        <span className="font-medium">Movimiento entre cuentas</span>
                        <span className="text-xs text-muted-foreground">
                          Transferir dinero
                        </span>
                      </div>
                    </DropdownMenuItem>
                    <DropdownMenuItem 
                      className="gap-2 p-2"
                      onClick={() => setCreditPurchaseModalOpen(true)}
                    >
                      <CreditCard className="size-4" />
                      <div className="flex flex-col">
                        <span className="font-medium">Compra con crédito</span>
                        <span className="text-xs text-muted-foreground">
                          Nueva compra en cuotas
                        </span>
                      </div>
                    </DropdownMenuItem>
                    <DropdownMenuItem 
                      className="gap-2 p-2"
                      onClick={() => setCardPaymentModalOpen(true)}
                    >
                      <Receipt className="size-4" />
                      <div className="flex flex-col">
                        <span className="font-medium">Pagar resumen tarjeta</span>
                        <span className="text-xs text-muted-foreground">
                          Liquidar resumen mensual
                        </span>
                      </div>
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </SidebarMenuItem>

              <SidebarSeparator className="my-2" />

              {/* Navegación Principal */}
              {navigation.map((item) => {
                const isActive = location.pathname === item.href
                return (
                  <SidebarMenuItem key={item.name}>
                    <SidebarMenuButton asChild isActive={isActive} tooltip={item.name}>
                      <Link to={item.href}>
                        <item.icon />
                        <span>{item.name}</span>
                      </Link>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                )
              })}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarFooter>
        {/* User Profile */}
        <SidebarMenu>
          <SidebarMenuItem>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <SidebarMenuButton
                  size="lg"
                  className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                >
                  <Avatar className="h-8 w-8 rounded-lg">
                    <AvatarImage src={user?.fotoPerfil} alt={user?.nombre} />
                    <AvatarFallback>{getUserInitials()}</AvatarFallback>
                  </Avatar>
                  <div className="grid flex-1 text-left text-sm leading-tight">
                    <span className="truncate font-semibold">{user?.nombre || 'Usuario'}</span>
                    <span className="truncate text-xs">{user?.email || 'cargando...'}</span>
                  </div>
                  <ChevronsUpDown className="ml-auto size-4" />
                </SidebarMenuButton>
              </DropdownMenuTrigger>
              <DropdownMenuContent
                className="w-[--radix-dropdown-menu-trigger-width] min-w-56 rounded-lg"
                side="bottom"
                align="end"
                sideOffset={4}
              >
                <DropdownMenuLabel className="p-0 font-normal">
                  <div className="flex items-center gap-2 px-1 py-1.5 text-left text-sm">
                    <Avatar className="h-8 w-8 rounded-lg">
                      <AvatarImage src={user?.fotoPerfil} alt={user?.nombre} />
                      <AvatarFallback>{getUserInitials()}</AvatarFallback>
                    </Avatar>
                    <div className="grid flex-1 text-left text-sm leading-tight">
                      <span className="truncate font-semibold">{user?.nombre || 'Usuario'}</span>
                      <span className="truncate text-xs">{user?.email || 'cargando...'}</span>
                    </div>
                  </div>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={() => navigate('/configuracion')}>
                  <User className="mr-2 h-4 w-4" />
                  Perfil
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => navigate('/configuracion')}>
                  <Settings className="mr-2 h-4 w-4" />
                  Configuración
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={logout} className="text-destructive focus:text-destructive">
                  <LogOut className="mr-2 h-4 w-4" />
                  Cerrar sesión
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>
      <SidebarRail />
    </Sidebar>

    <TransactionModal
      open={transactionModalOpen}
      onOpenChange={setTransactionModalOpen}
    />
    <AccountTransferModal
      open={accountTransferModalOpen}
      onOpenChange={setAccountTransferModalOpen}
    />
    <CreditPurchaseModal
      open={creditPurchaseModalOpen}
      onOpenChange={setCreditPurchaseModalOpen}
    />
    <CardPaymentModal
      open={cardPaymentModalOpen}
      onOpenChange={setCardPaymentModalOpen}
    />
    </>
  )
}
