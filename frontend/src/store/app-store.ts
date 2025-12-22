import { create } from 'zustand'
import type { Usuario, EspacioTrabajo } from '@/types'

interface AppState {
  user: Usuario | null
  currentWorkspace: EspacioTrabajo | null
  workspaces: EspacioTrabajo[]
  setUser: (user: Usuario | null) => void
  setCurrentWorkspace: (workspace: EspacioTrabajo | null) => void
  setWorkspaces: (workspaces: EspacioTrabajo[]) => void
}

export const useAppStore = create<AppState>((set) => ({
  user: null,
  currentWorkspace: null,
  workspaces: [],
  setUser: (user) => set({ user }),
  setCurrentWorkspace: (workspace) => set({ currentWorkspace: workspace }),
  setWorkspaces: (workspaces) => set({ workspaces }),
}))
