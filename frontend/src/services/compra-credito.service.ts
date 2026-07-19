import { apiClient } from '@/lib/api-client'
import type {
  CompraCreditoDTORequest,
  CompraCredito,
  CompraCreditoDTOResponse,
  ResumenTarjetaDTOResponse,
  PaginatedResponse,
  CompraCreditoBusquedaDTO,
} from '@/types'

export const compraCreditoService = {
  async registrarCompraCredito(compra: CompraCreditoDTORequest): Promise<CompraCredito> {
    const { data } = await apiClient.post<CompraCredito>('/compras-credito', compra)
    return data
  },

  async listarComprasPendientes(idEspacioTrabajo: string, page?: number, size?: number): Promise<PaginatedResponse<CompraCreditoDTOResponse>> {
    const params = new URLSearchParams()
    if (page !== undefined) params.append('page', page.toString())
    if (size !== undefined) params.append('size', size.toString())
    
    const url = `/compras-credito/pendientes/${idEspacioTrabajo}${params.toString() ? `?${params.toString()}` : ''}`
    const { data } = await apiClient.get<PaginatedResponse<CompraCreditoDTOResponse>>(url)
    return data
  },

  async buscarComprasCredito(busqueda: CompraCreditoBusquedaDTO): Promise<PaginatedResponse<CompraCreditoDTOResponse>> {
    const { data } = await apiClient.post<PaginatedResponse<CompraCreditoDTOResponse>>('/compras-credito/buscar', busqueda)
    return data
  },

  async removerCompraCredito(id: number): Promise<void> {
    await apiClient.delete(`/compras-credito/${id}`)
  },

  async listarResumenesPorTarjeta(idTarjeta: number): Promise<ResumenTarjetaDTOResponse[]> {
    const { data } = await apiClient.get<ResumenTarjetaDTOResponse[]>(`/compras-credito/resumenes/tarjeta/${idTarjeta}`)
    return data
  },

  async pagarResumenTarjeta(request: {
    idResumen: number
    fecha: string
    monto: number
    nombreCompletoAuditoria: string
    idEspacioTrabajo: string
    idCuentaBancaria?: number
  }): Promise<void> {
    await apiClient.post('/compras-credito/pagar-resumen', request)
  },
}
