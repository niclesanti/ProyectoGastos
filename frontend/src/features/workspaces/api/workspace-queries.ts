import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '@/lib/api-client'
import { EspacioTrabajoResponse, EspacioTrabajoRequest } from '@/types/workspace'
import { espacioTrabajoService } from '@/services/espacio-trabajo.service'

// Hook para LISTAR espacios de trabajo (GET)
export const useWorkspaces = (userId: string | undefined) => {
  return useQuery({
    queryKey: ['workspaces', userId],
    queryFn: async () => {
      const { data } = await apiClient.get<EspacioTrabajoResponse[]>(
        `/espacios-trabajo`
      )
      return data
    },
    enabled: !!userId,
    staleTime: 1000 * 60 * 5,
  })
}

// Hook para REGISTRAR un nuevo espacio de trabajo (POST)
export const useCreateWorkspace = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (newWorkspace: EspacioTrabajoRequest) => {
      const { data } = await apiClient.post<void>(
        '/espacios-trabajo',
        newWorkspace
      )
      return data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workspaces'] })
    },
  })
}

// Hook para COMPARTIR espacio de trabajo (POST)
export const useShareWorkspace = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({
      email,
      idEspacioTrabajo,
    }: {
      email: string
      idEspacioTrabajo: string
    }) => {
      const { data } = await apiClient.post<void>(
        `/espacios-trabajo/${idEspacioTrabajo}/miembros`,
        { email }
      )
      return data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workspaces'] })
    },
  })
}

// Hook para LISTAR solicitudes pendientes (GET)
export const useSolicitudesPendientes = () => {
  return useQuery({
    queryKey: ['solicitudes-pendientes'],
    queryFn: () => espacioTrabajoService.listarSolicitudesPendientes(),
    staleTime: 1000 * 60 * 2,
  })
}

// Hook para RESPONDER a una solicitud (POST)
export const useResponderSolicitud = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({
      idSolicitud,
      aceptada,
    }: {
      idSolicitud: number
      aceptada: boolean
    }) => {
      await espacioTrabajoService.responderSolicitud(idSolicitud, aceptada)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['solicitudes-pendientes'] })
      queryClient.invalidateQueries({ queryKey: ['workspaces'] })
    },
  })
}
