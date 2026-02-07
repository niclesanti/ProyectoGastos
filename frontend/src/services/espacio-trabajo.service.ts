import { apiClient } from '@/lib/api-client'
import type {
  EspacioTrabajo,
  EspacioTrabajoDTORequest,
  MiembroEspacio,
  InvitacionMiembroDTORequest,
  SolicitudPendienteEspacioTrabajo,
} from '@/types'

export const espacioTrabajoService = {
  async getAll(): Promise<EspacioTrabajo[]> {
    // El usuario se obtiene del contexto OAuth2 automáticamente
    const { data } = await apiClient.get<EspacioTrabajo[]>('/espaciotrabajo/listar')
    return data
  },

  async getById(id: string): Promise<EspacioTrabajo> {
    const { data } = await apiClient.get<EspacioTrabajo>(`/espaciotrabajo/${id}`)
    return data
  },

  async create(espacioTrabajo: EspacioTrabajoDTORequest): Promise<void> {
    await apiClient.post<void>('/espaciotrabajo/registrar', espacioTrabajo)
  },

  async update(id: string, espacioTrabajo: EspacioTrabajoDTORequest): Promise<EspacioTrabajo> {
    const { data } = await apiClient.put<EspacioTrabajo>(`/espaciotrabajo/${id}`, espacioTrabajo)
    return data
  },

  async delete(id: string): Promise<void> {
    await apiClient.delete<void>(`/espaciotrabajo/${id}`)
  },

  // Gestión de miembros
  async getMiembros(espacioTrabajoId: string): Promise<MiembroEspacio[]> {
    const { data } = await apiClient.get<MiembroEspacio[]>(`/espaciotrabajo/miembros/${espacioTrabajoId}`)
    return data
  },

  async compartirEspacio(email: string, idEspacioTrabajo: string): Promise<void> {
    // Ya NO se envía idUsuarioAdmin, se valida en el backend desde el contexto OAuth2
    await apiClient.put<void>(`/espaciotrabajo/compartir/${email}/${idEspacioTrabajo}`)
  },

  async invitarMiembro(invitacion: InvitacionMiembroDTORequest): Promise<MiembroEspacio> {
    const { data } = await apiClient.post<MiembroEspacio>(
      `/espacios-trabajo/${invitacion.espacioTrabajoId}/miembros`,
      { email: invitacion.email, rol: invitacion.rol }
    )
    return data
  },

  async eliminarMiembro(espacioTrabajoId: string, miembroId: string): Promise<void> {
    await apiClient.delete<void>(`/espacios-trabajo/${espacioTrabajoId}/miembros/${miembroId}`)
  },

  // Gestión de solicitudes pendientes
  async listarSolicitudesPendientes(): Promise<SolicitudPendienteEspacioTrabajo[]> {
    const { data } = await apiClient.get<SolicitudPendienteEspacioTrabajo[]>('/espaciotrabajo/solicitudes/pendientes')
    return data
  },

  async responderSolicitud(idSolicitud: number, aceptada: boolean): Promise<void> {
    await apiClient.put<void>(`/espaciotrabajo/solicitud/responder/${idSolicitud}/${aceptada}`)
  },
}
