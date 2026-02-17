import { forwardRef, InputHTMLAttributes, useCallback, useState, useEffect } from 'react';
import { Input } from './ui/input';

export interface MoneyInputProps
  extends Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange' | 'value' | 'type'> {
  /** Current value (can be number or MoneyDecimal) */
  value?: number | null;
  /** Callback when value changes - receives the numeric value */
  onChange?: (value: number | null) => void;
  /** Show currency symbol prefix (default: true) */
  showPrefix?: boolean;
  /** Minimum allowed value */
  min?: number;
  /** Maximum allowed value */
  max?: number;
  /** Allow negative values (default: false) */
  allowNegative?: boolean;
  /** Maximum number of integer digits (default: 12) */
  maxDigits?: number;
  /** Maximum number of decimal digits (default: 2) */
  maxDecimals?: number;
}

/**
 * MoneyInput Component
 * 
 * A specialized input field for monetary values.
 * Handles parsing, validation, and formatting automatically.
 * 
 * @example
 * ```tsx
 * <MoneyInput
 *   value={amount}
 *   onChange={(value) => setAmount(value)}
 *   min={0}
 *   placeholder="0.00"
 * />
 * ```
 */
export const MoneyInput = forwardRef<HTMLInputElement, MoneyInputProps>(
  (
    {
      value,
      onChange,
      showPrefix = true,
      min,
      max,
      allowNegative = false,
      maxDigits = 12,
      maxDecimals = 2,
      className = '',
      ...props
    },
    ref
  ) => {
    // Local state to preserve intermediate input like "12." or "12.5"
    const [localValue, setLocalValue] = useState<string>('');
    const [isFocused, setIsFocused] = useState(false);

    // Sync local value with prop value when it changes externally (but not while user is typing)
    useEffect(() => {
      // Only update from prop if not currently focused (user is not typing)
      if (!isFocused) {
        if (value !== null && value !== undefined) {
          setLocalValue(String(value));
        } else {
          setLocalValue('');
        }
      }
    }, [value, isFocused]);

    const handleChange = useCallback(
      (e: React.ChangeEvent<HTMLInputElement>) => {
        const inputValue = e.target.value;

        // Allow empty input
        if (inputValue === '' || inputValue === '-') {
          setLocalValue(inputValue);
          onChange?.(null);
          return;
        }

        // Remove currency symbols and whitespace
        const cleanValue = inputValue.replace(/[$\s,]/g, '');

        // Validate number format (allow trailing dot like "12.")
        const numberPattern = allowNegative ? /^-?\d*\.?\d*$/ : /^\d*\.?\d*$/;
        if (!numberPattern.test(cleanValue)) {
          return; // Invalid input, don't update
        }

        // Validate digits length
        const parts = cleanValue.replace(/^-/, '').split('.');
        const integerPart = parts[0] || '';
        const decimalPart = parts[1] || '';

        if (integerPart.length > maxDigits) {
          return; // Too many integer digits
        }

        if (decimalPart.length > maxDecimals) {
          return; // Too many decimal digits
        }

        // Update local display value immediately
        setLocalValue(cleanValue);

        // Only parse and callback if it's a complete number (not ending in ".")
        if (cleanValue.endsWith('.')) {
          // Don't callback yet, wait for user to type decimals
          return;
        }

        // Parse to number
        const parsed = parseFloat(cleanValue);
        if (isNaN(parsed)) {
          onChange?.(null);
          return;
        }

        // Apply min/max constraints
        let finalValue = parsed;
        if (min !== undefined && finalValue < min) {
          finalValue = min;
        }
        if (max !== undefined && finalValue > max) {
          finalValue = max;
        }

        // Ensure non-negative if allowNegative is false
        if (!allowNegative && finalValue < 0) {
          finalValue = 0;
        }

        onChange?.(finalValue);
      },
      [onChange, min, max, allowNegative, maxDigits, maxDecimals]
    );

    const handleFocus = useCallback(
      (e: React.FocusEvent<HTMLInputElement>) => {
        setIsFocused(true);
        props.onFocus?.(e);
      },
      [props.onFocus]
    );

    // Handle blur - ensure we have a valid number when user leaves field
    const handleBlur = useCallback(
      (e: React.FocusEvent<HTMLInputElement>) => {
        setIsFocused(false);
        
        if (localValue && !localValue.endsWith('.')) {
          const parsed = parseFloat(localValue);
          if (!isNaN(parsed)) {
            // Format cleanly on blur
            setLocalValue(String(parsed));
          }
        } else if (localValue.endsWith('.')) {
          // Remove trailing dot on blur
          const withoutDot = localValue.slice(0, -1);
          setLocalValue(withoutDot);
          if (withoutDot) {
            const parsed = parseFloat(withoutDot);
            if (!isNaN(parsed)) {
              onChange?.(parsed);
            }
          }
        }
        
        // Call original onBlur if provided
        props.onBlur?.(e);
      },
      [localValue, onChange, props.onBlur]
    );

    return (
      <div className="relative">
        {showPrefix && (
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500">
            $
          </span>
        )}
        <Input
          ref={ref}
          type="text"
          inputMode="decimal"
          value={localValue}
          onChange={handleChange}
          onFocus={handleFocus}
          onBlur={handleBlur}
          className={`${showPrefix ? 'pl-7' : ''} ${className}`}
          {...props}
        />
      </div>
    );
  }
);

MoneyInput.displayName = 'MoneyInput';
