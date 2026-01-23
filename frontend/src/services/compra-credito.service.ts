import { apiClient } from '@/lib/api-client'
import type { CompraCreditoDTORequest, CompraCredito, CompraCreditoDTOResponse, ResumenTarjetaDTOResponse } from '@/types'

export const compraCreditoService = {
  async registrarCompraCredito(compra: CompraCreditoDTORequest): Promise<CompraCredito> {
    const { data } = await apiClient.post<CompraCredito>('/comprascredito/registrar', compra)
    return data
  },

  async listarComprasPendientes(idEspacioTrabajo: string): Promise<CompraCreditoDTOResponse[]> {
    const { data } = await apiClient.get<CompraCreditoDTOResponse[]>(`/comprascredito/pendientes/${idEspacioTrabajo}`)
    return data
  },

  async removerCompraCredito(id: number): Promise<void> {
    await apiClient.delete(`/comprascredito/${id}`)
  },

  async listarResumenesPorTarjeta(idTarjeta: number): Promise<ResumenTarjetaDTOResponse[]> {
    const { data } = await apiClient.get<ResumenTarjetaDTOResponse[]>(`/comprascredito/resumenes/tarjeta/${idTarjeta}`)
    return data
  },

  async pagarResumenTarjeta(request: {
    idResumen: number
    fecha: string // formato 'yyyy-MM-dd'
    monto: number
    nombreCompletoAuditoria: string
    idEspacioTrabajo: string  // UUID
    idCuentaBancaria?: number
  }): Promise<void> {
    await apiClient.post('/comprascredito/pagar-resumen', request)
  },
}
