import { apiClient } from '@/lib/api-client'
import type { DescuentoDTORequest, DescuentoDTOResponse } from '@/types'

export const descuentoService = {
  async listarDescuentos(idEspacioTrabajo: string): Promise<DescuentoDTOResponse[]> {
    const { data } = await apiClient.get<DescuentoDTOResponse[]>(
      `/cuentas-bancarias/descuentos/espacio/${idEspacioTrabajo}`
    )
    return data
  },

  async crearDescuento(descuento: DescuentoDTORequest): Promise<void> {
    await apiClient.post('/cuentas-bancarias/descuentos', descuento)
  },

  async eliminarDescuento(id: number): Promise<void> {
    await apiClient.delete(`/cuentas-bancarias/descuentos/${id}`)
  },
}
