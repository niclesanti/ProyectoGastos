import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { MoneyInput } from '@/components/MoneyInput'

describe('MoneyInput', () => {
  describe('Basic rendering', () => {
    it('should render input field', () => {
      render(<MoneyInput value={null} onChange={() => {}} />)
      const input = screen.getByRole('textbox')
      expect(input).toBeInTheDocument()
    })

    it('should display currency prefix by default', () => {
      const { container } = render(<MoneyInput value={null} onChange={() => {}} />)
      const prefix = container.querySelector('span')
      expect(prefix).toHaveTextContent('$')
    })

    it('should hide currency prefix when showPrefix is false', () => {
      const { container } = render(
        <MoneyInput value={null} onChange={() => {}} showPrefix={false} />
      )
      const prefix = container.querySelector('span')
      expect(prefix).not.toBeInTheDocument()
    })

    it('should display value', () => {
      render(<MoneyInput value={123.45} onChange={() => {}} />)
      const input = screen.getByRole('textbox') as HTMLInputElement
      expect(input.value).toBe('123.45')
    })

    it('should display empty string for null value', () => {
      render(<MoneyInput value={null} onChange={() => {}} />)
      const input = screen.getByRole('textbox') as HTMLInputElement
      expect(input.value).toBe('')
    })
  })

  describe('User input', () => {
    it('should call onChange with parsed number', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '123.45' } })
      
      expect(onChange).toHaveBeenCalledWith(123.45)
    })

    it('should handle clearing input', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={100} onChange={onChange} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '' } })
      
      expect(onChange).toHaveBeenCalledWith(null)
    })

    it('should allow decimal input', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '50.99' } })
      
      expect(onChange).toHaveBeenCalledWith(50.99)
    })

    it('should filter out invalid characters', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: 'abc123def' } })
      
      // Should not call onChange with invalid input
      expect(onChange).not.toHaveBeenCalled()
    })

    it('should handle input with currency symbols', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '$100' } })
      
      expect(onChange).toHaveBeenCalledWith(100)
    })

    it('should handle input with commas', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '1,234.56' } })
      
      expect(onChange).toHaveBeenCalledWith(1234.56)
    })
  })

  describe('Validation - Min/Max', () => {
    it('should enforce minimum value', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} min={10} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '5' } })
      
      // Should clamp to minimum
      expect(onChange).toHaveBeenCalledWith(10)
    })

    it('should enforce maximum value', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} max={100} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '150' } })
      
      // Should clamp to maximum
      expect(onChange).toHaveBeenCalledWith(100)
    })

    it('should accept value within range', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} min={10} max={100} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '50' } })
      
      expect(onChange).toHaveBeenCalledWith(50)
    })
  })

  describe('Negative values', () => {
    it('should block negative values by default', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '-50' } })
      
      // Should not call onChange because negative sign is filtered out
      expect(onChange).not.toHaveBeenCalled()
    })

    it('should allow negative values when allowNegative is true', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} allowNegative />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '-50' } })
      
      expect(onChange).toHaveBeenCalledWith(-50)
    })

    it('should handle typing minus sign alone', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} allowNegative />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '-' } })
      
      // Should call onChange with null for incomplete input
      expect(onChange).toHaveBeenCalledWith(null)
    })
  })

  describe('Props forwarding', () => {
    it('should forward placeholder prop', () => {
      render(<MoneyInput value={null} onChange={() => {}} placeholder="Enter amount" />)
      const input = screen.getByPlaceholderText('Enter amount')
      expect(input).toBeInTheDocument()
    })

    it('should forward disabled prop', () => {
      render(<MoneyInput value={null} onChange={() => {}} disabled />)
      const input = screen.getByRole('textbox')
      expect(input).toBeDisabled()
    })

    it('should forward className prop', () => {
      const { container } = render(
        <MoneyInput value={null} onChange={() => {}} className="custom-class" />
      )
      const input = container.querySelector('input')
      expect(input).toHaveClass('custom-class')
    })
  })

  describe('Input mode', () => {
    it('should set inputMode to decimal', () => {
      render(<MoneyInput value={null} onChange={() => {}} />)
      const input = screen.getByRole('textbox')
      expect(input).toHaveAttribute('inputMode', 'decimal')
    })
  })

  describe('Edge cases', () => {
    it('should handle very large numbers', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '999999999.99' } })
      
      expect(onChange).toHaveBeenCalledWith(999999999.99)
    })

    it('should handle very small decimal values', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '0.01' } })
      
      expect(onChange).toHaveBeenCalledWith(0.01)
    })

    it('should handle multiple decimal points', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '12.34.56' } })
      
      // Should not call onChange with invalid format
      expect(onChange).not.toHaveBeenCalled()
    })

    it('should handle zero', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '0' } })
      
      expect(onChange).toHaveBeenCalledWith(0)
    })
  })

  describe('Real-world scenarios', () => {
    it('should handle transaction amount entry', () => {
      const onChange = vi.fn()
      render(
        <MoneyInput
          value={null}
          onChange={onChange}
          min={0}
          placeholder="0.00"
        />
      )
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '150.50' } })
      
      expect(onChange).toHaveBeenCalledWith(150.5)
    })

    it('should validate payment amount against balance', () => {
      const onChange = vi.fn()
      const maxBalance = 1000
      
      render(
        <MoneyInput
          value={null}
          onChange={onChange}
          min={0}
          max={maxBalance}
        />
      )
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '1500' } })
      
      // Should clamp to max balance
      expect(onChange).toHaveBeenCalledWith(1000)
    })

    it('should handle credit card payment entry', () => {
      const onChange = vi.fn()
      
      render(
        <MoneyInput
          value={null}
          onChange={onChange}
          min={0}
          placeholder="Monto a pagar"
        />
      )
      
      const input = screen.getByPlaceholderText('Monto a pagar')
      fireEvent.change(input, { target: { value: '2500.75' } })
      
      expect(onChange).toHaveBeenCalledWith(2500.75)
    })
  })

  describe('onChange behavior', () => {
    it('should not call onChange for invalid input', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: 'abc' } })
      
      // Invalid characters should be filtered before onChange
      expect(onChange).not.toHaveBeenCalled()
    })

    it('should call onChange for valid numeric input', () => {
      const onChange = vi.fn()
      render(<MoneyInput value={null} onChange={onChange} />)
      
      const input = screen.getByRole('textbox')
      fireEvent.change(input, { target: { value: '123' } })
      
      expect(onChange).toHaveBeenCalledWith(123)
    })
  })
})
