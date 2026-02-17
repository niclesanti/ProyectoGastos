/**
 * MoneyDecimal - Wrapper sobre decimal.js para operaciones monetarias precisas
 * 
 * Este módulo proporciona funcionalidades para manejar valores monetarios
 * con precisión decimal exacta, alineado con el backend BigDecimal (NUMERIC(15,2)).
 * 
 * @remarks
 * - Precision: 20 dígitos (excede NUMERIC(15,2) del backend)
 * - Rounding: ROUND_HALF_UP (estándar bancario argentino)
 * - Escala: 2 decimales (centavos)
 * 
 * @example
 * ```ts
 * const precio = MoneyDecimal.fromString("1234.56")
 * const total = precio.multiply(3)
 * console.log(total.format()) // "$3,703.68"
 * ```
 */

import Decimal from 'decimal.js'

// Configuración global de Decimal.js
Decimal.config({
  precision: 20,              // Precisión total (excede NUMERIC(15,2))
  rounding: Decimal.ROUND_HALF_UP,  // Redondeo estándar bancario
  toExpNeg: -7,              // No usar notación exponencial para números pequeños
  toExpPos: 20,              // No usar notación exponencial para números grandes
})

/**
 * Clase wrapper sobre Decimal.js para operaciones monetarias
 */
export class MoneyDecimal {
  private readonly value: decimal.Decimal

  private constructor(value: decimal.Decimal) {
    this.value = value
  }

  /**
   * Crea un MoneyDecimal desde un número
   * @param value - Valor numérico (puede tener imprecisión flotante)
   * @returns Instancia de MoneyDecimal
   * @throws Error si el valor es NaN o Infinity
   */
  static fromNumber(value: number): MoneyDecimal {
    if (!Number.isFinite(value)) {
      throw new Error('Cannot create MoneyDecimal from NaN or Infinity')
    }
    return new MoneyDecimal(new Decimal(value))
  }

  /**
   * Crea un MoneyDecimal desde un string (método preferido)
   * @param value - Valor en formato string (ej: "1234.56")
   * @returns Instancia de MoneyDecimal
   * @throws Si el string no es un número válido o está vacío
   */
  static fromString(value: string): MoneyDecimal {
    if (!value || value.trim() === '') {
      throw new Error('Cannot create MoneyDecimal from empty string')
    }
    try {
      const decimal = new Decimal(value)
      if (!decimal.isFinite()) {
        throw new Error('Invalid numeric string')
      }
      return new MoneyDecimal(decimal)
    } catch (error) {
      throw new Error(`Invalid money value: "${value}"`)
    }
  }

  /**
   * Crea un MoneyDecimal desde un Decimal existente
   * @param value - Instancia de Decimal
   * @returns Instancia de MoneyDecimal
   */
  static fromDecimal(value: decimal.Decimal): MoneyDecimal {
    return new MoneyDecimal(value)
  }

  /**
   * Convierte a número de JavaScript (puede perder precisión)
   * @remarks Usar solo para integración con librerías que requieren number (ej: Recharts)
   * @returns Valor como number
   */
  toNumber(): number {
    return this.value.toNumber()
  }

  /**
   * Convierte a string con 2 decimales
   * @returns Valor como string (ej: "1234.56")
   */
  toString(): string {
    return this.value.toFixed(2)
  }

  /**
   * Convierte a string con formato JSON (para serialización)
   * @returns Valor como string
   */
  toJSON(): string {
    return this.toString()
  }

  /**
   * Obtiene el valor Decimal interno (para operaciones avanzadas)
   * @returns Instancia de Decimal
   */
  toDecimal(): decimal.Decimal {
    return this.value
  }

  /**
   * Suma dos valores monetarios
   * @param other - Valor a sumar
   * @returns Nuevo MoneyDecimal con el resultado
   */
  add(other: MoneyDecimal): MoneyDecimal {
    return new MoneyDecimal(this.value.plus(other.value))
  }

  /**
   * Resta dos valores monetarios
   * @param other - Valor a restar
   * @returns Nuevo MoneyDecimal con el resultado
   */
  subtract(other: MoneyDecimal): MoneyDecimal {
    return new MoneyDecimal(this.value.minus(other.value))
  }

  /**
   * Multiplica por un número o MoneyDecimal
   * @param multiplier - Multiplicador (number o MoneyDecimal)
   * @returns Nuevo MoneyDecimal con el resultado
   */
  multiply(multiplier: number | MoneyDecimal): MoneyDecimal {
    const value = typeof multiplier === 'number' 
      ? new Decimal(multiplier)
      : multiplier.value
    return new MoneyDecimal(this.value.times(value))
  }

  /**
   * Divide por un número o MoneyDecimal
   * @param divisor - Divisor (number o MoneyDecimal)
   * @returns Nuevo MoneyDecimal con el resultado redondeado a 2 decimales
   * @throws Error si el divisor es cero
   */
  divide(divisor: number | MoneyDecimal): MoneyDecimal {
    const value = typeof divisor === 'number' 
      ? new Decimal(divisor)
      : divisor.value
    
    if (value.isZero()) {
      throw new Error('Cannot divide by zero')
    }
    
    return new MoneyDecimal(
      this.value.div(value).toDecimalPlaces(2, Decimal.ROUND_HALF_UP)
    )
  }

  /**
   * Compara si es mayor que otro valor
   * @param other - Valor a comparar
   * @returns true si es mayor
   */
  greaterThan(other: MoneyDecimal): boolean {
    return this.value.greaterThan(other.value)
  }

