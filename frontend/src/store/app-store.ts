import { create } from 'zustand'
import type { Usuario, EspacioTrabajo, CuentaBancaria, TransaccionDTOResponse, CompraCreditoDTOResponse, DashboardStatsDTO, NotificacionDTOResponse } from '@/types'
import { transaccionService } from '@/services/transaccion.service'
import { cuentaBancariaService } from '@/services/cuenta-bancaria.service'
import { compraCreditoService } from '@/services/compra-credito.service'
import { notificacionService } from '@/services/notificacion.service'

interface DashboardCache {
  data: TransaccionDTOResponse[]
  timestamp: number
}

interface CuentasCache {
  data: CuentaBancaria[]
  timestamp: number
}

interface ComprasPendientesCache {
  data: CompraCreditoDTOResponse[]
  timestamp: number
}

interface DashboardStatsCache {
  data: DashboardStatsDTO
  timestamp: number
}

interface NotificacionesCache {
  data: NotificacionDTOResponse[]
  timestamp: number
  unreadCount: number
}

interface AppState {
  user: Usuario | null
  currentWorkspace: EspacioTrabajo | null
  workspaces: EspacioTrabajo[]
  
  // Dashboard data with cache
  recentTransactions: Map<string, DashboardCache>
  bankAccounts: Map<string, CuentasCache>
  comprasPendientes: Map<string, ComprasPendientesCache>
  dashboardStats: Map<string, DashboardStatsCache>
  
  // Notificaciones
  notificaciones: NotificacionDTOResponse[]
  unreadCount: number
  notificacionesCache: NotificacionesCache | null
  
  // Actions
  setUser: (user: Usuario | null) => void
  setCurrentWorkspace: (workspace: EspacioTrabajo | null) => void
  setWorkspaces: (workspaces: EspacioTrabajo[]) => void
  
  // Dashboard actions with cache
  loadRecentTransactions: (idEspacio: string, forceRefresh?: boolean) => Promise<TransaccionDTOResponse[]>
  loadBankAccounts: (idEspacio: string, forceRefresh?: boolean) => Promise<CuentaBancaria[]>
  loadComprasPendientes: (idEspacio: string, forceRefresh?: boolean) => Promise<CompraCreditoDTOResponse[]>
  loadDashboardStats: (idEspacio: string, forceRefresh?: boolean) => Promise<DashboardStatsDTO>
  invalidateRecentTransactions: (idEspacio: string) => void
  invalidateBankAccounts: (idEspacio: string) => void
  invalidateComprasPendientes: (idEspacio: string) => void
  invalidateDashboardStats: (idEspacio: string) => void
  invalidateDashboardCache: (idEspacio: string) => void
  
  // Notification actions
  loadNotificaciones: (forceRefresh?: boolean) => Promise<void>
  loadUnreadCount: () => Promise<void>
  marcarComoLeida: (id: number) => Promise<void>
  marcarTodasComoLeidas: () => Promise<void>
  eliminarNotificacion: (id: number) => Promise<void>
  agregarNotificacion: (notificacion: NotificacionDTOResponse) => void
  invalidateNotificaciones: () => void
}

const CACHE_DURATION = 5 * 60 * 1000 // 5 minutos

const isCacheValid = (timestamp: number): boolean => {
  return Date.now() - timestamp < CACHE_DURATION
}

