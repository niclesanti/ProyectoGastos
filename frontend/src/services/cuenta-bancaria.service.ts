import { apiClient } from '@/lib/api-client'
import type {
  CuentaBancaria,
  CuentaBancariaDTORequest,
} from '@/types'

export const cuentaBancariaService = {
  async listarCuentas(idEspacioTrabajo: string): Promise<CuentaBancaria[]> {
    const { data } = await apiClient.get<CuentaBancaria[]>(`/cuentas-bancarias/espacio/${idEspacioTrabajo}`)
    return data
  },

  async crearCuenta(cuenta: CuentaBancariaDTORequest): Promise<void> {
    await apiClient.post('/cuentas-bancarias', cuenta)
  },

  async transferirEntreCuentas(idCuentaOrigen: number, idCuentaDestino: number, monto: number): Promise<void> {
    await apiClient.post('/cuentas-bancarias/transacciones', {
      idCuentaOrigen,
      idCuentaDestino,
      monto
    })
  },
}
