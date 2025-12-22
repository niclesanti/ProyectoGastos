import { api } from './api'
import type { DashboardInfoDTO } from '@/types'

export const dashboardService = {
  async getDashboardInfo(espacioTrabajoId: number): Promise<DashboardInfoDTO> {
    return api.get<DashboardInfoDTO>(`/transacciones/dashboard/${espacioTrabajoId}`)
  },

  async getSaldoActual(espacioTrabajoId: number): Promise<number> {
    return api.get<number>(`/espacios-trabajo/${espacioTrabajoId}/saldo`)
  },
}
