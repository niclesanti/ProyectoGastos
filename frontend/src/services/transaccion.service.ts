import { apiClient } from '@/lib/api-client'
import type {
  Transaccion,
  TransaccionDTORequest,
  TransaccionDTOResponse,
  TransaccionBusquedaDTO,
  MotivoTransaccion,
  ContactoTransferencia,
  DashboardStatsDTO,
} from '@/types'

export const transaccionService = {
  async registrarTransaccion(transaccion: TransaccionDTORequest): Promise<Transaccion> {
    const { data } = await apiClient.post<Transaccion>('/transaccion/registrar', transaccion)
    return data
  },

  async buscarTransaccionesRecientes(idEspacio: number): Promise<TransaccionDTOResponse[]> {
    const { data } = await apiClient.get<TransaccionDTOResponse[]>(`/transaccion/buscarRecientes/${idEspacio}`)
    return data
  },

  async removerTransaccion(id: number): Promise<void> {
    await apiClient.delete(`/transaccion/remover/${id}`)
  },

  async buscarTransacciones(busqueda: TransaccionBusquedaDTO): Promise<TransaccionDTOResponse[]> {
    const { data } = await apiClient.post<TransaccionDTOResponse[]>('/transaccion/buscar', busqueda)
    return data
  },

  async listarMotivos(idEspacioTrabajo: number): Promise<MotivoTransaccion[]> {
    const { data } = await apiClient.get<MotivoTransaccion[]>(`/transaccion/motivo/listar/${idEspacioTrabajo}`)
    return data
  },

  async listarContactos(idEspacioTrabajo: number): Promise<ContactoTransferencia[]> {
    const { data } = await apiClient.get<ContactoTransferencia[]>(`/transaccion/contacto/listar/${idEspacioTrabajo}`)
    return data
  },

  async obtenerDashboardStats(idEspacio: number): Promise<DashboardStatsDTO> {
    const { data } = await apiClient.get<DashboardStatsDTO>(`/dashboard/stats/${idEspacio}`)
    return data
  },
}
