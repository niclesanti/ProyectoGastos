import { apiClient } from '@/lib/api-client'
import type { TarjetaDTORequest, TarjetaDTOResponse, CuotaCredito } from '@/types'

export const tarjetaService = {
  async listarTarjetas(idEspacioTrabajo: string): Promise<TarjetaDTOResponse[]> {
    const { data } = await apiClient.get<TarjetaDTOResponse[]>(`/comprascredito/tarjetas/${idEspacioTrabajo}`)
    return data
  },

  async registrarTarjeta(tarjeta: TarjetaDTORequest): Promise<TarjetaDTOResponse> {
    const { data } = await apiClient.post<TarjetaDTOResponse>('/comprascredito/registrarTarjeta', tarjeta)
    return data
  },

  async listarCuotasPorTarjeta(idTarjeta: number): Promise<CuotaCredito[]> {
    const { data } = await apiClient.get<CuotaCredito[]>(`/comprascredito/cuotas/${idTarjeta}`)
    return data
  },

  async removerTarjeta(id: number): Promise<void> {
    await apiClient.delete(`/comprascredito/tarjeta/${id}`)
  },
}
