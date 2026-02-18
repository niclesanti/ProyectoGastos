/**
 * Tipos globales para valores monetarios
 * 
 * Este módulo define tipos para manejar valores monetarios con precisión decimal
 * durante la transición de number a MoneyDecimal en el frontend.
 */

import { MoneyDecimal } from '@/lib/money'

/**
 * Union type para soportar transición gradual de number a MoneyDecimal
 * 
 * @remarks
 * Durante la migración, algunos componentes pueden usar number (legacy) mientras
 * otros ya usan MoneyDecimal. Este tipo permite compatibilidad temporal.
 */
export type MoneyValue = number | MoneyDecimal

/**
 * Branded type para montos monetarios
 * 
 * @remarks
 * Proporciona type safety adicional para distinguir entre diferentes
 * tipos de valores monetarios en el sistema de tipos de TypeScript.
 */
export type Monto = MoneyDecimal & { readonly __brand: 'Monto' }

/**
 * Branded type para saldos
 */
export type Saldo = MoneyDecimal & { readonly __brand: 'Saldo' }

/**
 * Branded type para totales
 */
export type Total = MoneyDecimal & { readonly __brand: 'Total' }

/**
 * Branded type para cuotas
 */
export type Cuota = MoneyDecimal & { readonly __brand: 'Cuota' }

/**
 * Helper para convertir MoneyValue a MoneyDecimal de forma segura
 * 
 * @param value - Valor que puede ser number o MoneyDecimal
 * @returns MoneyDecimal
 */
export function toMoneyDecimal(value: MoneyValue): MoneyDecimal {
  if (typeof value === 'number') {
    return MoneyDecimal.fromNumber(value)
  }
  return value
}

/**
 * Type guard para verificar si un valor es number
 * 
 * @param value - Valor a verificar
 * @returns true si es number
 */
export function isNumber(value: MoneyValue): value is number {
  return typeof value === 'number'
}

/**
 * Type guard para verificar si un valor es MoneyDecimal
 * 
 * @param value - Valor a verificar
 * @returns true si es MoneyDecimal
 */
export function isMoneyDecimal(value: MoneyValue): value is MoneyDecimal {
  return value instanceof MoneyDecimal
}
