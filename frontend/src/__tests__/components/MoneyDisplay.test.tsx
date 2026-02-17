import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MoneyDisplay } from '@/components/MoneyDisplay'
import { MoneyDecimal } from '@/lib/money'

describe('MoneyDisplay', () => {
  describe('Basic rendering', () => {
    it('should render monetary value with default props', () => {
      render(<MoneyDisplay value={1234.56} />)
      // Accept both en-US ($1,234.56) and es-AR ($ 1.234,56) formats
      expect(screen.getByText(/\$\s?1[.,]234[.,]56/)).toBeInTheDocument()
    })

    it('should render MoneyDecimal value', () => {
      const money = MoneyDecimal.fromNumber(9876.54)
      render(<MoneyDisplay value={money} />)
      expect(screen.getByText(/\$\s?9[.,]876[.,]54/)).toBeInTheDocument()
    })

    it('should render zero', () => {
      render(<MoneyDisplay value={0} />)
      expect(screen.getByText(/\$\s?0[.,]00/)).toBeInTheDocument()
    })

    it('should render negative value', () => {
      render(<MoneyDisplay value={-123.45} />)
      expect(screen.getByText(/-\$\s?123[.,]45/)).toBeInTheDocument()
    })
  })

  describe('Currency symbol', () => {
    it('should show currency symbol by default', () => {
      render(<MoneyDisplay value={100} />)
      const text = screen.getByText(/\$\s?100[.,]00/)
      expect(text).toBeInTheDocument()
    })

    it('should hide currency symbol when showCurrency is false', () => {
      render(<MoneyDisplay value={100} showCurrency={false} />)
      expect(screen.getByText('100.00')).toBeInTheDocument()
    })
  })

  describe('Decimal places', () => {
    it('should use 2 decimals by default', () => {
      render(<MoneyDisplay value={100} />)
      expect(screen.getByText(/\$\s?100[.,]00/)).toBeInTheDocument()
    })

    it('should respect custom decimal places with showCurrency false', () => {
      render(<MoneyDisplay value={100.123} decimals={3} showCurrency={false} />)
      expect(screen.getByText('100.123')).toBeInTheDocument()
    })

    it('should round to specified decimals', () => {
      render(<MoneyDisplay value={100.456} decimals={2} showCurrency={false} />)
      expect(screen.getByText('100.46')).toBeInTheDocument()
    })
  })

  describe('Null and undefined handling', () => {
    it('should show fallback for null value', () => {
      render(<MoneyDisplay value={null} />)
      expect(screen.getByText(/\$\s?0[.,]00/)).toBeInTheDocument()
    })

    it('should show fallback for undefined value', () => {
      render(<MoneyDisplay value={undefined} />)
      expect(screen.getByText(/\$\s?0[.,]00/)).toBeInTheDocument()
    })

    it('should show custom fallback', () => {
      render(<MoneyDisplay value={null} fallback="N/A" />)
      expect(screen.getByText('N/A')).toBeInTheDocument()
    })

    it('should show custom fallback for undefined', () => {
      render(<MoneyDisplay value={undefined} fallback="--" />)
      expect(screen.getByText('--')).toBeInTheDocument()
    })
  })

  describe('CSS classes', () => {
    it('should apply custom className', () => {
      const { container } = render(
        <MoneyDisplay value={100} className="custom-class" />
      )
      const span = container.querySelector('span')
      expect(span).toHaveClass('custom-class')
    })

    it('should not apply color classes by default', () => {
      const { container } = render(<MoneyDisplay value={100} />)
      const span = container.querySelector('span')
      expect(span).not.toHaveClass('text-green-600')
      expect(span).not.toHaveClass('text-red-600')
    })
  })

  describe('Colored display', () => {
    it('should show green for positive values when colored is true', () => {
      const { container } = render(<MoneyDisplay value={100} colored />)
      const span = container.querySelector('span')
      expect(span).toHaveClass('text-green-600')
    })

    it('should show red for negative values when colored is true', () => {
      const { container } = render(<MoneyDisplay value={-50} colored />)
      const span = container.querySelector('span')
      expect(span).toHaveClass('text-red-600')
    })

    it('should show gray for zero when colored is true', () => {
      const { container } = render(<MoneyDisplay value={0} colored />)
      const span = container.querySelector('span')
      expect(span).toHaveClass('text-gray-600')
    })

    it('should work with MoneyDecimal and colored', () => {
      const money = MoneyDecimal.fromNumber(250)
      const { container } = render(<MoneyDisplay value={money} colored />)
      const span = container.querySelector('span')
      expect(span).toHaveClass('text-green-600')
    })
  })

  describe('Combined props', () => {
    it('should combine custom className with colored classes', () => {
      const { container } = render(
        <MoneyDisplay value={100} colored className="font-bold" />
      )
      const span = container.querySelector('span')
      expect(span).toHaveClass('text-green-600')
      expect(span).toHaveClass('font-bold')
    })

    it('should handle all props together', () => {
      render(
        <MoneyDisplay
          value={1234.567}
          showCurrency={false}
          decimals={3}
          className="test-class"
          colored
        />
      )
      expect(screen.getByText('1234.567')).toBeInTheDocument()
    })
  })

  describe('Edge cases', () => {
    it('should handle very large numbers', () => {
      render(<MoneyDisplay value={999999999.99} />)
      expect(screen.getByText(/\$\s?999[.,]999[.,]999[.,]99/)).toBeInTheDocument()
    })

    it('should handle very small numbers', () => {
      render(<MoneyDisplay value={0.01} />)
      expect(screen.getByText(/\$\s?0[.,]01/)).toBeInTheDocument()
    })

    it('should handle fractional cents', () => {
      render(<MoneyDisplay value={100.999} showCurrency={false} />)
      // Should round to 2 decimals when using formatCurrency
      const text = screen.getByText(/100\.99|101\.00/)
      expect(text).toBeInTheDocument()
    })
  })

  describe('Real-world scenarios', () => {
    it('should display balance in sidebar', () => {
      const balance = MoneyDecimal.fromNumber(2500.75)
      render(<MoneyDisplay value={balance} />)
      expect(screen.getByText(/\$\s?2[.,]500[.,]75/)).toBeInTheDocument()
    })

    it('should display transaction amount', () => {
      render(<MoneyDisplay value={150.5} colored />)
      const element = screen.getByText(/\$\s?150[.,]50/)
      expect(element).toBeInTheDocument()
      expect(element).toHaveClass('text-green-600')
    })

    it('should display debt as negative colored', () => {
      const debt = MoneyDecimal.fromNumber(-1200)
      render(<MoneyDisplay value={debt} colored />)
      const element = screen.getByText(/-\$\s?1[.,]200[.,]00/)
      expect(element).toBeInTheDocument()
      expect(element).toHaveClass('text-red-600')
    })

    it('should handle null balance gracefully', () => {
      render(<MoneyDisplay value={null} fallback="No disponible" />)
      expect(screen.getByText('No disponible')).toBeInTheDocument()
    })
  })

  describe('Backward compatibility', () => {
    it('should work with plain numbers (legacy data)', () => {
      const legacyValue: number = 500
      render(<MoneyDisplay value={legacyValue} />)
      expect(screen.getByText(/\$\s?500[.,]00/)).toBeInTheDocument()
    })

    it('should work with MoneyDecimal (new data)', () => {
      const newValue = MoneyDecimal.fromNumber(500)
      render(<MoneyDisplay value={newValue} />)
      expect(screen.getByText(/\$\s?500[.,]00/)).toBeInTheDocument()
    })
  })
})
