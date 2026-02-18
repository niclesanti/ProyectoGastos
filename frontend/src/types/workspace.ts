// Tipos TypeScript que coinciden con los DTOs de Java del backend

import { MoneyDecimal } from '../lib/money'

export interface EspacioTrabajoResponse {
  id: string  // UUID
  nombre: string
  saldo: MoneyDecimal
  usuarioAdminId: string  // UUID
}

export interface EspacioTrabajoRequest {
  nombre: string
  idUsuarioAdmin: string  // UUID
}
