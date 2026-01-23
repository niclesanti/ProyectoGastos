// Tipos TypeScript que coinciden con los DTOs de Java del backend

export interface EspacioTrabajoResponse {
  id: string  // UUID
  nombre: string
  saldo: number
  usuarioAdminId: string  // UUID
}

export interface EspacioTrabajoRequest {
  nombre: string
  idUsuarioAdmin: string  // UUID
}
