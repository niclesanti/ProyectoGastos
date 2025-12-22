import { api } from './api'
import type {
  Transaccion,
  TransaccionDTORequest,
  PageResponse,
  PageRequest,
} from '@/types'

export const transaccionService = {
  async getAll(espacioTrabajoId: number, pageRequest?: PageRequest): Promise<PageResponse<Transaccion>> {
    const params = new URLSearchParams()
    if (pageRequest) {
      params.append('page', String(pageRequest.page))
      params.append('size', String(pageRequest.size))
      if (pageRequest.sort) params.append('sort', pageRequest.sort)
    }
    return api.get<PageResponse<Transaccion>>(
      `/transacciones/espacio/${espacioTrabajoId}?${params.toString()}`
    )
  },

  async getById(id: number): Promise<Transaccion> {
    return api.get<Transaccion>(`/transacciones/${id}`)
  },

  async create(transaccion: TransaccionDTORequest): Promise<Transaccion> {
    return api.post<Transaccion>('/transacciones', transaccion)
  },

  async update(id: number, transaccion: TransaccionDTORequest): Promise<Transaccion> {
    return api.put<Transaccion>(`/transacciones/${id}`, transaccion)
  },

  async delete(id: number): Promise<void> {
    return api.delete<void>(`/transacciones/${id}`)
  },

  async getRecent(espacioTrabajoId: number, limit: number = 10): Promise<Transaccion[]> {
    return api.get<Transaccion[]>(`/transacciones/espacio/${espacioTrabajoId}/recientes?limit=${limit}`)
  },
}
