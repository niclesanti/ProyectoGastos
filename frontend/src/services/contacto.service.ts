import { apiClient } from '@/lib/api-client'
import type { Contacto, ContactoDTORequest } from '@/types'

export const contactoService = {
  async listarContactos(idEspacioTrabajo: string): Promise<Contacto[]> {
    const { data } = await apiClient.get<Contacto[]>(`/transacciones/contactos/espacio/${idEspacioTrabajo}`)
    return data
  },

  async registrarContacto(contacto: ContactoDTORequest): Promise<Contacto> {
    const { data } = await apiClient.post<Contacto>('/transacciones/contactos', contacto)
    return data
  },
}
