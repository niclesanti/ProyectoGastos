/**
 * Money Module - Barrel Export
 * 
 * Centralized exports for monetary utilities, components, and hooks.
 * Import from this file for cleaner imports across the application.
 * 
 * @example
 * ```tsx
 * import { MoneyDecimal, MoneyDisplay, useMoney } from '@/lib/money'
 * ```
 */

// Core Money Class
export { MoneyDecimal } from './money'

// Type definitions
export type { MoneyValue, Monto, Saldo, Total, Cuota } from '@/types/money'

// Components
export { MoneyDisplay } from '@/components/MoneyDisplay'
export { MoneyInput } from '@/components/MoneyInput'

// Hooks
export { useMoney } from '@/hooks/useMoney'

// Utilities
export { formatCurrency } from './utils'
