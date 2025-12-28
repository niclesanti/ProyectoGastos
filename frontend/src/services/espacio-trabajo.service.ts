import { apiClient } from '@/lib/api-client'
import type {
  EspacioTrabajo,
  EspacioTrabajoDTORequest,
  MiembroEspacio,
  InvitacionMiembroDTORequest,
} from '@/types'

export const espacioTrabajoService = {
  async getAll(): Promise<EspacioTrabajo[]> {
    const { data } = await apiClient.get<EspacioTrabajo[]>('/espacios-trabajo')
    return data
  },

  async getById(id: number): Promise<EspacioTrabajo> {
    const { data } = await apiClient.get<EspacioTrabajo>(`/espacios-trabajo/${id}`)
    return data
  },

  async create(espacioTrabajo: EspacioTrabajoDTORequest): Promise<EspacioTrabajo> {
    const { data } = await apiClient.post<EspacioTrabajo>('/espacios-trabajo', espacioTrabajo)
    return data
  },

  async update(id: number, espacioTrabajo: EspacioTrabajoDTORequest): Promise<EspacioTrabajo> {
    const { data } = await apiClient.put<EspacioTrabajo>(`/espacios-trabajo/${id}`, espacioTrabajo)
    return data
  },

  async delete(id: number): Promise<void> {
    await apiClient.delete<void>(`/espacios-trabajo/${id}`)
  },

  // Gestión de miembros
  async getMiembros(espacioTrabajoId: number): Promise<MiembroEspacio[]> {
    const { data } = await apiClient.get<MiembroEspacio[]>(`/espaciotrabajo/miembros/${espacioTrabajoId}`)
    return data
  },

  async invitarMiembro(invitacion: InvitacionMiembroDTORequest): Promise<MiembroEspacio> {
    const { data } = await apiClient.post<MiembroEspacio>(
      `/espacios-trabajo/${invitacion.espacioTrabajoId}/miembros`,
      { email: invitacion.email, rol: invitacion.rol }
    )
    return data
  },

  async eliminarMiembro(espacioTrabajoId: number, miembroId: number): Promise<void> {
    await apiClient.delete<void>(`/espacios-trabajo/${espacioTrabajoId}/miembros/${miembroId}`)
  },
}
