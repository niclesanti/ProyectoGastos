import { apiClient } from '@/lib/api-client'
import type { CompraCreditoDTORequest, CompraCredito, CompraCreditoDTOResponse } from '@/types'

export const compraCreditoService = {
  async registrarCompraCredito(compra: CompraCreditoDTORequest): Promise<CompraCredito> {
    const { data } = await apiClient.post<CompraCredito>('/comprascredito/registrar', compra)
    return data
  },

  async listarComprasPendientes(idEspacioTrabajo: number): Promise<CompraCreditoDTOResponse[]> {
    const { data } = await apiClient.get<CompraCreditoDTOResponse[]>(`/comprascredito/pendientes/${idEspacioTrabajo}`)
    return data
  },
}
