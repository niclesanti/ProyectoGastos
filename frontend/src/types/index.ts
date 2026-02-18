import { MoneyDecimal } from '../lib/money'
import type { MoneyValue } from './money'

// Enums
export enum TipoTransaccion {
  INGRESO = 'INGRESO',
  GASTO = 'GASTO',
}

// Entidades Base
export interface Usuario {
  id: string  // UUID
  nombre: string
  email: string
  picture?: string
}

export interface EspacioTrabajo {
  id: string  // UUID
  nombre: string
  saldo: MoneyDecimal
  usuarioAdmin: Usuario
  usuariosParticipantes?: Usuario[]
}

export enum RolMiembro {
  ADMIN = 'ADMIN',
  EDITOR = 'EDITOR',
  VIEWER = 'VIEWER',
}

export interface MiembroEspacio {
  id: string  // UUID
  nombre: string
  email: string
  fotoPerfil?: string
}

export interface SolicitudPendienteEspacioTrabajo {
  id: number
  espacioTrabajoNombre: string
  usuarioAdminNombre: string
  fotoPerfilUsuarioAdmin?: string
  fechaCreacion: string // ISO datetime string
}

export interface InvitacionMiembroDTORequest {
  email: string
  rol: RolMiembro
  espacioTrabajoId: string  // UUID
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
  saldoActual: MoneyDecimal
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
  monto: MoneyDecimal
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
  monto: MoneyDecimal
  tipo: TipoTransaccion
  descripcion?: string
  nombreCompletoAuditoria: string
  fechaCreacion: string // LocalDateTime
  idEspacioTrabajo: string  // UUID
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
  monto: MoneyValue
  fecha: string // ISO date string
  descripcion?: string
  nombreCompletoAuditoria: string
  idEspacioTrabajo: string  // UUID
  idMotivo: number
  idContacto?: number
  idCuentaBancaria?: number
}

export interface CuentaBancariaDTORequest {
  nombre: string
  entidadFinanciera: string
  idEspacioTrabajo: string  // UUID
  saldoActual: MoneyValue
}

export interface EspacioTrabajoDTORequest {
  nombre: string
  idUsuarioAdmin: string  // UUID - requerido
}

export interface MotivoTransaccionDTORequest {
  motivo: string
  idEspacioTrabajo: string  // UUID
}

export interface ContactoDTORequest {
  nombre: string
  idEspacioTrabajo: string  // UUID
}

export interface TransaccionBusquedaDTO {
  mes?: number | null
  anio?: number | null
  motivo?: string | null
  contacto?: string | null
  idEspacioTrabajo: string  // UUID
}

export interface TarjetaDTORequest {
  numeroTarjeta: string
  entidadFinanciera: string
  redDePago: string
  diaCierre: number
  diaVencimientoPago: number
  espacioTrabajoId: string  // UUID
}

export interface TarjetaDTOResponse {
  id: number
  numeroTarjeta: string
  entidadFinanciera: string
  redDePago: string
  diaCierre: number
  diaVencimientoPago: number
  espacioTrabajoId: string  // UUID
}

export interface CompraCreditoDTORequest {
  fechaCompra: string
  montoTotal: MoneyValue
  cantidadCuotas: number
  descripcion?: string
  nombreCompletoAuditoria: string
  espacioTrabajoId: string  // UUID
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
  espacioTrabajoId: string  // UUID
}

// DTOs Response
export interface DistribucionGastoDTO {
  motivo: string
  porcentaje: number
}

export interface IngresosGastosMesDTO {
  mes: string
  ingresos: MoneyDecimal
  gastos: MoneyDecimal
}

export interface SaldoAcumuladoMesDTO {
  mes: string
  saldo: MoneyDecimal
}

export interface DashboardInfoDTO {
  ingresosGastos: IngresosGastosMesDTO[]
  distribucionGastos: DistribucionGastoDTO[]
  saldoAcumuladoMes: SaldoAcumuladoMesDTO[]
}

// Dashboard Stats DTO (consolidado desde backend)
export interface DashboardStatsDTO {
  balanceTotal: MoneyDecimal
  gastosMensuales: MoneyDecimal
  resumenMensual: MoneyDecimal
  deudaTotalPendiente: MoneyDecimal
  flujoMensual: IngresosGastosMesDTO[]
  distribucionGastos: DistribucionGastoDTO[]
}

// Estadísticas del Dashboard
export interface DashboardStats {
  totalBalance: MoneyDecimal
  monthlySpending: MoneyDecimal
  upcomingCreditDue: MoneyDecimal
  outstandingDebt: MoneyDecimal
  balanceChange?: MoneyDecimal
  spendingChange?: MoneyDecimal
}

// Transacciones Recientes
export interface RecentTransaction {
  id: number
  descripcion: string
  monto: MoneyDecimal
  fecha: string
  tipo: TipoTransaccion
  motivo: string
  status?: 'success' | 'pending' | 'failed'
}

// Compras a Crédito
export interface CompraCredito {
  id: number
  descripcion: string
  montoTotal: MoneyDecimal
  cantidadCuotas: number
  fechaCompra: string
  espacioTrabajo?: EspacioTrabajo
}

export interface CompraCreditoDTOResponse {
  id: number
  fechaCompra: string
  montoTotal: MoneyDecimal
  cantidadCuotas: number
  cuotasPagadas: number
  descripcion?: string
  nombreCompletoAuditoria: string
  fechaCreacion: string
  espacioTrabajoId: string  // UUID
  nombreEspacioTrabajo: string
  motivoId: number
  nombreMotivo: string
  comercioId?: number
  nombreComercio?: string
  tarjetaId: number
  numeroTarjeta: string
  entidadFinanciera: string
  redDePago: string
}

export interface CuotaCredito {
  id: number
  numeroCuota: number
  montoCuota: MoneyDecimal
  fechaVencimiento: string
  pagada: boolean
  compraCredito: CompraCredito
}

export interface ResumenTarjetaDTOResponse {
  id: number
  mes: number
  anio: number
  fechaVencimiento: string
  montoTotal: MoneyDecimal
  estado: 'PENDIENTE' | 'VENCIDO' | 'PAGADO'
  tarjetaId: number
  cuotas: CuotaResumenDTO[]
}

export interface CuotaResumenDTO {
  id: number
  numeroCuota: number
  montoCuota: MoneyDecimal
  descripcion: string
  totalCuotas: number
  motivo: string
}

// Notificaciones
export enum TipoNotificacion {
  CIERRE_TARJETA = 'CIERRE_TARJETA',
  VENCIMIENTO_RESUMEN = 'VENCIMIENTO_RESUMEN',
  INVITACION_ESPACIO = 'INVITACION_ESPACIO',
  MIEMBRO_AGREGADO = 'MIEMBRO_AGREGADO',
  SISTEMA = 'SISTEMA',
}

export interface NotificacionDTOResponse {
  id: number
  tipo: TipoNotificacion
  mensaje: string
  leida: boolean
  fechaCreacion: string // ISO datetime string
  fechaLeida?: string | null // ISO datetime string
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

// Paginated Response - matches backend PaginatedResponse DTO
export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  currentPage: number
  pageSize: number
  first: boolean
  last: boolean
  hasPrevious: boolean
  hasNext: boolean
}
