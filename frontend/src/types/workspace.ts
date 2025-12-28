// Tipos TypeScript que coinciden con los DTOs de Java del backend

export interface EspacioTrabajoResponse {
  id: number
  nombre: string
  saldo: number
  usuarioAdminId: number
}

export interface EspacioTrabajoRequest {
  nombre: string
  idUsuarioAdmin: number
}
