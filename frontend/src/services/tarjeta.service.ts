import { apiClient } from '@/lib/api-client'
import type { TarjetaDTORequest, TarjetaDTOResponse, CuotaCredito } from '@/types'

export const tarjetaService = {
  async listarTarjetas(idEspacioTrabajo: string): Promise<TarjetaDTOResponse[]> {
    const { data } = await apiClient.get<TarjetaDTOResponse[]>(`/compras-credito/tarjetas/${idEspacioTrabajo}`)
    return data
  },

  async registrarTarjeta(tarjeta: TarjetaDTORequest): Promise<TarjetaDTOResponse> {
    const { data } = await apiClient.post<TarjetaDTOResponse>('/compras-credito/tarjetas', tarjeta)
    return data
  },

  async listarCuotasPorTarjeta(idTarjeta: number): Promise<CuotaCredito[]> {
    const { data } = await apiClient.get<CuotaCredito[]>(`/compras-credito/cuotas/${idTarjeta}`)
    return data
  },

  async removerTarjeta(id: number): Promise<void> {
    await apiClient.delete(`/compras-credito/tarjeta/${id}`)
  },

  async modificarTarjeta(id: number, diaCierre: number, diaVencimientoPago: number): Promise<TarjetaDTOResponse> {
    const { data } = await apiClient.put<TarjetaDTOResponse>(
      `/compras-credito/tarjetas/${id}`,
      { diaCierre, diaVencimientoPago }
    )
    return data
  },
}
