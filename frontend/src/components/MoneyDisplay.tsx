import { toMoneyDecimal, type MoneyValue } from '@/types/money';
import { formatCurrency } from '@/lib/utils';
import { MoneyDecimal } from '@/lib/money';

interface MoneyDisplayProps {
  value: MoneyValue | null | undefined;
  /** Show currency symbol (default: true) */
  showCurrency?: boolean;
  /** Number of decimal places (default: 2) */
  decimals?: number;
  /** Fallback text when value is null/undefined (default: "$0.00") */
  fallback?: string;
  /** Additional CSS classes */
  className?: string;
  /** Show colored text based on value (green for positive, red for negative) */
  colored?: boolean;
}

/**
 * MoneyDisplay Component
 * 
 * Displays monetary values with proper decimal precision.
 * Accepts both number and MoneyDecimal for flexibility.
 * All formatting maintains full precision throughout the calculation chain.
 * 
 * @example
 * ```tsx
 * <MoneyDisplay value={workspace.saldo} />
 * <MoneyDisplay value={transaction.monto} colored />
 * <MoneyDisplay value={100.50} decimals={2} />
 * ```
 */
export function MoneyDisplay({
  value,
  showCurrency = true,
  decimals = 2,
  fallback = '$0.00',
  className = '',
  colored = false,
}: MoneyDisplayProps) {
  if (value === null || value === undefined) {
    return <span className={className}>{fallback}</span>;
  }

  const valueDecimal = toMoneyDecimal(value);

  const formattedValue = showCurrency
    ? formatCurrency(valueDecimal)
    : valueDecimal.toNumber().toFixed(decimals);

  // Determine color class if colored prop is true using native MoneyDecimal comparison
  const colorClass = colored
    ? valueDecimal.greaterThan(MoneyDecimal.fromNumber(0))
      ? 'text-green-600'
      : valueDecimal.lessThan(MoneyDecimal.fromNumber(0))
      ? 'text-red-600'
      : 'text-gray-600'
    : '';

  const combinedClassName = `${colorClass} ${className}`.trim();

  return <span className={combinedClassName}>{formattedValue}</span>;
}
