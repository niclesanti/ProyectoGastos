import { useState, useEffect } from 'react'
import { useAppStore } from '@/store/app-store'
import { useAuth } from '@/contexts/AuthContext'
import { espacioTrabajoService } from '@/services/espacio-trabajo.service'
import { useCreateWorkspace, useShareWorkspace } from '@/features/workspaces/api/workspace-queries'
import type { MiembroEspacio, RolMiembro } from '@/types'
import {
  Users,
  Landmark,
  Bell,
  User,
  UserPlus,
  Trash2,
  Shield,
  Mail,
} from 'lucide-react'

import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'

export function ConfiguracionPage() {
  const espacioActual = useAppStore((state) => state.currentWorkspace)
  const { user: usuario } = useAuth()
  
  // Hooks de TanStack Query para espacios de trabajo
  const createWorkspaceMutation = useCreateWorkspace()
  const shareWorkspaceMutation = useShareWorkspace()
  
  const [miembros, setMiembros] = useState<MiembroEspacio[]>([])
  const [isInviteDialogOpen, setIsInviteDialogOpen] = useState(false)
  const [inviteEmail, setInviteEmail] = useState('')
  const [nombreEspacio, setNombreEspacio] = useState('')
  const [isLoadingMiembros, setIsLoadingMiembros] = useState(false)
  const [errorMiembros, setErrorMiembros] = useState<string | null>(null)

  useEffect(() => {
    if (espacioActual) {
      loadMiembros()
    }
  }, [espacioActual])

  const loadMiembros = async () => {
    if (!espacioActual) return
    
    setIsLoadingMiembros(true)
    setErrorMiembros(null)
    
    try {
      const data = await espacioTrabajoService.getMiembros(espacioActual.id)
      setMiembros(data)
      console.log('Miembros cargados:', data)
    } catch (error: any) {
      console.error('Error al cargar miembros:', error)
      setErrorMiembros(error?.message || 'No se pudieron cargar los miembros')
      // Por ahora, si falla, dejamos la lista vacía
      setMiembros([])
    } finally {
      setIsLoadingMiembros(false)
    }
  }

  const handleShareWorkspace = async () => {
    if (!inviteEmail || !usuario?.id || !espacioActual?.id) {
      console.log('Validación falló:', { inviteEmail, usuarioId: usuario?.id, espacioActualId: espacioActual?.id })
      return
    }
    
    shareWorkspaceMutation.mutate(
      {
        email: inviteEmail,
        idEspacioTrabajo: espacioActual.id,
        idUsuarioAdmin: usuario.id,
      },
      {
        onSuccess: () => {
          console.log('Espacio compartido exitosamente')
          setIsInviteDialogOpen(false)
          setInviteEmail('')
          // Recargar miembros después de compartir
          loadMiembros()
        },
        onError: (error) => {
          console.error('Error al compartir espacio:', error)
        },
      }
    )
  }

  const handleRemoveMember = async (miembroId: number) => {
    if (!espacioActual) return
    
    try {
      // TODO: Implementar endpoint de eliminación de miembro
      console.log('Eliminar miembro:', miembroId)
      // await espacioTrabajoService.eliminarMiembro(espacioActual.id, miembroId)
      // await loadMiembros()
    } catch (error) {
      console.error('Error al eliminar miembro:', error)
    }
  }

  const handleCreateWorkspace = async () => {
    console.log('handleCreateWorkspace llamado', { nombreEspacio, usuario })
    
    if (!nombreEspacio.trim() || !usuario?.id) {
      console.log('Validación falló:', { nombreEspacio: nombreEspacio.trim(), usuarioId: usuario?.id })
      return
    }
    
    console.log('Ejecutando mutation con:', { nombre: nombreEspacio.trim(), idUsuarioAdmin: usuario.id })
    
    createWorkspaceMutation.mutate(
      {
        nombre: nombreEspacio.trim(),
        idUsuarioAdmin: usuario.id,
      },
      {
        onSuccess: () => {
          console.log('Espacio de trabajo creado exitosamente')
          setNombreEspacio('') // Limpiar el input
        },
        onError: (error) => {
          console.error('Error al crear espacio de trabajo:', error)
        },
      }
    )
  }

  const isAdmin = espacioActual?.usuarioAdmin.id === usuario?.id

  const getRolBadgeVariant = (rol: RolMiembro) => {
    switch (rol) {
      case 'ADMIN':
        return 'default'
      case 'EDITOR':
        return 'secondary'
      default:
        return 'outline'
    }
  }

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2)
  }

  return (
    <div className="space-y-6 pt-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Configuración</h2>
        <p className="text-muted-foreground">
          Gestiona tu espacio de trabajo y preferencias
        </p>
      </div>

      <Tabs defaultValue="workspace" className="space-y-4">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="workspace" className="gap-2">
            <Users className="h-4 w-4" />
            <span className="hidden sm:inline">Espacio de trabajo</span>
          </TabsTrigger>
          <TabsTrigger value="financial" className="gap-2" disabled>
            <Landmark className="h-4 w-4" />
            <span className="hidden sm:inline">Preferencias</span>
          </TabsTrigger>
          <TabsTrigger value="notifications" className="gap-2" disabled>
            <Bell className="h-4 w-4" />
            <span className="hidden sm:inline">Notificaciones</span>
          </TabsTrigger>
          <TabsTrigger value="account" className="gap-2" disabled>
            <User className="h-4 w-4" />
            <span className="hidden sm:inline">Cuenta</span>
          </TabsTrigger>
        </TabsList>

        {/* WORKSPACE TAB */}
        <TabsContent value="workspace" className="space-y-4">
          {/* Información del Espacio */}
          <Card>
            <CardHeader>
              <CardTitle>Nuevo espacio de trabajo</CardTitle>
              <CardDescription>
                Crea y personaliza el nombre de tu espacio de trabajo
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="workspace-name">Nombre del espacio</Label>
                <div className="flex gap-2">
                  <Input
                    id="workspace-name"
                    value={nombreEspacio}
                    onChange={(e) => setNombreEspacio(e.target.value)}
                    placeholder="Mi Espacio de Trabajo"
                    disabled={createWorkspaceMutation.isPending}
                  />
                  <Button
                    onClick={handleCreateWorkspace}
                    disabled={!nombreEspacio.trim() || createWorkspaceMutation.isPending}
                  >
                    {createWorkspaceMutation.isPending ? 'Guardando...' : 'Guardar'}
                  </Button>
                </div>
                {createWorkspaceMutation.isError && (
                  <p className="text-sm text-destructive">
                    Error al crear el espacio de trabajo. Intenta nuevamente.
                  </p>
                )}
                {createWorkspaceMutation.isSuccess && (
                  <p className="text-sm text-green-500">
                    ¡Espacio de trabajo creado exitosamente!
                  </p>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Gestión de Miembros */}
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>Miembros del equipo</CardTitle>
                  <CardDescription>
                    Invita personas para colaborar en este espacio
                  </CardDescription>
                </div>
                <Dialog open={isInviteDialogOpen} onOpenChange={setIsInviteDialogOpen}>
                  <DialogTrigger asChild>
                    <Button disabled={!isAdmin}>
                      <UserPlus className="mr-2 h-4 w-4" />
                      Invitar miembros
                    </Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Compartir espacio de trabajo</DialogTitle>
                      <DialogDescription>
                        Comparte "{espacioActual?.nombre}" con otra persona por correo electrónico.
                      </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4 py-4">
                      <div className="space-y-2">
                        <Label htmlFor="email">Correo electrónico</Label>
                        <Input
                          id="email"
                          type="email"
                          placeholder="usuario@ejemplo.com"
                          value={inviteEmail}
                          onChange={(e) => setInviteEmail(e.target.value)}
                          disabled={shareWorkspaceMutation.isPending}
                        />
                        <p className="text-sm text-muted-foreground">
                          La persona recibirá acceso al espacio "{espacioActual?.nombre}".
                        </p>
                        {shareWorkspaceMutation.isError && (
                          <p className="text-sm text-destructive">
                            Error al compartir el espacio. Intenta nuevamente.
                          </p>
                        )}
                      </div>
                    </div>
                    <DialogFooter>
                      <Button
                        variant="outline"
                        onClick={() => setIsInviteDialogOpen(false)}
                        disabled={shareWorkspaceMutation.isPending}
                      >
                        Cancelar
                      </Button>
                      <Button
                        onClick={handleShareWorkspace}
                        disabled={shareWorkspaceMutation.isPending || !inviteEmail.trim()}
                      >
                        {shareWorkspaceMutation.isPending ? 'Compartiendo...' : 'Compartir Espacio'}
                      </Button>
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
              </div>
            </CardHeader>
            <CardContent>
              {isLoadingMiembros ? (
                <div className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-zinc-800 bg-zinc-950/50 p-12 text-center">
                  <div className="text-4xl mb-4">⏳</div>
                  <h3 className="text-xl font-semibold mb-2">Cargando miembros...</h3>
                  <p className="text-muted-foreground max-w-sm">
                    Obteniendo la información del equipo.
                  </p>
                </div>
              ) : errorMiembros ? (
                <div className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-zinc-800 bg-zinc-950/50 p-12 text-center">
                  <div className="text-4xl mb-4">⚠️</div>
                  <h3 className="text-xl font-semibold mb-2">Error al cargar miembros</h3>
                  <p className="text-muted-foreground mb-4 max-w-sm">
                    No se pudieron cargar los miembros del equipo. Intenta recargar la página.
                  </p>
                  <Button onClick={loadMiembros} variant="outline">
                    Reintentar
                  </Button>
                </div>
              ) : miembros.length === 0 ? (
                <div className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-zinc-800 bg-zinc-950/50 p-12 text-center">
                  <div className="flex items-center gap-2 mb-4">
                    <div className="text-4xl">🧠</div>
                    <div className="text-4xl">💼</div>
                    <div className="text-4xl">🐇</div>
                  </div>
                  <h3 className="text-xl font-semibold mb-2">Aún no hay colaboradores</h3>
                  <p className="text-muted-foreground mb-4 max-w-sm">
                    Este espacio solo tiene al administrador. Invita a tu equipo para comenzar a colaborar.
                  </p>
                  <Button
                    onClick={() => setIsInviteDialogOpen(true)}
                    disabled={!isAdmin}
                  >
                    <UserPlus className="mr-2 h-4 w-4" />
                    Invitar miembros
                  </Button>
                </div>
              ) : (
                <div className="space-y-6">
                  {/* Resumen visual de miembros */}
                  <div className="flex items-center justify-between p-4 rounded-lg border border-zinc-800 bg-zinc-950/30">
                    <div className="flex items-center gap-4">
                      <div className="flex -space-x-2">
                        {miembros.slice(0, 5).map((miembro, index) => (
                          <Avatar 
                            key={miembro.id} 
                            className="h-10 w-10 border-2 border-zinc-900"
                            style={{ zIndex: 5 - index }}
                          >
                            <AvatarImage src={miembro.fotoPerfil} alt={miembro.nombre} />
                            <AvatarFallback className="bg-gradient-to-br from-blue-500 to-purple-600 text-white text-sm">
                              {miembro.nombre?.substring(0, 2).toUpperCase()}
                            </AvatarFallback>
                          </Avatar>
                        ))}
                        {miembros.length > 5 && (
                          <div className="h-10 w-10 rounded-full border-2 border-zinc-900 bg-zinc-800 flex items-center justify-center text-sm font-semibold">
                            +{miembros.length - 5}
                          </div>
                        )}
                      </div>
                      <div>
                        <p className="text-sm font-semibold">
                          {miembros.length} {miembros.length === 1 ? 'miembro' : 'miembros'}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          en este espacio de trabajo
                        </p>
                      </div>
                    </div>
                    <Button
                      onClick={() => setIsInviteDialogOpen(true)}
                      disabled={!isAdmin}
                      size="sm"
                    >
                      <UserPlus className="mr-2 h-4 w-4" />
                      Invitar más
                    </Button>
                  </div>

                  {/* Lista de miembros */}
                  <div className="space-y-2">
                    {miembros.map((miembro) => (
                      <div
                        key={miembro.id}
                        className="flex items-center justify-between p-3 rounded-lg border border-zinc-800 hover:bg-zinc-950/50 transition-colors"
                      >
                        <div className="flex items-center gap-3">
                          <Avatar className="h-9 w-9">
                            <AvatarImage src={miembro.fotoPerfil} alt={miembro.nombre} />
                            <AvatarFallback className="bg-gradient-to-br from-blue-500 to-purple-600 text-white text-xs">
                              {miembro.nombre?.substring(0, 2).toUpperCase()}
                            </AvatarFallback>
                          </Avatar>
                          <div>
                            <p className="font-medium text-sm">{miembro.nombre}</p>
                            <p className="text-xs text-muted-foreground">{miembro.email}</p>
                          </div>
                        </div>
                        {isAdmin && miembro.id !== usuario?.id && (
                          <Button
                            variant="ghost"
                            size="sm"
                            className="text-destructive hover:text-destructive hover:bg-destructive/10"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* FINANCIAL PREFERENCES TAB */}
        <TabsContent value="financial" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Preferencias financieras</CardTitle>
              <CardDescription>
                Configura tu moneda base y categorías (próximamente)
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="currency">Moneda base</Label>
                <Select disabled>
                  <SelectTrigger id="currency">
                    <SelectValue placeholder="ARS - Peso Argentino" />
                  </SelectTrigger>
                </Select>
              </div>
              <Separator />
              <div className="space-y-4">
                <h4 className="text-sm font-medium">Categorías personalizadas</h4>
                <p className="text-sm text-muted-foreground">
                  Funcionalidad próximamente disponible
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* NOTIFICATIONS TAB */}
        <TabsContent value="notifications" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Notificaciones y alertas</CardTitle>
              <CardDescription>
                Configura recordatorios y alertas (próximamente)
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Recordatorios de gastos diarios</Label>
                  <p className="text-sm text-muted-foreground">
                    Recibe un resumen diario de tus movimientos
                  </p>
                </div>
                <Switch disabled />
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Alertas de vencimiento de tarjetas</Label>
                  <p className="text-sm text-muted-foreground">
                    Te notificaremos antes de las fechas de vencimiento
                  </p>
                </div>
                <Switch disabled />
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* ACCOUNT TAB */}
        <TabsContent value="account" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Información de cuenta</CardTitle>
              <CardDescription>
                Tu perfil y configuración de datos
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="flex items-center gap-4">
                <Avatar className="h-16 w-16">
                  <AvatarImage src={usuario?.fotoPerfil} />
                  <AvatarFallback>
                    {usuario ? getInitials(usuario.nombre) : 'U'}
                  </AvatarFallback>
                </Avatar>
                <div>
                  <h4 className="text-lg font-semibold">{usuario?.nombre}</h4>
                  <p className="text-sm text-muted-foreground">{usuario?.email}</p>
                  <Badge variant="outline" className="mt-2">
                    Autenticado con Google
                  </Badge>
                </div>
              </div>
              <Separator />
              <div className="space-y-4">
                <h4 className="text-sm font-medium">Gestión de datos</h4>
                <div className="flex gap-2">
                  <Button variant="outline" disabled>
                    Exportar datos a CSV
                  </Button>
                  <Button variant="destructive" disabled>
                    Eliminar espacio de trabajo
                  </Button>
                </div>
                <p className="text-sm text-muted-foreground">
                  Funcionalidad próximamente disponible
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
