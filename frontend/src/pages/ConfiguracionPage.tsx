import { useState, useEffect } from 'react'
import { useAppStore } from '@/store/app-store'
import { useAuth } from '@/contexts/AuthContext'
import { espacioTrabajoService } from '@/services/espacio-trabajo.service'
import { 
  useCreateWorkspace, 
  useShareWorkspace,
  useSolicitudesPendientes,
  useResponderSolicitud,
} from '@/features/workspaces/api/workspace-queries'
import type { MiembroEspacio } from '@/types'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { toast } from 'sonner'
import {
  Users,
  Landmark,
  Bell,
  User,
  UserPlus,
  Trash2,
  Check,
  X,
  Mail,
  Clock,
} from 'lucide-react'

import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'

// Schema de validación para crear espacio de trabajo
const createWorkspaceSchema = z.object({
  nombre: z
    .string()
    .min(1, { message: 'Por favor, ingresa el nombre del espacio de trabajo.' })
    .max(50, { message: 'El nombre no puede exceder los 50 caracteres.' })
    .regex(
      /^[a-zA-Z0-9,()\\-_/\s]+$/,
      { message: 'Solo se permiten letras, números, coma, paréntesis, guiones, barra y espacios.' }
    ),
})

// Schema de validación para compartir espacio de trabajo
const shareWorkspaceSchema = z.object({
  email: z
    .string()
    .min(1, { message: 'Por favor, ingresa un correo electrónico.' })
    .max(100, { message: 'El correo no puede exceder los 100 caracteres.' })
    .email({ message: 'Debe proporcionar un email válido.' })
    .regex(
      /^[a-zA-Z0-9@.\-_]+$/,
      { message: 'El email solo puede contener letras, números, @, punto, guiones y barra baja.' }
    ),
})

type CreateWorkspaceFormValues = z.infer<typeof createWorkspaceSchema>
type ShareWorkspaceFormValues = z.infer<typeof shareWorkspaceSchema>

