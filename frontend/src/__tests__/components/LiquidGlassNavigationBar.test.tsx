import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { LiquidGlassNavigationBar } from '@/components/LiquidGlassNavigationBar'
import { useAppStore } from '@/store/app-store'

const mockWorkspace = {
  id: 'ws-1',
  nombre: 'Espacio de prueba',
} as any

const actionCases = [
  { label: 'Registrar gastos e ingresos', testid: 'txn-modal' },
  { label: 'Movimiento entre cuentas', testid: 'transfer-modal' },
  { label: 'Compras con crédito', testid: 'credit-modal' },
  { label: 'Resumen mensual de tarjetas', testid: 'payment-modal' },
]

vi.mock('@/components/TransactionModal', () => ({
  TransactionModal: ({
    open,
    onOpenChange,
  }: {
    open: boolean
    onOpenChange: (open: boolean) => void
  }) =>
    open ? (
      <div data-testid="txn-modal">
        <button onClick={() => onOpenChange(false)}>Cerrar txn</button>
      </div>
    ) : null,
}))

vi.mock('@/components/AccountTransferModal', () => ({
  AccountTransferModal: ({
    open,
    onOpenChange,
  }: {
    open: boolean
    onOpenChange: (open: boolean) => void
  }) =>
    open ? (
      <div data-testid="transfer-modal">
        <button onClick={() => onOpenChange(false)}>Cerrar transfer</button>
      </div>
    ) : null,
}))

vi.mock('@/components/CreditPurchaseModal', () => ({
  CreditPurchaseModal: ({
    open,
    onOpenChange,
  }: {
    open: boolean
    onOpenChange: (open: boolean) => void
  }) =>
    open ? (
      <div data-testid="credit-modal">
        <button onClick={() => onOpenChange(false)}>Cerrar credit</button>
      </div>
    ) : null,
}))

vi.mock('@/components/CardPaymentModal', () => ({
  CardPaymentModal: ({
    open,
    onOpenChange,
  }: {
    open: boolean
    onOpenChange: (open: boolean) => void
  }) =>
    open ? (
      <div data-testid="payment-modal">
        <button onClick={() => onOpenChange(false)}>Cerrar payment</button>
      </div>
    ) : null,
}))

describe('LiquidGlassNavigationBar', () => {
  beforeEach(() => {
    useAppStore.setState({ currentWorkspace: null })
  })

  it('no renderiza la barra sin un workspace activo', () => {
    render(<LiquidGlassNavigationBar />)
    expect(
      screen.queryByRole('navigation', { name: 'Acciones rápidas' })
    ).not.toBeInTheDocument()
  })

  it('renderiza la navegación con los 4 botones de acción', () => {
    useAppStore.setState({ currentWorkspace: mockWorkspace })
    render(<LiquidGlassNavigationBar />)

    expect(
      screen.getByRole('navigation', { name: 'Acciones rápidas' })
    ).toBeInTheDocument()

    for (const action of actionCases) {
      expect(screen.getByRole('button', { name: action.label })).toBeInTheDocument()
    }
  })

  it.each(actionCases)(
    'abre solo el modal "$testid" al hacer click en "$label"',
    ({ label, testid }) => {
      useAppStore.setState({ currentWorkspace: mockWorkspace })
      render(<LiquidGlassNavigationBar />)

      fireEvent.click(screen.getByRole('button', { name: label }))

      expect(screen.getByTestId(testid)).toBeInTheDocument()

      const otherTestids = actionCases
        .map((action) => action.testid)
        .filter((id) => id !== testid)
      for (const otherTestid of otherTestids) {
        expect(screen.queryByTestId(otherTestid)).not.toBeInTheDocument()
      }
    }
  )

  it('cierra el modal activo vía onOpenChange(false) y vuelve al estado neutro', () => {
    useAppStore.setState({ currentWorkspace: mockWorkspace })
    render(<LiquidGlassNavigationBar />)

    fireEvent.click(
      screen.getByRole('button', { name: 'Registrar gastos e ingresos' })
    )
    expect(screen.getByTestId('txn-modal')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Cerrar txn' }))
    expect(screen.queryByTestId('txn-modal')).not.toBeInTheDocument()

    for (const action of actionCases) {
      expect(screen.queryByTestId(action.testid)).not.toBeInTheDocument()
    }

    fireEvent.click(screen.getByRole('button', { name: 'Compras con crédito' }))
    expect(screen.getByTestId('credit-modal')).toBeInTheDocument()
    expect(screen.queryByTestId('txn-modal')).not.toBeInTheDocument()
  })
})