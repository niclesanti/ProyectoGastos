import { api } from './api'
import type {
  EspacioTrabajo,
  EspacioTrabajoDTORequest,
  MiembroEspacio,
  InvitacionMiembroDTORequest,
} from '@/types'

export const espacioTrabajoService = {
  async getAll(): Promise<EspacioTrabajo[]> {
    return api.get<EspacioTrabajo[]>('/espacios-trabajo')
  },

  async getById(id: number): Promise<EspacioTrabajo> {
    return api.get<EspacioTrabajo>(`/espacios-trabajo/${id}`)
  },

  async create(espacioTrabajo: EspacioTrabajoDTORequest): Promise<EspacioTrabajo> {
    return api.post<EspacioTrabajo>('/espacios-trabajo', espacioTrabajo)
  },

  async update(id: number, espacioTrabajo: EspacioTrabajoDTORequest): Promise<EspacioTrabajo> {
    return api.put<EspacioTrabajo>(`/espacios-trabajo/${id}`, espacioTrabajo)
  },

  async delete(id: number): Promise<void> {
    return api.delete<void>(`/espacios-trabajo/${id}`)
  },

  // Gestión de miembros
  async getMiembros(espacioTrabajoId: number): Promise<MiembroEspacio[]> {
    return api.get<MiembroEspacio[]>(`/espacios-trabajo/${espacioTrabajoId}/miembros`)
  },

  async invitarMiembro(invitacion: InvitacionMiembroDTORequest): Promise<MiembroEspacio> {
    return api.post<MiembroEspacio>(
      `/espacios-trabajo/${invitacion.espacioTrabajoId}/miembros`,
      { email: invitacion.email, rol: invitacion.rol }
    )
  },

  async eliminarMiembro(espacioTrabajoId: number, miembroId: number): Promise<void> {
    return api.delete<void>(`/espacios-trabajo/${espacioTrabajoId}/miembros/${miembroId}`)
  },
}
