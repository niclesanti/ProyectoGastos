import { apiClient } from '@/lib/api-client'
import type { DescuentoDTORequest, DescuentoDTOResponse } from '@/types'

export const descuentoService = {
  async listarDescuentos(idEspacioTrabajo: string): Promise<DescuentoDTOResponse[]> {
    const { data } = await apiClient.get<DescuentoDTOResponse[]>(
      `/cuentabancaria/descuento/listar/${idEspacioTrabajo}`
    )
    return data
  },

  async crearDescuento(descuento: DescuentoDTORequest): Promise<void> {
    await apiClient.post('/cuentabancaria/descuento/crear', descuento)
  },

  async eliminarDescuento(id: number): Promise<void> {
    await apiClient.delete(`/cuentabancaria/descuento/eliminar/${id}`)
  },
}
