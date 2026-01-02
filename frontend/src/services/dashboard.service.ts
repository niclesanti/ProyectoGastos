import { api } from './api'
import type { DashboardStatsDTO } from '@/types'

export const dashboardService = {
  async getDashboardStats(espacioTrabajoId: number): Promise<DashboardStatsDTO> {
    return api.get<DashboardStatsDTO>(`/transaccion/dashboard-stats/${espacioTrabajoId}`)
  },

  async getSaldoActual(espacioTrabajoId: number): Promise<number> {
    return api.get<number>(`/espacios-trabajo/${espacioTrabajoId}/saldo`)
  },
}
