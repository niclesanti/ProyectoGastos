import { useEffect, useState } from 'react'
import { useAppStore } from '@/store/app-store'
import type { DashboardStatsDTO } from '@/types'

export function useDashboardStats() {
  const currentWorkspace = useAppStore((state) => state.currentWorkspace)
  const loadDashboardStats = useAppStore((state) => state.loadDashboardStats)
  const invalidateDashboardStats = useAppStore((state) => state.invalidateDashboardStats)
  const dashboardStatsCache = useAppStore((state) => state.dashboardStats)
  
  const [stats, setStats] = useState<DashboardStatsDTO | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<Error | null>(null)

  useEffect(() => {
    if (!currentWorkspace?.id) {
      setStats(null)
      return
    }

    const fetchStats = async () => {
      setIsLoading(true)
      setError(null)
      try {
        const data = await loadDashboardStats(currentWorkspace.id)
        setStats(data)
      } catch (err) {
        console.error('Error loading dashboard stats:', err)
        setError(err instanceof Error ? err : new Error('Unknown error'))
        setStats(null)
      } finally {
        setIsLoading(false)
      }
    }

    fetchStats()
  }, [currentWorkspace?.id, loadDashboardStats, dashboardStatsCache])

  const refreshStats = async () => {
    if (!currentWorkspace?.id) return
    
    setIsLoading(true)
    setError(null)
    try {
      const data = await loadDashboardStats(currentWorkspace.id, true)
      setStats(data)
    } catch (err) {
      console.error('Error refreshing dashboard stats:', err)
      setError(err instanceof Error ? err : new Error('Unknown error'))
    } finally {
      setIsLoading(false)
    }
  }

  const invalidateStats = () => {
    if (currentWorkspace?.id) {
      invalidateDashboardStats(currentWorkspace.id)
    }
  }

  return {
    stats,
    isLoading,
    error,
    refreshStats,
    invalidateStats,
  }
}
