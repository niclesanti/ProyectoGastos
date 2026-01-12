import { apiClient } from '@/lib/api-client'
import type { Contacto, ContactoDTORequest } from '@/types'

export const contactoService = {
  async listarContactos(idEspacioTrabajo: number): Promise<Contacto[]> {
    const { data } = await apiClient.get<Contacto[]>(`/transaccion/contacto/listar/${idEspacioTrabajo}`)
    return data
  },

  async registrarContacto(contacto: ContactoDTORequest): Promise<Contacto> {
    const { data } = await apiClient.post<Contacto>('/transaccion/contacto/registrar', contacto)
    return data
  },
}
