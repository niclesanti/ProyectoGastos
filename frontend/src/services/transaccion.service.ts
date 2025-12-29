import { apiClient } from '@/lib/api-client'
import type {
  Transaccion,
  TransaccionDTORequest,
} from '@/types'

export const transaccionService = {
  async registrarTransaccion(transaccion: TransaccionDTORequest): Promise<Transaccion> {
    const { data } = await apiClient.post<Transaccion>('/transaccion/registrar', transaccion)
    return data
  },

  async buscarTransaccionesRecientes(idEspacio: number): Promise<Transaccion[]> {
    const { data } = await apiClient.get<Transaccion[]>(`/transaccion/buscarRecientes/${idEspacio}`)
    return data
  },

  async removerTransaccion(id: number): Promise<void> {
    await apiClient.delete(`/transaccion/remover/${id}`)
  },
}
