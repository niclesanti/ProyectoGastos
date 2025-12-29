// Enums
export enum TipoTransaccion {
  INGRESO = 'INGRESO',
  GASTO = 'GASTO',
}

// Entidades Base
export interface Usuario {
  id: number
  nombre: string
  email: string
  picture?: string
}

export interface EspacioTrabajo {
  id: number
  nombre: string
  saldo: number
  usuarioAdmin: Usuario
  usuariosParticipantes?: Usuario[]
}

export enum RolMiembro {
  ADMIN = 'ADMIN',
  EDITOR = 'EDITOR',
  VIEWER = 'VIEWER',
}

export interface MiembroEspacio {
  id: number
  nombre: string
  email: string
  fotoPerfil?: string
}

export interface InvitacionMiembroDTORequest {
  email: string
  rol: RolMiembro
  espacioTrabajoId: number
}

export interface MotivoTransaccion {
  id: number
  motivo: string
  espacioTrabajo?: EspacioTrabajo
}

export interface CuentaBancaria {
  id: number
  nombre: string
  entidadFinanciera: string
  saldoActual: number
  espacioTrabajo?: EspacioTrabajo
}

export interface ContactoTransferencia {
  id: number
  nombre: string
  espacioTrabajo?: EspacioTrabajo
}

export interface Transaccion {
  id: number
  tipo: TipoTransaccion
  monto: number
  fecha: string // ISO date string
  descripcion?: string
  nombreCompletoAuditoria: string
  fechaCreacion: string // ISO datetime string
  espacioTrabajo: EspacioTrabajo
  motivo: MotivoTransaccion
  contacto?: ContactoTransferencia
  cuentaBancaria?: CuentaBancaria
}

// DTO Response para búsqueda de transacciones
export interface TransaccionDTOResponse {
  id: number
  fecha: string // LocalDate
  monto: number
  tipo: TipoTransaccion
  descripcion?: string
  nombreCompletoAuditoria: string
  fechaCreacion: string // LocalDateTime
  idEspacioTrabajo: number
  nombreEspacioTrabajo: string
  idMotivo: number
  nombreMotivo: string
  idContacto?: number
  nombreContacto?: string
  nombreCuentaBancaria?: string
}

// DTOs Request
export interface TransaccionDTORequest {
  tipo: string // 'gasto' | 'ingreso'
  monto: number
  fecha: string // ISO date string
  descripcion?: string
  nombreCompletoAuditoria: string
  idEspacioTrabajo: number
  idMotivo: number
  idContacto?: number
  idCuentaBancaria?: number
}

export interface CuentaBancariaDTORequest {
  nombre: string
  entidadFinanciera: string
  idEspacioTrabajo: number
}

export interface EspacioTrabajoDTORequest {
  nombre: string
  saldo?: number
}

export interface MotivoTransaccionDTORequest {
  motivo: string
  idEspacioTrabajo: number
}

export interface ContactoDTORequest {
  nombre: string
  idEspacioTrabajo: number
}

export interface TransaccionBusquedaDTO {
  mes?: number | null
  anio?: number | null
  motivo?: string | null
  contacto?: string | null
  idEspacioTrabajo: number
}

export interface TarjetaDTORequest {
  numeroTarjeta: string
  entidadFinanciera: string
  redDePago: string
  diaCierre: number
  diaVencimientoPago: number
  espacioTrabajoId: number
}

export interface TarjetaDTOResponse {
  id: number
  numeroTarjeta: string
  entidadFinanciera: string
  redDePago: string
  diaCierre: number
  diaVencimientoPago: number
  espacioTrabajoId: number
}

export interface CompraCreditoDTORequest {
  fechaCompra: string
  montoTotal: number
  cantidadCuotas: number
  descripcion?: string
  nombreCompletoAuditoria: string
  espacioTrabajoId: number
  motivoId: number
  comercioId?: number
  tarjetaId: number
}

// Aliases para compatibilidad
export type Motivo = MotivoTransaccion
export type MotivoDTORequest = MotivoTransaccionDTORequest
export type Contacto = ContactoTransferencia

export interface Tarjeta {
  id: number
  numeroTarjeta: string
  entidadFinanciera: string
  redDePago: string
  diaCierre: number
  diaVencimientoPago: number
  espacioTrabajoId: number
}

// DTOs Response
export interface DistribucionGastoDTO {
  motivo: string
  porcentaje: number
}

export interface IngresosGastosMesDTO {
  mes: string
  ingresos: number
  gastos: number
}

export interface SaldoAcumuladoMesDTO {
  mes: string
  saldo: number
}

export interface DashboardInfoDTO {
  ingresosGastos: IngresosGastosMesDTO[]
  distribucionGastos: DistribucionGastoDTO[]
  saldoAcumuladoMes: SaldoAcumuladoMesDTO[]
}

// Estadísticas del Dashboard
export interface DashboardStats {
  totalBalance: number
  monthlySpending: number
  upcomingCreditDue: number
  outstandingDebt: number
  balanceChange?: number
  spendingChange?: number
}

// Transacciones Recientes
export interface RecentTransaction {
  id: number
  descripcion: string
  monto: number
  fecha: string
  tipo: TipoTransaccion
  motivo: string
  status?: 'success' | 'pending' | 'failed'
}

// Compras a Crédito
export interface CompraCredito {
  id: number
  descripcion: string
  montoTotal: number
  cantidadCuotas: number
  fechaCompra: string
  espacioTrabajo?: EspacioTrabajo
}

export interface CuotaCredito {
  id: number
  numeroCuota: number
  montoCuota: number
  fechaVencimiento: string
  pagada: boolean
  compraCredito: CompraCredito
}

// API Response wrapper
export interface ApiResponse<T> {
  data: T
  message?: string
  success: boolean
}

// Pagination
export interface PageRequest {
  page: number
  size: number
  sort?: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}
