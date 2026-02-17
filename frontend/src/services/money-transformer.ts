/**
 * Money Field Transformer - Transformación automática JSON ↔ MoneyDecimal
 * 
 * Este módulo proporciona transformación bidireccional entre JSON y MoneyDecimal:
 * - Response: Convierte campos monetarios de number a MoneyDecimal
 * - Request: Serializa MoneyDecimal a string/number para JSON
 * 
 * @remarks
 * Alineado con el backend BigDecimal (NUMERIC(15,2))
 */

import { MoneyDecimal } from '../lib/money'

/**
 * Lista exhaustiva de campos monetarios que deben transformarse
 * automáticamente en responses del backend.
 * 
 * @remarks
 * - Incluye campos de todas las entidades y DTOs
 * - Excluye campos numéricos no monetarios (IDs, cantidades, porcentajes)
 * - Sincronizado con backend DTOs
 */
const MONEY_FIELD_NAMES = new Set([
  // EspacioTrabajo
  'saldo',
  
  // CuentaBancaria
  'saldoActual',
  
  // Transacciones
  'monto',
  
  // Compras a Crédito
  'montoTotal',
  'montoCuota',
  
  // Dashboard Stats
  'ingresos',
  'gastos',
  'balanceTotal',
  'gastosMensuales',
  'resumenMensual',
  'deudaTotalPendiente',
  'totalBalance',
  'monthlySpending',
  'upcomingCreditDue',
  'outstandingDebt',
  'balanceChange',
  'spendingChange',
])

/**
 * Verifica si un campo debe transformarse a MoneyDecimal
 */
function isMoneyField(fieldName: string): boolean {
  return MONEY_FIELD_NAMES.has(fieldName)
}

/**
 * Transforma un valor primitivo de campo monetario a MoneyDecimal
 */
function transformMoneyValue(value: unknown): MoneyDecimal {
  if (typeof value === 'number') {
    return MoneyDecimal.fromNumber(value)
  }
  if (typeof value === 'string') {
    return MoneyDecimal.fromString(value)
  }
  // Si ya es MoneyDecimal, retornar tal cual
  if (value instanceof MoneyDecimal) {
    return value
  }
  
  // Fallback: convertir a 0
  console.warn(`[MoneyTransformer] Valor inesperado para campo monetario:`, value)
  return MoneyDecimal.ZERO
}

/**
 * Transforma recursivamente un objeto, convirtiendo campos monetarios
 * de number/string a MoneyDecimal.
 * 
 * @param data - Objeto, array o primitivo desde el backend
 * @returns Datos transformados con MoneyDecimal en campos monetarios
 * 
 * @example
 * ```ts
 * const response = { monto: 1234.56, descripcion: 'Test' }
 * const transformed = transformMoneyFields(response)
 * // { monto: MoneyDecimal(1234.56), descripcion: 'Test' }
 * ```
 */
export function transformMoneyFields<T>(data: T): T {
  // Caso null/undefined
  if (data === null || data === undefined) {
    return data
  }

  // Caso Array: transformar cada elemento recursivamente
  if (Array.isArray(data)) {
    return data.map(item => transformMoneyFields(item)) as T
  }

  // Caso Objeto: transformar campos recursivamente
  if (typeof data === 'object' && data !== null) {
    // Evitar transformar instancias de clases especiales
    if (data instanceof Date || data instanceof MoneyDecimal) {
      return data
    }

    const transformed: any = {}
    
    for (const [key, value] of Object.entries(data)) {
      // Si es un campo monetario y es number/string, transformar
      if (isMoneyField(key) && (typeof value === 'number' || typeof value === 'string')) {
        transformed[key] = transformMoneyValue(value)
      } 
      // Si no es monetario, transformar recursivamente (puede haber objetos anidados)
      else {
        transformed[key] = transformMoneyFields(value)
      }
    }
    
    return transformed as T
  }

  // Caso primitivo: retornar sin cambios
  return data
}

/**
 * Serializa un objeto para JSON.stringify, convirtiendo MoneyDecimal a number.
 * 
 * Uso con JSON.stringify:
 * ```ts
 * const data = { monto: MoneyDecimal.fromNumber(100), nombre: 'Test' }
 * JSON.stringify(data, moneyReplacer)
 * // '{"monto":100,"nombre":"Test"}'
 * ```
 */
export function moneyReplacer(_key: string, value: any): any {
  // Si el valor es MoneyDecimal, convertir a number para JSON
  if (value instanceof MoneyDecimal) {
    return value.toNumber()
  }
  
  return value
}

/**
 * Serializa un objeto con MoneyDecimal a JSON string.
 * 
 * @param data - Objeto que puede contener MoneyDecimal
 * @returns JSON string con MoneyDecimal serializado a number
 * 
 * @example
 * ```ts
 * const request = { monto: MoneyDecimal.fromNumber(1000) }
 * const json = serializeMoneyFields(request)
 * // '{"monto":1000}'
 * ```
 */
export function serializeMoneyFields(data: any): string {
  return JSON.stringify(data, moneyReplacer)
}
