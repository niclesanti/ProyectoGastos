import { apiClient } from '@/lib/api-client'
import type { Tarjeta, TarjetaDTORequest, CuotaCredito } from '@/types'

export const tarjetaService = {
  async listarTarjetas(idEspacioTrabajo: number): Promise<Tarjeta[]> {
    const { data } = await apiClient.get<Tarjeta[]>(`/comprascredito/tarjetas/${idEspacioTrabajo}`)
    return data
  },

  async registrarTarjeta(tarjeta: TarjetaDTORequest): Promise<Tarjeta> {
    const { data } = await apiClient.post<Tarjeta>('/comprascredito/registrarTarjeta', tarjeta)
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
