import { useState, useEffect } from 'react'
import { useAppStore } from '@/store/app-store'
import { espacioTrabajoService } from '@/services/espacio-trabajo.service'
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
  const usuario = useAppStore((state) => state.user)
  const espaciosDeTrabajo = useAppStore((state) => state.workspaces)
  const [miembros, setMiembros] = useState<MiembroEspacio[]>([])
  const [isInviteDialogOpen, setIsInviteDialogOpen] = useState(false)
  const [inviteEmail, setInviteEmail] = useState('')
  const [selectedEspacioId, setSelectedEspacioId] = useState<string>(espacioActual?.id.toString() || '')
  const [isLoading, setIsLoading] = useState(false)
  const [nombreEspacio, setNombreEspacio] = useState(espacioActual?.nombre || '')

  useEffect(() => {
    if (espacioActual) {
      setNombreEspacio(espacioActual.nombre)
      setSelectedEspacioId(espacioActual.id.toString())
      loadMiembros()
    }
  }, [espacioActual])

  const loadMiembros = async () => {
    if (!espacioActual) return
    try {
      const data = await espacioTrabajoService.getMiembros(espacioActual.id)
      setMiembros(data)
    } catch (error) {
      console.error('Error al cargar miembros:', error)
    }
  }

  const handleInviteMember = async () => {
    if (!selectedEspacioId || !inviteEmail) return
    
    setIsLoading(true)
    try {
      await espacioTrabajoService.invitarMiembro({
        email: inviteEmail,
        rol: 'EDITOR' as RolMiembro,
        espacioTrabajoId: parseInt(selectedEspacioId),
      })
      await loadMiembros()
      setIsInviteDialogOpen(false)
      setInviteEmail('')
      setSelectedEspacioId(espacioActual?.id.toString() || '')
    } catch (error) {
      console.error('Error al invitar miembro:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const handleRemoveMember = async (miembroId: number) => {
    if (!espacioActual) return
    
    try {
      await espacioTrabajoService.eliminarMiembro(espacioActual.id, miembroId)
      await loadMiembros()
    } catch (error) {
      console.error('Error al eliminar miembro:', error)
    }
  }

  const handleUpdateWorkspaceName = async () => {
    if (!espacioActual || nombreEspacio === espacioActual.nombre) return
    
    try {
      await espacioTrabajoService.update(espacioActual.id, {
        nombre: nombreEspacio,
        saldo: espacioActual.saldo,
      })
    } catch (error) {
      console.error('Error al actualizar nombre:', error)
    }
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
                    disabled={!isAdmin}
                  />
                  <Button
                    onClick={handleUpdateWorkspaceName}
                    disabled={!isAdmin || nombreEspacio === espacioActual?.nombre}
                  >
                    Guardar
                  </Button>
                </div>
                {!isAdmin && (
                  <p className="text-sm text-muted-foreground">
                    Solo el administrador puede cambiar el nombre del espacio
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
                      <DialogTitle>Invitar nuevo miembro</DialogTitle>
                      <DialogDescription>
                        Añade un miembro a tu espacio de trabajo. Recibirá una invitación por correo.
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
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="workspace">Espacio de trabajo</Label>
                        <Select
                          value={selectedEspacioId}
                          onValueChange={setSelectedEspacioId}
                        >
                          <SelectTrigger id="workspace">
                            <SelectValue placeholder="Seleccionar espacio" />
                          </SelectTrigger>
                          <SelectContent>
                            {espaciosDeTrabajo.map((espacio) => (
                              <SelectItem key={espacio.id} value={espacio.id.toString()}>
                                {espacio.nombre}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                        <p className="text-sm text-muted-foreground">
                          Selecciona el espacio de trabajo al que quieres invitar a este miembro.
                        </p>
                      </div>
                    </div>
                    <DialogFooter>
                      <Button
                        variant="outline"
                        onClick={() => setIsInviteDialogOpen(false)}
                      >
                        Cancelar
                      </Button>
                      <Button
                        onClick={handleInviteMember}
                        disabled={isLoading || !inviteEmail || !selectedEspacioId}
                      >
                        {isLoading ? 'Enviando...' : 'Enviar Invitación'}
                      </Button>
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
              </div>
            </CardHeader>
            <CardContent>
              {miembros.length === 0 ? (
                <div className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-zinc-800 bg-zinc-950/50 p-12 text-center">
                  <div className="flex items-center gap-2 mb-4">
                    <div className="text-4xl">🧠</div>
                    <div className="text-4xl">💼</div>
                    <div className="text-4xl">🐇</div>
                  </div>
                  <h3 className="text-xl font-semibold mb-2">Sin miembros del equipo</h3>
                  <p className="text-muted-foreground mb-4 max-w-sm">
                    Invita a tu equipo para colaborar en este proyecto.
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
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Miembro</TableHead>
                      <TableHead>Correo</TableHead>
                      <TableHead>Rol</TableHead>
                      <TableHead>Fecha de ingreso</TableHead>
                      <TableHead className="text-right">Acciones</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {miembros.map((miembro) => (
                      <TableRow key={miembro.id}>
                        <TableCell className="flex items-center gap-3">
                          <Avatar className="h-8 w-8">
                            <AvatarImage src={miembro.usuario.picture} />
                            <AvatarFallback>
                              {getInitials(miembro.usuario.nombre)}
                            </AvatarFallback>
                          </Avatar>
                          <span className="font-medium">{miembro.usuario.nombre}</span>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <Mail className="h-4 w-4 text-muted-foreground" />
                            {miembro.usuario.email}
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge variant={getRolBadgeVariant(miembro.rol)}>
                            {miembro.rol === 'ADMIN' && <Shield className="mr-1 h-3 w-3" />}
                            {miembro.rol}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          {new Date(miembro.fechaIngreso).toLocaleDateString('es-ES')}
                        </TableCell>
                        <TableCell className="text-right">
                          {isAdmin && miembro.usuario.id !== usuario?.id && (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleRemoveMember(miembro.id)}
                            >
                              <Trash2 className="h-4 w-4 text-destructive" />
                            </Button>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
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
                  <AvatarImage src={usuario?.picture} />
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
