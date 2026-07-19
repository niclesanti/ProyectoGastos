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
    const { data } = await apiClient.get<EspacioTrabajo[]>('/espacios-trabajo')
    return data
  },

  async getById(id: string): Promise<EspacioTrabajo> {
    const { data } = await apiClient.get<EspacioTrabajo>(`/espacios-trabajo/${id}`)
    return data
  },

  async create(espacioTrabajo: EspacioTrabajoDTORequest): Promise<void> {
    await apiClient.post<void>('/espacios-trabajo', espacioTrabajo)
  },

  async update(id: string, espacioTrabajo: EspacioTrabajoDTORequest): Promise<EspacioTrabajo> {
    const { data } = await apiClient.put<EspacioTrabajo>(`/espacios-trabajo/${id}`, espacioTrabajo)
    return data
  },

  async delete(id: string): Promise<void> {
    await apiClient.delete<void>(`/espacios-trabajo/${id}`)
  },

  // Gestión de miembros
  async getMiembros(espacioTrabajoId: string): Promise<MiembroEspacio[]> {
    const { data } = await apiClient.get<MiembroEspacio[]>(`/espacios-trabajo/${espacioTrabajoId}/miembros`)
    return data
  },

  async compartirEspacio(email: string, idEspacioTrabajo: string): Promise<void> {
    await apiClient.post<void>(`/espacios-trabajo/${idEspacioTrabajo}/miembros`, { email })
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
    const { data } = await apiClient.get<SolicitudPendienteEspacioTrabajo[]>('/espacios-trabajo/solicitudes/pendientes')
    return data
  },

  async responderSolicitud(idSolicitud: number, aceptada: boolean): Promise<void> {
    await apiClient.post<void>(`/espacios-trabajo/solicitudes/${idSolicitud}/responder`, { aceptada })
  },
}
