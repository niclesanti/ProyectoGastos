import { apiClient } from '@/lib/api-client'
import type { CompraCreditoDTORequest, CompraCredito } from '@/types'

export const compraCreditoService = {
  async registrarCompraCredito(compra: CompraCreditoDTORequest): Promise<CompraCredito> {
    const { data } = await apiClient.post<CompraCredito>('/comprascredito/registrar', compra)
    return data
  },
}