export function ConfiguracionPage() {
  const espacioActual = useAppStore((state) => state.currentWorkspace)
  const { user: usuario } = useAuth()
  
  // Hooks de TanStack Query para espacios de trabajo
  const createWorkspaceMutation = useCreateWorkspace()
  const shareWorkspaceMutation = useShareWorkspace()
  const { data: solicitudesPendientes, isLoading: isLoadingSolicitudes, error: errorSolicitudes, refetch: refetchSolicitudes } = useSolicitudesPendientes()
  const responderSolicitudMutation = useResponderSolicitud()
  
  const [miembros, setMiembros] = useState<MiembroEspacio[]>([])
  const [isInviteDialogOpen, setIsInviteDialogOpen] = useState(false)
  const [isLoadingMiembros, setIsLoadingMiembros] = useState(false)
  const [errorMiembros, setErrorMiembros] = useState<string | null>(null)
  
  // Estado para paginación de solicitudes
  const [currentPageSolicitudes, setCurrentPageSolicitudes] = useState(1)
  const solicitudesPorPagina = 4

  // Form para crear espacio de trabajo
  const createWorkspaceForm = useForm<CreateWorkspaceFormValues>({
    resolver: zodResolver(createWorkspaceSchema),
    defaultValues: {
      nombre: '',
    },
  })

  // Form para compartir espacio de trabajo
  const shareWorkspaceForm = useForm<ShareWorkspaceFormValues>({
    resolver: zodResolver(shareWorkspaceSchema),
    defaultValues: {
      email: '',
    },
  })

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
      setMiembros([])
    } finally {
      setIsLoadingMiembros(false)
    }
  }

  const onCreateWorkspace = async (values: CreateWorkspaceFormValues) => {
    if (!usuario?.id) {
      toast.error('Error de autenticación', {
        description: 'No se pudo identificar el usuario. Por favor, vuelve a iniciar sesión.',
      })
      return
    }

    createWorkspaceMutation.mutate(
      {
        nombre: values.nombre.trim(),
        idUsuarioAdmin: usuario.id,
      },
      {
        onSuccess: () => {
          toast.success('¡Espacio creado exitosamente!', {
            description: `El espacio "${values.nombre}" ha sido creado correctamente.`,
          })
          createWorkspaceForm.reset()
        },
        onError: (error: any) => {
          console.error('Error al crear espacio de trabajo:', error)
          toast.error('Error al crear el espacio de trabajo', {
            description: error?.message || 'Intenta nuevamente o contacta al soporte.',
          })
        },
      }
    )
  }

  const onShareWorkspace = async (values: ShareWorkspaceFormValues) => {
    if (!usuario?.id || !espacioActual?.id) {
      toast.error('Error de configuración', {
        description: 'No se pudo identificar el espacio o usuario. Intenta recargar la página.',
      })
      return
    }

    shareWorkspaceMutation.mutate(
      {
        email: values.email,
        idEspacioTrabajo: espacioActual.id,
      },
      {
        onSuccess: () => {
          toast.success('¡Invitación enviada!', {
            description: `Se ha compartido el espacio con ${values.email}.`,
          })
          setIsInviteDialogOpen(false)
          shareWorkspaceForm.reset()
          loadMiembros()
        },
        onError: (error: any) => {
          console.error('Error al compartir espacio:', error)
          toast.error('Error al compartir el espacio', {
            description: error?.message || 'Verifica el correo e intenta nuevamente.',
          })
        },
      }
    )
  }

  const isAdmin = espacioActual?.usuarioAdmin.id === usuario?.id

  // Calcular paginación para solicitudes
  const totalSolicitudes = solicitudesPendientes?.length || 0
  const totalPaginasSolicitudes = Math.ceil(totalSolicitudes / solicitudesPorPagina)
  const indexOfLastSolicitud = currentPageSolicitudes * solicitudesPorPagina
  const indexOfFirstSolicitud = indexOfLastSolicitud - solicitudesPorPagina
  const solicitudesActuales = solicitudesPendientes?.slice(indexOfFirstSolicitud, indexOfLastSolicitud) || []

  // Handlers para solicitudes
  const handleAceptarSolicitud = async (idSolicitud: number, nombreEspacio: string) => {
    responderSolicitudMutation.mutate(
      { idSolicitud, aceptada: true },
      {
        onSuccess: () => {
          toast.success('¡Solicitud aceptada!', {
            description: `Ahora eres parte del espacio "${nombreEspacio}".`,
          })
          setCurrentPageSolicitudes(1) // Resetear a primera página
        },
        onError: (error: any) => {
          console.error('Error al aceptar solicitud:', error)
          toast.error('Error al aceptar la solicitud', {
            description: error?.message || 'Intenta nuevamente.',
          })
        },
      }
    )
  }

  const handleRechazarSolicitud = async (idSolicitud: number, nombreEspacio: string) => {
    responderSolicitudMutation.mutate(
      { idSolicitud, aceptada: false },
      {
        onSuccess: () => {
          toast.info('Solicitud rechazada', {
            description: `Has rechazado la invitación a "${nombreEspacio}".`,
          })
          setCurrentPageSolicitudes(1) // Resetear a primera página
        },
        onError: (error: any) => {
          console.error('Error al rechazar solicitud:', error)
          toast.error('Error al rechazar la solicitud', {
            description: error?.message || 'Intenta nuevamente.',
          })
        },
      }
    )
  }

  // Función para filtrar caracteres permitidos en el nombre del espacio
  const filterWorkspaceName = (value: string): string => {
    // Solo permite: letras, números, coma, paréntesis, guiones, barra y espacios
    return value.replace(/[^a-zA-Z0-9,()\-_/\s]/g, '')
  }

  // Función para filtrar caracteres permitidos en emails
  const filterEmailChars = (value: string): string => {
    // Solo permite: letras, números, @, punto, guiones y barra baja
    return value.replace(/[^a-zA-Z0-9@.\-_]/g, '')
  }

  // Estas funciones están comentadas porque actualmente no se usan en la UI
  // Se pueden habilitar cuando se implemente la funcionalidad completa de gestión de miembros
  
  // const handleRemoveMember = async (miembroId: number) => {
  //   if (!espacioActual) return
  //   
  //   try {
  //     console.log('Eliminar miembro:', miembroId)
  //     toast.info('Función en desarrollo', {
  //       description: 'La eliminación de miembros estará disponible próximamente.',
  //     })
  //     // await espacioTrabajoService.eliminarMiembro(espacioActual.id, miembroId)
  //     // await loadMiembros()
  //   } catch (error) {
  //     console.error('Error al eliminar miembro:', error)
  //     toast.error('Error al eliminar miembro', {
  //       description: 'No se pudo eliminar el miembro. Intenta nuevamente.',
  //     })
  //   }
  // }

  // const getRolBadgeVariant = (rol: RolMiembro) => {
  //   switch (rol) {
  //     case 'ADMIN':
  //       return 'default'
  //     case 'EDITOR':
  //       return 'secondary'
  //     default:
  //       return 'outline'
  //   }
  // }

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2)
  }

  return (
    <div className="space-y-4 sm:space-y-6 pt-4 sm:pt-6">
      <div>
        <h2 className="text-2xl sm:text-3xl font-bold tracking-tight">Configuración</h2>
        <p className="text-sm sm:text-base text-muted-foreground">
          Gestiona tu espacio de trabajo y preferencias
        </p>
      </div>

      <Tabs defaultValue="workspace" className="space-y-4">
        <TabsList className="grid w-full grid-cols-4 text-xs sm:text-sm">
          <TabsTrigger value="workspace" className="gap-1 sm:gap-2 px-2 sm:px-4">
            <Users className="h-3 w-3 sm:h-4 sm:w-4" />
            <span className="hidden sm:inline">Espacio de trabajo</span>
            <span className="sm:hidden">Espacio</span>
          </TabsTrigger>
          <TabsTrigger value="financial" className="gap-1 sm:gap-2 px-2 sm:px-4" disabled>
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
            <CardContent>
              <Form {...createWorkspaceForm}>
                <form onSubmit={createWorkspaceForm.handleSubmit(onCreateWorkspace)} className="space-y-4">
                  <FormField
                    control={createWorkspaceForm.control}
                    name="nombre"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Nombre del espacio</FormLabel>
                        <div className="flex flex-col sm:flex-row gap-2">
                          <FormControl>
                            <Input
                              placeholder="Mi Espacio de Trabajo"
                              {...field}
                              onChange={(e) => {
                                const filteredValue = filterWorkspaceName(e.target.value)
                                field.onChange(filteredValue)
                              }}
                              disabled={createWorkspaceMutation.isPending}
                              maxLength={50}
                              className="w-full"
                            />
                          </FormControl>
                          <Button
                            type="submit"
                            disabled={createWorkspaceMutation.isPending || !field.value?.trim()}
                            className="w-full sm:w-auto"
                          >
                            {createWorkspaceMutation.isPending ? 'Guardando...' : 'Guardar'}
                          </Button>
                        </div>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </form>
              </Form>
            </CardContent>
          </Card>

          {/* Gestión de Miembros */}
          <Card>
            <CardHeader>
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
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
                  <DialogContent className="max-w-md max-h-[90vh] overflow-y-auto p-4 sm:p-6">
                    <DialogHeader className="space-y-2">
                      <DialogTitle className="text-lg sm:text-xl">Compartir espacio de trabajo</DialogTitle>
                      <DialogDescription className="text-sm">
                        Comparte "{espacioActual?.nombre}" con otra persona por correo electrónico.
                      </DialogDescription>
                    </DialogHeader>
                    <Form {...shareWorkspaceForm}>
                      <form onSubmit={shareWorkspaceForm.handleSubmit(onShareWorkspace)} className="space-y-4 py-4">
                        <FormField
                          control={shareWorkspaceForm.control}
                          name="email"
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>Correo electrónico</FormLabel>
                              <FormControl>
                                <Input
                                  type="email"
                                  placeholder="usuario@ejemplo.com"
                                  {...field}
                                  onChange={(e) => {
                                    const filteredValue = filterEmailChars(e.target.value)
                                    field.onChange(filteredValue)
                                  }}
                                  disabled={shareWorkspaceMutation.isPending}
                                  maxLength={100}
                                />
                              </FormControl>
                              <p className="text-sm text-muted-foreground">
                                La persona recibirá acceso al espacio "{espacioActual?.nombre}".
                              </p>
                              <FormMessage />
                            </FormItem>
                          )}
                        />
                        <DialogFooter className="flex-col sm:flex-row gap-2">
                          <Button
                            type="button"
                            variant="outline"
                            onClick={() => {
                              setIsInviteDialogOpen(false)
                              shareWorkspaceForm.reset()
                            }}
                            disabled={shareWorkspaceMutation.isPending}
                            className="w-full sm:w-auto"
                          >
                            Cancelar
                          </Button>
                          <Button
                            type="submit"
                            disabled={shareWorkspaceMutation.isPending}
                            className="w-full sm:w-auto"
                          >
                            {shareWorkspaceMutation.isPending ? 'Compartiendo...' : 'Compartir Espacio'}
                          </Button>
                        </DialogFooter>
                      </form>
                    </Form>
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

          {/* Solicitudes de Colaboración */}
          <Card>
            <CardHeader>
              <CardTitle>Solicitudes de colaboración</CardTitle>
              <CardDescription>
                Solicitudes para colaborar en un nuevo espacio de trabajo
              </CardDescription>
            </CardHeader>
            <CardContent>
              {isLoadingSolicitudes ? (
                <div className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-zinc-800 bg-zinc-950/50 p-12 text-center">
                  <div className="text-4xl mb-4">⏳</div>
                  <h3 className="text-xl font-semibold mb-2">Cargando solicitudes...</h3>
                  <p className="text-muted-foreground max-w-sm">
                    Obteniendo tus invitaciones pendientes.
                  </p>
                </div>
              ) : errorSolicitudes ? (
                <div className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-zinc-800 bg-zinc-950/50 p-12 text-center">
                  <div className="text-4xl mb-4">⚠️</div>
                  <h3 className="text-xl font-semibold mb-2">Error al cargar solicitudes</h3>
                  <p className="text-muted-foreground mb-4 max-w-sm">
                    No se pudieron cargar las solicitudes. Intenta recargar la página.
                  </p>
                  <Button onClick={() => refetchSolicitudes()} variant="outline">
                    Reintentar
                  </Button>
                </div>
              ) : totalSolicitudes === 0 ? (
                <div className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-zinc-800 bg-zinc-950/50 p-12 text-center">
                  <div className="text-4xl mb-4">📭</div>
                  <h3 className="text-xl font-semibold mb-2">No tienes solicitudes pendientes</h3>
                  <p className="text-muted-foreground max-w-sm">
                    Cuando alguien te invite a colaborar en su espacio, las invitaciones aparecerán aquí.
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  {/* Resumen de solicitudes */}
                  <div className="flex items-center justify-between p-4 rounded-lg border border-zinc-800 bg-zinc-950/30">
                    <div className="flex items-center gap-3">
                      <div className="h-10 w-10 rounded-full bg-blue-500/10 flex items-center justify-center">
                        <Mail className="h-5 w-5 text-blue-500" />
                      </div>
                      <div>
                        <p className="text-sm font-semibold">
                          {totalSolicitudes} {totalSolicitudes === 1 ? 'invitación pendiente' : 'invitaciones pendientes'}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          Revisa y responde a tus invitaciones
                        </p>
                      </div>
                    </div>
                    <Badge variant="secondary" className="gap-1">
                      <Clock className="h-3 w-3" />
                      Pendiente
                    </Badge>
                  </div>

                  {/* Lista de solicitudes paginadas */}
                  <div className="space-y-3">
                    {solicitudesActuales.map((solicitud) => (
                      <div
                        key={solicitud.id}
                        className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-lg border border-zinc-800 hover:bg-zinc-950/50 transition-colors"
                      >
                        <div className="flex items-start gap-3 flex-1">
                          <Avatar className="h-10 w-10 mt-1">
                            <AvatarImage src={solicitud.fotoPerfilUsuarioAdmin} alt={solicitud.usuarioAdminNombre} />
                            <AvatarFallback className="bg-gradient-to-br from-green-500 to-blue-600 text-white text-xs">
                              {solicitud.usuarioAdminNombre?.substring(0, 2).toUpperCase()}
                            </AvatarFallback>
                          </Avatar>
                          <div className="flex-1 min-w-0">
                            <p className="font-semibold text-sm mb-1">{solicitud.espacioTrabajoNombre}</p>
                            <p className="text-xs text-muted-foreground mb-2">
                              <span className="font-medium">{solicitud.usuarioAdminNombre}</span> te invitó a colaborar
                            </p>
                            <p className="text-xs text-muted-foreground">
                              {new Date(solicitud.fechaCreacion).toLocaleDateString('es-AR', {
                                day: 'numeric',
                                month: 'short',
                                year: 'numeric',
                              })}
                            </p>
                          </div>
                        </div>
                        <div className="flex gap-2 sm:ml-auto">
                          <Button
                            size="sm"
                            variant="outline"
                            className="flex-1 sm:flex-none text-destructive hover:text-destructive hover:bg-destructive/10 border-destructive/20"
                            onClick={() => handleRechazarSolicitud(solicitud.id, solicitud.espacioTrabajoNombre)}
                            disabled={responderSolicitudMutation.isPending}
                          >
                            <X className="h-4 w-4 mr-1" />
                            Rechazar
                          </Button>
                          <Button
                            size="sm"
                            className="flex-1 sm:flex-none"
                            onClick={() => handleAceptarSolicitud(solicitud.id, solicitud.espacioTrabajoNombre)}
                            disabled={responderSolicitudMutation.isPending}
                          >
                            <Check className="h-4 w-4 mr-1" />
                            Aceptar
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Paginación */}
                  {totalPaginasSolicitudes > 1 && (
                    <Pagination className="mt-6">
                      <PaginationContent>
                        <PaginationItem>
                          <PaginationPrevious
                            onClick={() => setCurrentPageSolicitudes((prev) => Math.max(prev - 1, 1))}
                            className={currentPageSolicitudes === 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                          />
                        </PaginationItem>
                        {Array.from({ length: totalPaginasSolicitudes }, (_, i) => i + 1).map((page) => (
                          <PaginationItem key={page}>
                            <PaginationLink
                              onClick={() => setCurrentPageSolicitudes(page)}
                              isActive={currentPageSolicitudes === page}
                              className="cursor-pointer"
                            >
                              {page}
                            </PaginationLink>
                          </PaginationItem>
                        ))}
                        <PaginationItem>
                          <PaginationNext
                            onClick={() => setCurrentPageSolicitudes((prev) => Math.min(prev + 1, totalPaginasSolicitudes))}
                            className={currentPageSolicitudes === totalPaginasSolicitudes ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                          />
                        </PaginationItem>
                      </PaginationContent>
                    </Pagination>
                  )}
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