export const useAppStore = create<AppState>((set, get) => ({
  user: null,
  currentWorkspace: null,
  workspaces: [],
  recentTransactions: new Map(),
  bankAccounts: new Map(),
  comprasPendientes: new Map(),
  dashboardStats: new Map(),
  notificaciones: [],
  unreadCount: 0,
  notificacionesCache: null,
  
  setUser: (user) => set({ user }),
  setCurrentWorkspace: (workspace) => set({ currentWorkspace: workspace }),
  setWorkspaces: (workspaces) => set({ workspaces }),
  
  loadRecentTransactions: async (idEspacio: string, forceRefresh = false) => {
    const cache = get().recentTransactions.get(idEspacio)
    
    // Si existe caché válido y no se fuerza el refresh, retornar del caché
    if (cache && isCacheValid(cache.timestamp) && !forceRefresh) {
      return cache.data
    }
    
    // Llamar a la API
    const data = await transaccionService.buscarTransaccionesRecientes(idEspacio)
    
    // Actualizar el caché
    set((state) => ({
      recentTransactions: new Map(state.recentTransactions).set(idEspacio, {
        data,
        timestamp: Date.now(),
      }),
    }))
    
    return data
  },
  
  loadBankAccounts: async (idEspacio: string, forceRefresh = false) => {
    const cache = get().bankAccounts.get(idEspacio)
    
    // Si existe caché válido y no se fuerza el refresh, retornar del caché
    if (cache && isCacheValid(cache.timestamp) && !forceRefresh) {
      return cache.data
    }
    
    // Llamar a la API
    const data = await cuentaBancariaService.listarCuentas(idEspacio)
    
    // Actualizar el caché
    set((state) => ({
      bankAccounts: new Map(state.bankAccounts).set(idEspacio, {
        data,
        timestamp: Date.now(),
      }),
    }))
    
    return data
  },
  
  loadComprasPendientes: async (idEspacio: string, forceRefresh = false) => {
    const cache = get().comprasPendientes.get(idEspacio)
    
    // Si existe caché válido y no se fuerza el refresh, retornar del caché
    if (cache && isCacheValid(cache.timestamp) && !forceRefresh) {
      return cache.data
    }
    
    // Llamar a la API con un tamaño de página grande para obtener todas las compras
    const response = await compraCreditoService.listarComprasPendientes(idEspacio, 0, 100)
    const data = response.content
    
    // Actualizar el caché
    set((state) => ({
      comprasPendientes: new Map(state.comprasPendientes).set(idEspacio, {
        data,
        timestamp: Date.now(),
      }),
    }))
    
    return data
  },
  
  invalidateRecentTransactions: (idEspacio: string) => {
    set((state) => {
      const newCache = new Map(state.recentTransactions)
      newCache.delete(idEspacio)
      return { recentTransactions: newCache }
    })
  },
  
  invalidateBankAccounts: (idEspacio: string) => {
    set((state) => {
      const newCache = new Map(state.bankAccounts)
      newCache.delete(idEspacio)
      return { bankAccounts: newCache }
    })
  },
  
  invalidateComprasPendientes: (idEspacio: string) => {
    set((state) => {
      const newCache = new Map(state.comprasPendientes)
      newCache.delete(idEspacio)
      return { comprasPendientes: newCache }
    })
  },
  
  loadDashboardStats: async (idEspacio: string, forceRefresh = false) => {
    const cache = get().dashboardStats.get(idEspacio)
    
    // Si existe caché válido y no se fuerza el refresh, retornar del caché
    if (cache && isCacheValid(cache.timestamp) && !forceRefresh) {
      return cache.data
    }
    
    // Llamar a la API
    const data = await transaccionService.obtenerDashboardStats(idEspacio)
    
    // Actualizar el caché
    set((state) => ({
      dashboardStats: new Map(state.dashboardStats).set(idEspacio, {
        data,
        timestamp: Date.now(),
      }),
    }))
    
    return data
  },
  
  invalidateDashboardStats: (idEspacio: string) => {
    set((state) => {
      const newCache = new Map(state.dashboardStats)
      newCache.delete(idEspacio)
      return { dashboardStats: newCache }
    })
  },
  
  invalidateDashboardCache: (idEspacio: string) => {
    get().invalidateRecentTransactions(idEspacio)
    get().invalidateBankAccounts(idEspacio)
    get().invalidateComprasPendientes(idEspacio)
    get().invalidateDashboardStats(idEspacio)
  },
  
  // Notification actions
  loadNotificaciones: async (forceRefresh = false) => {
    const cache = get().notificacionesCache
    
    // Si existe caché válido y no se fuerza el refresh, no hacer nada
    if (cache && isCacheValid(cache.timestamp) && !forceRefresh) {
      return
    }
    
    try {
      const data = await notificacionService.obtenerNotificaciones()
      const unreadCount = data.filter(n => !n.leida).length
      
      set({
        notificaciones: data,
        unreadCount,
        notificacionesCache: {
          data,
          unreadCount,
          timestamp: Date.now(),
        },
      })
    } catch (error) {
      console.error('Error al cargar notificaciones:', error)
    }
  },
  
  loadUnreadCount: async () => {
    try {
      const count = await notificacionService.contarNoLeidas()
      set({ unreadCount: count })
    } catch (error) {
      console.error('Error al cargar contador de no leídas:', error)
    }
  },
  
  marcarComoLeida: async (id: number) => {
    try {
      await notificacionService.marcarComoLeida(id)
      
      // Actualizar estado local
      set((state) => {
        const notificaciones = state.notificaciones.map(n =>
          n.id === id ? { ...n, leida: true, fechaLeida: new Date().toISOString() } : n
        )
        const unreadCount = notificaciones.filter(n => !n.leida).length
        
        return {
          notificaciones,
          unreadCount,
          notificacionesCache: state.notificacionesCache ? {
            ...state.notificacionesCache,
            data: notificaciones,
            unreadCount,
          } : null,
        }
      })
    } catch (error) {
      console.error('Error al marcar notificación como leída:', error)
      throw error
    }
  },
  
  marcarTodasComoLeidas: async () => {
    try {
      await notificacionService.marcarTodasComoLeidas()
      
      // Actualizar estado local
      set((state) => {
        const now = new Date().toISOString()
        const notificaciones = state.notificaciones.map(n => ({
          ...n,
          leida: true,
          fechaLeida: n.leida ? n.fechaLeida : now,
        }))
        
        return {
          notificaciones,
          unreadCount: 0,
          notificacionesCache: state.notificacionesCache ? {
            ...state.notificacionesCache,
            data: notificaciones,
            unreadCount: 0,
          } : null,
        }
      })
    } catch (error) {
      console.error('Error al marcar todas como leídas:', error)
      throw error
    }
  },
  
  eliminarNotificacion: async (id: number) => {
    try {
      await notificacionService.eliminarNotificacion(id)
      
      // Actualizar estado local
      set((state) => {
        const notificaciones = state.notificaciones.filter(n => n.id !== id)
        const unreadCount = notificaciones.filter(n => !n.leida).length
        
        return {
          notificaciones,
          unreadCount,
          notificacionesCache: state.notificacionesCache ? {
            ...state.notificacionesCache,
            data: notificaciones,
            unreadCount,
          } : null,
        }
      })
    } catch (error) {
      console.error('Error al eliminar notificación:', error)
      throw error
    }
  },
  
  agregarNotificacion: (notificacion: NotificacionDTOResponse) => {
    set((state) => {
      // Agregar al inicio del array
      const notificaciones = [notificacion, ...state.notificaciones]
      const unreadCount = notificaciones.filter(n => !n.leida).length
      
      return {
        notificaciones,
        unreadCount,
        notificacionesCache: state.notificacionesCache ? {
          data: notificaciones,
          unreadCount,
          timestamp: Date.now(),
        } : null,
      }
    })
  },
  
  invalidateNotificaciones: () => {
    set({ notificacionesCache: null })
  },
}))
