import { useCallback } from 'react';
import { MoneyDecimal } from '@/lib/money';
import { toMoneyDecimal, type MoneyValue } from '@/types/money';

/**
 * useMoney Hook
 * 
 * Provides common monetary operations and utilities using MoneyDecimal natively.
 * All operations maintain decimal precision throughout the calculation chain.
 * 
 * @example
 * ```tsx
 * const { compare, add, subtract, isPositive } = useMoney();
 * 
 * if (isPositive(balance)) {
 *   // ...
 * }
 * 
 * const total = add(amount1, amount2);
 * ```
 */
export function useMoney() {
  /**
   * Compare two monetary values
   * @returns -1 if a < b, 0 if a === b, 1 if a > b
   */
  const compare = useCallback((a: MoneyValue, b: MoneyValue): number => {
    const aDecimal = toMoneyDecimal(a);
    const bDecimal = toMoneyDecimal(b);
    if (aDecimal.lessThan(bDecimal)) return -1;
    if (aDecimal.greaterThan(bDecimal)) return 1;
    return 0;
  }, []);

  /**
   * Check if value is positive (> 0)
   */
  const isPositive = useCallback((value: MoneyValue): boolean => {
    return toMoneyDecimal(value).greaterThan(MoneyDecimal.fromNumber(0));
  }, []);

  /**
   * Check if value is negative (< 0)
   */
  const isNegative = useCallback((value: MoneyValue): boolean => {
    return toMoneyDecimal(value).lessThan(MoneyDecimal.fromNumber(0));
  }, []);

  /**
   * Check if value is zero
   */
  const isZero = useCallback((value: MoneyValue): boolean => {
    return toMoneyDecimal(value).equals(MoneyDecimal.fromNumber(0));
  }, []);

  /**
   * Check if first value is greater than second
   */
  const isGreaterThan = useCallback((a: MoneyValue, b: MoneyValue): boolean => {
    return toMoneyDecimal(a).greaterThan(toMoneyDecimal(b));
  }, []);

  /**
   * Check if first value is less than second
   */
  const isLessThan = useCallback((a: MoneyValue, b: MoneyValue): boolean => {
    return toMoneyDecimal(a).lessThan(toMoneyDecimal(b));
  }, []);

  /**
   * Add two or more monetary values
   */
  const add = useCallback((...values: MoneyValue[]): MoneyDecimal => {
    return values.reduce((sum: MoneyDecimal, value) => {
      return sum.add(toMoneyDecimal(value));
    }, MoneyDecimal.fromNumber(0));
  }, []);

  /**
   * Subtract second value from first
   */
  const subtract = useCallback((a: MoneyValue, b: MoneyValue): MoneyDecimal => {
    return toMoneyDecimal(a).subtract(toMoneyDecimal(b));
  }, []);

  /**
   * Multiply a monetary value by a factor
   */
  const multiply = useCallback((value: MoneyValue, factor: number): MoneyDecimal => {
    return toMoneyDecimal(value).multiply(factor);
  }, []);

  /**
   * Divide a monetary value by a divisor
   */
  const divide = useCallback((value: MoneyValue, divisor: number): MoneyDecimal => {
    return toMoneyDecimal(value).divide(divisor);
  }, []);

  /**
   * Get the absolute value
   */
  const abs = useCallback((value: MoneyValue): MoneyDecimal => {
    return toMoneyDecimal(value).abs();
  }, []);

  /**
   * Sum an array of monetary values
   */
  const sum = useCallback((values: MoneyValue[]): MoneyDecimal => {
    return values.reduce((total: MoneyDecimal, value) => {
      return total.add(toMoneyDecimal(value));
    }, MoneyDecimal.fromNumber(0));
  }, []);

  /**
   * Calculate average of monetary values
   */
  const average = useCallback((values: MoneyValue[]): MoneyDecimal => {
    if (values.length === 0) return MoneyDecimal.fromNumber(0);
    const total = sum(values);
    return total.divide(values.length);
  }, [sum]);

  /**
   * Find maximum value using native MoneyDecimal comparison
   */
  const max = useCallback((...values: MoneyValue[]): MoneyDecimal => {
    if (values.length === 0) return MoneyDecimal.fromNumber(0);
    return values.map(toMoneyDecimal).reduce((maxVal, current) => 
      current.greaterThan(maxVal) ? current : maxVal
    );
  }, []);

  /**
   * Find minimum value using native MoneyDecimal comparison
   */
  const min = useCallback((...values: MoneyValue[]): MoneyDecimal => {
    if (values.length === 0) return MoneyDecimal.fromNumber(0);
    return values.map(toMoneyDecimal).reduce((minVal, current) => 
      current.lessThan(minVal) ? current : minVal
    );
  }, []);

  return {
    // Comparisons
    compare,
    isPositive,
    isNegative,
    isZero,
    isGreaterThan,
    isLessThan,
    
    // Arithmetic
    add,
    subtract,
    multiply,
    divide,
    abs,
    
    // Aggregations
    sum,
    average,
    max,
    min,
  };
}
