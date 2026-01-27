import { api } from './api'
import type { DashboardStatsDTO } from '@/types'

export const dashboardService = {
  async getDashboardStats(espacioTrabajoId: string): Promise<DashboardStatsDTO> {
    return api.get<DashboardStatsDTO>(`/dashboard/stats/${espacioTrabajoId}`)
  },

  async getSaldoActual(espacioTrabajoId: string): Promise<number> {
    return api.get<number>(`/espacios-trabajo/${espacioTrabajoId}/saldo`)
  },
}