  /**
   * Compara si es mayor o igual que otro valor
   * @param other - Valor a comparar
   * @returns true si es mayor o igual
   */
  greaterThanOrEqual(other: MoneyDecimal): boolean {
    return this.value.greaterThanOrEqualTo(other.value)
  }

  /**
   * Compara si es menor que otro valor
   * @param other - Valor a comparar
   * @returns true si es menor
   */
  lessThan(other: MoneyDecimal): boolean {
    return this.value.lessThan(other.value)
  }

  /**
   * Compara si es menor o igual que otro valor
   * @param other - Valor a comparar
   * @returns true si es menor o igual
   */
  lessThanOrEqual(other: MoneyDecimal): boolean {
    return this.value.lessThanOrEqualTo(other.value)
  }

  /**
   * Compara si es igual a otro valor
   * @param other - Valor a comparar
   * @returns true si son iguales
   */
  equals(other: MoneyDecimal): boolean {
    return this.value.equals(other.value)
  }

  /**
   * Verifica si el valor es cero
   * @returns true si es cero
   */
  isZero(): boolean {
    return this.value.isZero()
  }

  /**
   * Verifica si el valor es positivo
   * @returns true si es mayor que cero
   */
  isPositive(): boolean {
    return this.value.greaterThan(0)
  }

  /**
   * Verifica si el valor es negativo
   * @returns true si es menor que cero
   */
  isNegative(): boolean {
    return this.value.lessThan(0)
  }

  /**
   * Valor absoluto
   * @returns Nuevo MoneyDecimal con el valor absoluto
   */
  abs(): MoneyDecimal {
    return new MoneyDecimal(this.value.abs())
  }

  /**
   * Negación del valor
   * @returns Nuevo MoneyDecimal con el valor negado
   */
  negate(): MoneyDecimal {
    return new MoneyDecimal(this.value.negated())
  }

  /**
   * Convierte a string con decimales especificados
   * @param decimals - Número de decimales (default: 2)
   * @returns Valor como string con decimales fijos
   */
  toFixed(decimals: number = 2): string {
    return this.value.toFixed(decimals)
  }

  /**
   * Formatea como moneda con símbolo y separadores
   * @returns Valor formateado como "$1,234.56"
   */
  format(): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
    }).format(this.toNumber())
  }

  /**
   * Redondea a 2 decimales (ya es el comportamiento por defecto)
   * @returns Nuevo MoneyDecimal redondeado
   */
  round(): MoneyDecimal {
    return new MoneyDecimal(
      this.value.toDecimalPlaces(2, Decimal.ROUND_HALF_UP)
    )
  }

  // Constantes estáticas
  static readonly ZERO = new MoneyDecimal(new Decimal(0))
  static readonly ONE = new MoneyDecimal(new Decimal(1))
}

/**
 * Suma un array de valores monetarios
 * @param values - Array de MoneyDecimal
 * @returns Suma total como MoneyDecimal
 */
export function sumMoney(values: MoneyDecimal[]): MoneyDecimal {
  return values.reduce((acc, val) => acc.add(val), MoneyDecimal.ZERO)
}

/**
 * Encuentra el valor máximo en un array
 * @param values - Array de MoneyDecimal
 * @returns Valor máximo o ZERO si el array está vacío
 */
export function maxMoney(values: MoneyDecimal[]): MoneyDecimal {
  if (values.length === 0) return MoneyDecimal.ZERO
  return values.reduce((max, val) => val.greaterThan(max) ? val : max)
}

/**
 * Encuentra el valor mínimo en un array
 * @param values - Array de MoneyDecimal
 * @returns Valor mínimo o ZERO si el array está vacío
 */
export function minMoney(values: MoneyDecimal[]): MoneyDecimal {
  if (values.length === 0) return MoneyDecimal.ZERO
  return values.reduce((min, val) => val.lessThan(min) ? val : min)
}

/**
 * Calcula el promedio de un array de valores
 * @param values - Array de MoneyDecimal
 * @returns Promedio como MoneyDecimal o ZERO si el array está vacío
 */
export function averageMoney(values: MoneyDecimal[]): MoneyDecimal {
  if (values.length === 0) return MoneyDecimal.ZERO
  return sumMoney(values).divide(values.length)
}

/**
 * Distribuye un monto en N partes iguales, ajustando el último valor
 * para compensar errores de redondeo
 * 
 * @example
 * ```ts
 * // Dividir 1000 en 3 cuotas: [333.33, 333.33, 333.34]
 * const cuotas = distributeMoney(MoneyDecimal.fromString("1000"), 3)
 * ```
 * 
 * @param total - Monto total a distribuir
 * @param parts - Número de partes
 * @returns Array con las partes distribuidas
 */
export function distributeMoney(total: MoneyDecimal, parts: number): MoneyDecimal[] {
  if (parts <= 0) throw new Error('Parts must be greater than 0')
  if (parts === 1) return [total]

  const quotient = total.divide(parts)
  const result: MoneyDecimal[] = []
  
  // Crear N-1 partes con el cociente redondeado
  for (let i = 0; i < parts - 1; i++) {
    result.push(quotient)
  }
  
  // La última parte compensa el error de redondeo
  const sumOfParts = sumMoney(result)
  const lastPart = total.subtract(sumOfParts)
  result.push(lastPart)
  
  return result
}

/**
 * Type guard para verificar si un valor es MoneyDecimal
 * @param value - Valor a verificar
 * @returns true si es MoneyDecimal
 */
export function isMoneyDecimal(value: unknown): value is MoneyDecimal {
  return value instanceof MoneyDecimal
}
