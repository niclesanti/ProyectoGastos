import { describe, it, expect } from 'vitest'
import { MoneyDecimal } from '@/lib/money'

describe('MoneyDecimal', () => {
  describe('Factory methods', () => {
    it('should create from number', () => {
      const money = MoneyDecimal.fromNumber(100.5)
      expect(money.toNumber()).toBe(100.5)
    })

    it('should create from string', () => {
      const money = MoneyDecimal.fromString('250.75')
      expect(money.toNumber()).toBe(250.75)
    })

    it('should handle zero', () => {
      const money = MoneyDecimal.fromNumber(0)
      expect(money.toNumber()).toBe(0)
    })

    it('should handle negative numbers', () => {
      const money = MoneyDecimal.fromNumber(-50.25)
      expect(money.toNumber()).toBe(-50.25)
    })

    it('should handle large numbers', () => {
      const money = MoneyDecimal.fromNumber(1000000.99)
      expect(money.toNumber()).toBe(1000000.99)
    })
  })

  describe('Arithmetic operations', () => {
    it('should add two monetary values', () => {
      const a = MoneyDecimal.fromNumber(100)
      const b = MoneyDecimal.fromNumber(50)
      const result = a.add(b)
      expect(result.toNumber()).toBe(150)
    })

    it('should subtract two monetary values', () => {
      const a = MoneyDecimal.fromNumber(100)
      const b = MoneyDecimal.fromNumber(30)
      const result = a.subtract(b)
      expect(result.toNumber()).toBe(70)
    })

    it('should multiply by a number', () => {
      const money = MoneyDecimal.fromNumber(50)
      const result = money.multiply(2)
      expect(result.toNumber()).toBe(100)
    })

    it('should multiply by another MoneyDecimal', () => {
      const a = MoneyDecimal.fromNumber(10)
      const b = MoneyDecimal.fromNumber(5)
      const result = a.multiply(b)
      expect(result.toNumber()).toBe(50)
    })

    it('should divide by a number', () => {
      const money = MoneyDecimal.fromNumber(100)
      const result = money.divide(4)
      expect(result.toNumber()).toBe(25)
    })

    it('should divide by another MoneyDecimal', () => {
      const a = MoneyDecimal.fromNumber(100)
      const b = MoneyDecimal.fromNumber(4)
      const result = a.divide(b)
      expect(result.toNumber()).toBe(25)
    })

    it('should handle division with rounding', () => {
      const money = MoneyDecimal.fromNumber(100)
      const result = money.divide(3)
      // Should round to 2 decimals with ROUND_HALF_UP
      expect(result.toNumber()).toBe(33.33)
    })

    it('should calculate absolute value', () => {
      const negative = MoneyDecimal.fromNumber(-50)
      expect(negative.abs().toNumber()).toBe(50)

      const positive = MoneyDecimal.fromNumber(50)
      expect(positive.abs().toNumber()).toBe(50)
    })

    it('should negate value', () => {
      const money = MoneyDecimal.fromNumber(100)
      const result = money.negate()
      expect(result.toNumber()).toBe(-100)
    })
  })

  describe('Comparison operations', () => {
    it('should check equality', () => {
      const a = MoneyDecimal.fromNumber(100)
      const b = MoneyDecimal.fromNumber(100)
      const c = MoneyDecimal.fromNumber(50)

      expect(a.equals(b)).toBe(true)
      expect(a.equals(c)).toBe(false)
    })

    it('should compare greater than', () => {
      const a = MoneyDecimal.fromNumber(100)
      const b = MoneyDecimal.fromNumber(50)

      expect(a.greaterThan(b)).toBe(true)
      expect(b.greaterThan(a)).toBe(false)
    })

    it('should compare greater than or equal', () => {
      const a = MoneyDecimal.fromNumber(100)
      const b = MoneyDecimal.fromNumber(100)
      const c = MoneyDecimal.fromNumber(50)

      expect(a.greaterThanOrEqual(b)).toBe(true)
      expect(a.greaterThanOrEqual(c)).toBe(true)
      expect(c.greaterThanOrEqual(a)).toBe(false)
    })

    it('should compare less than', () => {
      const a = MoneyDecimal.fromNumber(50)
      const b = MoneyDecimal.fromNumber(100)

      expect(a.lessThan(b)).toBe(true)
      expect(b.lessThan(a)).toBe(false)
    })

    it('should compare less than or equal', () => {
      const a = MoneyDecimal.fromNumber(50)
      const b = MoneyDecimal.fromNumber(50)
      const c = MoneyDecimal.fromNumber(100)

      expect(a.lessThanOrEqual(b)).toBe(true)
      expect(a.lessThanOrEqual(c)).toBe(true)
      expect(c.lessThanOrEqual(a)).toBe(false)
    })
  })

  describe('Helper methods', () => {
    it('should check if zero', () => {
      const zero = MoneyDecimal.fromNumber(0)
      const nonZero = MoneyDecimal.fromNumber(100)

      expect(zero.isZero()).toBe(true)
      expect(nonZero.isZero()).toBe(false)
    })

    it('should check if positive', () => {
      const positive = MoneyDecimal.fromNumber(100)
      const negative = MoneyDecimal.fromNumber(-50)
      const zero = MoneyDecimal.fromNumber(0)

      expect(positive.isPositive()).toBe(true)
      expect(negative.isPositive()).toBe(false)
      expect(zero.isPositive()).toBe(false)
    })

    it('should check if negative', () => {
      const positive = MoneyDecimal.fromNumber(100)
      const negative = MoneyDecimal.fromNumber(-50)
      const zero = MoneyDecimal.fromNumber(0)

      expect(positive.isNegative()).toBe(false)
      expect(negative.isNegative()).toBe(true)
      expect(zero.isNegative()).toBe(false)
    })

    it('should convert to string', () => {
      const money = MoneyDecimal.fromNumber(123.45)
      expect(money.toString()).toBe('123.45')
    })

    it('should convert to string with specified decimals', () => {
      const money = MoneyDecimal.fromNumber(123.456)
      expect(money.toFixed(2)).toBe('123.46')
    })

    it('should format as currency', () => {
      const money = MoneyDecimal.fromNumber(1234.56)
      expect(money.format()).toBe('$1,234.56')
    })
  })

  describe('Precision handling', () => {
    it('should maintain precision in addition', () => {
      const a = MoneyDecimal.fromNumber(0.1)
      const b = MoneyDecimal.fromNumber(0.2)
      const result = a.add(b)
      expect(result.toNumber()).toBe(0.3) // Not 0.30000000000000004
    })

    it('should maintain precision in multiplication', () => {
      const money = MoneyDecimal.fromNumber(0.1)
      const result = money.multiply(3)
      expect(result.toNumber()).toBe(0.3) // Not 0.30000000000000004
    })

    it('should round to 2 decimals in division', () => {
      const money = MoneyDecimal.fromNumber(10)
      const result = money.divide(3)
      expect(result.toNumber()).toBe(3.33)
    })
  })

  describe('Edge cases', () => {
    it('should handle very small numbers', () => {
      const money = MoneyDecimal.fromNumber(0.01)
      expect(money.toNumber()).toBe(0.01)
    })

    it('should handle division by zero gracefully', () => {
      const money = MoneyDecimal.fromNumber(100)
      expect(() => money.divide(0)).toThrow()
    })

    it('should handle invalid string input', () => {
      expect(() => MoneyDecimal.fromString('invalid')).toThrow()
    })

    it('should handle empty string', () => {
      expect(() => MoneyDecimal.fromString('')).toThrow()
    })

    it('should handle NaN', () => {
      expect(() => MoneyDecimal.fromNumber(NaN)).toThrow()
    })

    it('should handle Infinity', () => {
      expect(() => MoneyDecimal.fromNumber(Infinity)).toThrow()
    })
  })

  describe('Immutability', () => {
    it('should not mutate original value on addition', () => {
      const original = MoneyDecimal.fromNumber(100)
      const other = MoneyDecimal.fromNumber(50)
      const result = original.add(other)

      expect(original.toNumber()).toBe(100)
      expect(result.toNumber()).toBe(150)
    })

    it('should not mutate original value on subtraction', () => {
      const original = MoneyDecimal.fromNumber(100)
      const other = MoneyDecimal.fromNumber(30)
      const result = original.subtract(other)

      expect(original.toNumber()).toBe(100)
      expect(result.toNumber()).toBe(70)
    })

    it('should not mutate original value on multiplication', () => {
      const original = MoneyDecimal.fromNumber(50)
      const result = original.multiply(2)

      expect(original.toNumber()).toBe(50)
      expect(result.toNumber()).toBe(100)
    })
  })
})
