import { describe, it, expect } from 'vitest'
import { renderHook } from '@testing-library/react'
import { useMoney } from '@/hooks/useMoney'
import { MoneyDecimal } from '@/lib/money'

describe('useMoney', () => {
  describe('Comparisons', () => {
    it('should compare two values', () => {
      const { result } = renderHook(() => useMoney())
      
      expect(result.current.compare(100, 50)).toBe(1)
      expect(result.current.compare(50, 100)).toBe(-1)
      expect(result.current.compare(100, 100)).toBe(0)
    })

    it('should check if value is positive', () => {
      const { result } = renderHook(() => useMoney())
      
      expect(result.current.isPositive(100)).toBe(true)
      expect(result.current.isPositive(-50)).toBe(false)
      expect(result.current.isPositive(0)).toBe(false)
    })

    it('should check if value is negative', () => {
      const { result } = renderHook(() => useMoney())
      
      expect(result.current.isNegative(-50)).toBe(true)
      expect(result.current.isNegative(100)).toBe(false)
      expect(result.current.isNegative(0)).toBe(false)
    })

    it('should check if value is zero', () => {
      const { result } = renderHook(() => useMoney())
      
      expect(result.current.isZero(0)).toBe(true)
      expect(result.current.isZero(100)).toBe(false)
      expect(result.current.isZero(-50)).toBe(false)
    })

    it('should check if value is greater than another', () => {
      const { result } = renderHook(() => useMoney())
      
      expect(result.current.isGreaterThan(100, 50)).toBe(true)
      expect(result.current.isGreaterThan(50, 100)).toBe(false)
      expect(result.current.isGreaterThan(100, 100)).toBe(false)
    })

    it('should check if value is less than another', () => {
      const { result } = renderHook(() => useMoney())
      
      expect(result.current.isLessThan(50, 100)).toBe(true)
      expect(result.current.isLessThan(100, 50)).toBe(false)
      expect(result.current.isLessThan(100, 100)).toBe(false)
    })

    it('should work with MoneyDecimal values', () => {
      const { result } = renderHook(() => useMoney())
      const a = MoneyDecimal.fromNumber(100)
      const b = MoneyDecimal.fromNumber(50)
      
      expect(result.current.isGreaterThan(a, b)).toBe(true)
      expect(result.current.isLessThan(b, a)).toBe(true)
    })
  })

  describe('Arithmetic', () => {
    it('should add multiple values', () => {
      const { result } = renderHook(() => useMoney())
      
      const total = result.current.add(100, 50, 25)
      expect(total.toNumber()).toBe(175)
    })

    it('should add empty values', () => {
      const { result } = renderHook(() => useMoney())
      
      const total = result.current.add()
      expect(total.toNumber()).toBe(0)
    })

    it('should subtract values', () => {
      const { result } = renderHook(() => useMoney())
      
      const result_value = result.current.subtract(100, 30)
      expect(result_value.toNumber()).toBe(70)
    })

    it('should multiply values', () => {
      const { result } = renderHook(() => useMoney())
      
      const result_value = result.current.multiply(50, 2)
      expect(result_value.toNumber()).toBe(100)
    })

    it('should divide values', () => {
      const { result } = renderHook(() => useMoney())
      
      const result_value = result.current.divide(100, 4)
      expect(result_value.toNumber()).toBe(25)
    })

    it('should calculate absolute value', () => {
      const { result } = renderHook(() => useMoney())
      
      expect(result.current.abs(-50).toNumber()).toBe(50)
      expect(result.current.abs(50).toNumber()).toBe(50)
    })

    it('should work with mixed number and MoneyDecimal types', () => {
      const { result } = renderHook(() => useMoney())
      const moneyDecimal = MoneyDecimal.fromNumber(50)
      
      const total = result.current.add(100, moneyDecimal, 25)
      expect(total.toNumber()).toBe(175)
    })
  })

  describe('Aggregations', () => {
    it('should sum array of values', () => {
      const { result } = renderHook(() => useMoney())
      
      const values = [100, 50, 25, 75]
      const total = result.current.sum(values)
      expect(total.toNumber()).toBe(250)
    })

    it('should sum empty array', () => {
      const { result } = renderHook(() => useMoney())
      
      const total = result.current.sum([])
      expect(total.toNumber()).toBe(0)
    })

    it('should calculate average', () => {
      const { result } = renderHook(() => useMoney())
      
      const values = [100, 200, 300]
      const avg = result.current.average(values)
      expect(avg.toNumber()).toBe(200)
    })

    it('should handle average of empty array', () => {
      const { result } = renderHook(() => useMoney())
      
      const avg = result.current.average([])
      expect(avg.toNumber()).toBe(0)
    })

    it('should find maximum value', () => {
      const { result } = renderHook(() => useMoney())
      
      const maxValue = result.current.max(100, 50, 200, 75)
      expect(maxValue.toNumber()).toBe(200)
    })

    it('should find minimum value', () => {
      const { result } = renderHook(() => useMoney())
      
      const minValue = result.current.min(100, 50, 200, 75)
      expect(minValue.toNumber()).toBe(50)
    })

    it('should handle max with no values', () => {
      const { result } = renderHook(() => useMoney())
      
      const maxValue = result.current.max()
      expect(maxValue.toNumber()).toBe(0)
    })

    it('should handle min with no values', () => {
      const { result } = renderHook(() => useMoney())
      
      const minValue = result.current.min()
      expect(minValue.toNumber()).toBe(0)
    })

    it('should work with MoneyDecimal values in aggregations', () => {
      const { result } = renderHook(() => useMoney())
      const values = [
        MoneyDecimal.fromNumber(100),
        MoneyDecimal.fromNumber(50),
        50, // Mixed with number
      ]
      
      const total = result.current.sum(values)
      expect(total.toNumber()).toBe(200)
    })
  })


  describe('Precision handling', () => {
    it('should maintain precision in additions', () => {
      const { result } = renderHook(() => useMoney())
      
      const total = result.current.add(0.1, 0.2)
      expect(total.toNumber()).toBe(0.3) // Not 0.30000000000000004
    })

    it('should maintain precision in sum', () => {
      const { result } = renderHook(() => useMoney())
      
      const total = result.current.sum([0.1, 0.2, 0.3])
      expect(total.toNumber()).toBe(0.6)
    })

    it('should round division results properly', () => {
      const { result } = renderHook(() => useMoney())
      
      const quotient = result.current.divide(10, 3)
      expect(quotient.toNumber()).toBe(3.33)
    })
  })

  describe('Real-world scenarios', () => {
    it('should calculate transaction totals correctly', () => {
      const { result } = renderHook(() => useMoney())
      
      const transactions = [
        MoneyDecimal.fromNumber(1234.56),
        MoneyDecimal.fromNumber(567.89),
        MoneyDecimal.fromNumber(890.12),
      ]
      
      const total = result.current.sum(transactions)
      expect(total.toNumber()).toBe(2692.57)
    })

    it('should validate sufficient balance', () => {
      const { result } = renderHook(() => useMoney())
      
      const balance = MoneyDecimal.fromNumber(1000)
      const paymentAmount = MoneyDecimal.fromNumber(750)
      
      const hasSufficientFunds = result.current.isGreaterThan(balance, paymentAmount)
      expect(hasSufficientFunds).toBe(true)
      
      const remaining = result.current.subtract(balance, paymentAmount)
      expect(remaining.toNumber()).toBe(250)
    })

    it('should calculate installment amounts', () => {
      const { result } = renderHook(() => useMoney())
      
      const totalAmount = MoneyDecimal.fromNumber(1200)
      const installments = 12
      
      const monthlyPayment = result.current.divide(totalAmount, installments)
      expect(monthlyPayment.toNumber()).toBe(100)
    })

    it('should calculate monthly averages', () => {
      const { result } = renderHook(() => useMoney())
      
      const expenses = [
        MoneyDecimal.fromNumber(2500),
        MoneyDecimal.fromNumber(2800),
        MoneyDecimal.fromNumber(2200),
        MoneyDecimal.fromNumber(2600),
      ]
      
      const average = result.current.average(expenses)
      expect(average.toNumber()).toBe(2525)
    })
  })
})
