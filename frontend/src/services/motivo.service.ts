import { apiClient } from '@/lib/api-client'
import type { Motivo, MotivoDTORequest } from '@/types'

export const motivoService = {
  async listarMotivos(idEspacioTrabajo: string): Promise<Motivo[]> {
    const { data } = await apiClient.get<Motivo[]>(`/transacciones/motivos/espacio/${idEspacioTrabajo}`)
    return data
  },

  async registrarMotivo(motivo: MotivoDTORequest): Promise<Motivo> {
    const { data } = await apiClient.post<Motivo>('/transacciones/motivos', motivo)
    return data
  },
}
