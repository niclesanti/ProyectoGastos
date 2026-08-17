import { describe, it, expect } from 'vitest'
import { normalizeDecimalSeparator } from '@/lib/utils'

describe('normalizeDecimalSeparator', () => {
  it('should keep plain integers unchanged', () => {
    expect(normalizeDecimalSeparator('123')).toBe('123')
  })

  it('should keep dot as decimal separator', () => {
    expect(normalizeDecimalSeparator('12.34')).toBe('12.34')
  })

  it('should convert comma to dot as decimal separator', () => {
    expect(normalizeDecimalSeparator('12,34')).toBe('12.34')
  })

  it('should keep trailing dot', () => {
    expect(normalizeDecimalSeparator('12.')).toBe('12.')
  })

  it('should convert trailing comma to dot', () => {
    expect(normalizeDecimalSeparator('12,')).toBe('12.')
  })

  it('should treat comma as thousands separator when dot is decimal', () => {
    expect(normalizeDecimalSeparator('1,234.56')).toBe('1234.56')
  })

  it('should treat dot as thousands separator when comma is decimal', () => {
    expect(normalizeDecimalSeparator('1.234,56')).toBe('1234.56')
  })

  it('should remove currency symbols and whitespace', () => {
    expect(normalizeDecimalSeparator('$ 1,234.56')).toBe('1234.56')
  })

  it('should handle negative values', () => {
    expect(normalizeDecimalSeparator('-12,5')).toBe('-12.5')
  })

  it('should handle empty string', () => {
    expect(normalizeDecimalSeparator('')).toBe('')
  })
})