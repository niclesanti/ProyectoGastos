import { useCallback } from 'react'
import { useAppStore } from '@/store/app-store'

/**
 * Hook personalizado para manejar la invalidación del caché del dashboard
 * 
 * Uso:
 * - Después de crear/editar/eliminar transacciones
 * - Después de transferencias entre cuentas bancarias
 * - Después de crear/eliminar cuentas bancarias
 */
export function useDashboardCache() {
  const { 
    currentWorkspace,
    invalidateRecentTransactions, 
    invalidateBankAccounts,
    invalidateComprasPendientes,
    invalidateDashboardStats,
    invalidateDashboardCache,
    loadRecentTransactions,
    loadBankAccounts,
    loadComprasPendientes,
    loadDashboardStats,
  } = useAppStore()

  /**
   * Invalida y recarga las transacciones recientes
   */
  const refreshTransactions = useCallback(async () => {
    if (!currentWorkspace?.id) return

    invalidateRecentTransactions(currentWorkspace.id)
    return await loadRecentTransactions(currentWorkspace.id, true)
  }, [currentWorkspace?.id, invalidateRecentTransactions, loadRecentTransactions])

  /**
   * Invalida y recarga las cuentas bancarias
   */
  const refreshBankAccounts = useCallback(async () => {
    if (!currentWorkspace?.id) return

    invalidateBankAccounts(currentWorkspace.id)
    return await loadBankAccounts(currentWorkspace.id, true)
  }, [currentWorkspace?.id, invalidateBankAccounts, loadBankAccounts])

  /**
   * Invalida y recarga las compras pendientes
   */
  const refreshComprasPendientes = useCallback(async () => {
    if (!currentWorkspace?.id) return

    invalidateComprasPendientes(currentWorkspace.id)
    return await loadComprasPendientes(currentWorkspace.id, true)
  }, [currentWorkspace?.id, invalidateComprasPendientes, loadComprasPendientes])

  /**
   * Invalida y recarga las estadísticas del dashboard (KPIs + charts)
   */
  const refreshDashboardStats = useCallback(async () => {
    if (!currentWorkspace?.id) return

    invalidateDashboardStats(currentWorkspace.id)
    return await loadDashboardStats(currentWorkspace.id, true)
  }, [currentWorkspace?.id, invalidateDashboardStats, loadDashboardStats])

  /**
   * Invalida y recarga todo el dashboard (transacciones, cuentas, compras y stats)
   */
  const refreshDashboard = useCallback(async () => {
    if (!currentWorkspace?.id) return

    invalidateDashboardCache(currentWorkspace.id)
    
    // Cargar todos en paralelo
    await Promise.all([
      loadRecentTransactions(currentWorkspace.id, true),
      loadBankAccounts(currentWorkspace.id, true),
      loadComprasPendientes(currentWorkspace.id, true),
      loadDashboardStats(currentWorkspace.id, true),
    ])
  }, [
    currentWorkspace?.id,
    invalidateDashboardCache,
    loadRecentTransactions,
    loadBankAccounts,
    loadComprasPendientes,
    loadDashboardStats,
  ])

  /**
   * Solo invalida el caché sin recargar
   * Útil cuando navegas a otra página y quieres que se recargue en la próxima visita
   */
  const invalidateCache = useCallback(() => {
    if (!currentWorkspace?.id) return
    invalidateDashboardCache(currentWorkspace.id)
  }, [currentWorkspace?.id, invalidateDashboardCache])

  return {
    refreshTransactions,
    refreshBankAccounts,
    refreshComprasPendientes,
    refreshDashboardStats,
    refreshDashboard,
    invalidateCache,
  }
}
