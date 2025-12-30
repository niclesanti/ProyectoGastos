import { create } from 'zustand'
import type { Usuario, EspacioTrabajo, CuentaBancaria, Transaccion, CompraCreditoDTOResponse } from '@/types'
import { transaccionService } from '@/services/transaccion.service'
import { cuentaBancariaService } from '@/services/cuenta-bancaria.service'
import { compraCreditoService } from '@/services/compra-credito.service'

interface DashboardCache {
  data: Transaccion[]
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

interface AppState {
  user: Usuario | null
  currentWorkspace: EspacioTrabajo | null
  workspaces: EspacioTrabajo[]
  
  // Dashboard data with cache
  recentTransactions: Map<number, DashboardCache>
  bankAccounts: Map<number, CuentasCache>
  comprasPendientes: Map<number, ComprasPendientesCache>
  
  // Actions
  setUser: (user: Usuario | null) => void
  setCurrentWorkspace: (workspace: EspacioTrabajo | null) => void
  setWorkspaces: (workspaces: EspacioTrabajo[]) => void
  
  // Dashboard actions with cache
  loadRecentTransactions: (idEspacio: number, forceRefresh?: boolean) => Promise<Transaccion[]>
  loadBankAccounts: (idEspacio: number, forceRefresh?: boolean) => Promise<CuentaBancaria[]>
  loadComprasPendientes: (idEspacio: number, forceRefresh?: boolean) => Promise<CompraCreditoDTOResponse[]>
  invalidateRecentTransactions: (idEspacio: number) => void
  invalidateBankAccounts: (idEspacio: number) => void
  invalidateComprasPendientes: (idEspacio: number) => void
  invalidateDashboardCache: (idEspacio: number) => void
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
  
  setUser: (user) => set({ user }),
  setCurrentWorkspace: (workspace) => set({ currentWorkspace: workspace }),
  setWorkspaces: (workspaces) => set({ workspaces }),
  
  loadRecentTransactions: async (idEspacio: number, forceRefresh = false) => {
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
  
  loadBankAccounts: async (idEspacio: number, forceRefresh = false) => {
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
  
  loadComprasPendientes: async (idEspacio: number, forceRefresh = false) => {
    const cache = get().comprasPendientes.get(idEspacio)
    
    // Si existe caché válido y no se fuerza el refresh, retornar del caché
    if (cache && isCacheValid(cache.timestamp) && !forceRefresh) {
      return cache.data
    }
    
    // Llamar a la API
    const data = await compraCreditoService.listarComprasPendientes(idEspacio)
    
    // Actualizar el caché
    set((state) => ({
      comprasPendientes: new Map(state.comprasPendientes).set(idEspacio, {
        data,
        timestamp: Date.now(),
      }),
    }))
    
    return data
  },
  
  invalidateRecentTransactions: (idEspacio: number) => {
    set((state) => {
      const newCache = new Map(state.recentTransactions)
      newCache.delete(idEspacio)
      return { recentTransactions: newCache }
    })
  },
  
  invalidateBankAccounts: (idEspacio: number) => {
    set((state) => {
      const newCache = new Map(state.bankAccounts)
      newCache.delete(idEspacio)
      return { bankAccounts: newCache }
    })
  },
  
  invalidateComprasPendientes: (idEspacio: number) => {
    set((state) => {
      const newCache = new Map(state.comprasPendientes)
      newCache.delete(idEspacio)
      return { comprasPendientes: newCache }
    })
  },
  
  invalidateDashboardCache: (idEspacio: number) => {
    get().invalidateRecentTransactions(idEspacio)
    get().invalidateBankAccounts(idEspacio)
    get().invalidateComprasPendientes(idEspacio)
  },
}))
