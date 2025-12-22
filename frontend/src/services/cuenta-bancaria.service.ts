import { api } from './api'
import type {
  CuentaBancaria,
  CuentaBancariaDTORequest,
} from '@/types'

export const cuentaBancariaService = {
  async getAll(espacioTrabajoId: number): Promise<CuentaBancaria[]> {
    return api.get<CuentaBancaria[]>(`/cuentas-bancarias/espacio/${espacioTrabajoId}`)
  },

  async getById(id: number): Promise<CuentaBancaria> {
    return api.get<CuentaBancaria>(`/cuentas-bancarias/${id}`)
  },

  async create(cuenta: CuentaBancariaDTORequest): Promise<CuentaBancaria> {
    return api.post<CuentaBancaria>('/cuentas-bancarias', cuenta)
  },

  async update(id: number, cuenta: CuentaBancariaDTORequest): Promise<CuentaBancaria> {
    return api.put<CuentaBancaria>(`/cuentas-bancarias/${id}`, cuenta)
  },

  async delete(id: number): Promise<void> {
    return api.delete<void>(`/cuentas-bancarias/${id}`)
  },
}
