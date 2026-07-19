import { apiClient } from '@/lib/api-client'
import type {
  Transaccion,
  TransaccionDTORequest,
  TransaccionDTOResponse,
  TransaccionBusquedaDTO,
  MotivoTransaccion,
  ContactoTransferencia,
  DashboardStatsDTO,
  PaginatedResponse,
} from '@/types'

export const transaccionService = {
  async registrarTransaccion(transaccion: TransaccionDTORequest): Promise<Transaccion> {
    const { data } = await apiClient.post<Transaccion>('/transacciones', transaccion)
    return data
  },

  async buscarTransaccionesRecientes(idEspacio: string): Promise<TransaccionDTOResponse[]> {
    const { data } = await apiClient.get<TransaccionDTOResponse[]>(`/transacciones/recientes/${idEspacio}`)
    return data
  },

  async removerTransaccion(id: number): Promise<void> {
    await apiClient.delete(`/transacciones/${id}`)
  },

  async buscarTransacciones(busqueda: TransaccionBusquedaDTO & { page?: number; size?: number }): Promise<PaginatedResponse<TransaccionDTOResponse>> {
    const { data } = await apiClient.post<PaginatedResponse<TransaccionDTOResponse>>('/transacciones/buscar', busqueda)
    return data
  },

  async listarMotivos(idEspacioTrabajo: string): Promise<MotivoTransaccion[]> {
    const { data } = await apiClient.get<MotivoTransaccion[]>(`/transacciones/motivos/espacio/${idEspacioTrabajo}`)
    return data
  },

  async listarContactos(idEspacioTrabajo: string): Promise<ContactoTransferencia[]> {
    const { data } = await apiClient.get<ContactoTransferencia[]>(`/transacciones/contactos/espacio/${idEspacioTrabajo}`)
    return data
  },

  async obtenerDashboardStats(idEspacio: string): Promise<DashboardStatsDTO> {
    const { data } = await apiClient.get<DashboardStatsDTO>(`/dashboard/stats/${idEspacio}`)
    return data
  },
}
