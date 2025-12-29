import { apiClient } from '@/lib/api-client'
import type {
  CuentaBancaria,
  CuentaBancariaDTORequest,
} from '@/types'

export const cuentaBancariaService = {
  async listarCuentas(idEspacioTrabajo: number): Promise<CuentaBancaria[]> {
    const { data } = await apiClient.get<CuentaBancaria[]>(`/cuentabancaria/listar/${idEspacioTrabajo}`)
    return data
  },

  async crearCuenta(cuenta: CuentaBancariaDTORequest): Promise<void> {
    await apiClient.post('/cuentabancaria/crear', cuenta)
  },

  async transferirEntreCuentas(idCuentaOrigen: number, idCuentaDestino: number, monto: number): Promise<void> {
    await apiClient.put(`/cuentabancaria/transaccion/${idCuentaOrigen}/${idCuentaDestino}/${monto}`)
  },
}
