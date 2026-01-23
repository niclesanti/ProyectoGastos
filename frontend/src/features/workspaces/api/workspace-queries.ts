import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '@/lib/api-client'
import { EspacioTrabajoResponse, EspacioTrabajoRequest } from '@/types/workspace'

// Hook para LISTAR espacios de trabajo (GET)
export const useWorkspaces = (userId: string | undefined) => {
  return useQuery({
    queryKey: ['workspaces', userId], // El caché se identifica por este ID
    queryFn: async () => {
      const { data } = await apiClient.get<EspacioTrabajoResponse[]>(
        `/espaciotrabajo/listar`
      )
      return data
    },
    enabled: !!userId, // Solo se ejecuta si hay un usuario autenticado
    staleTime: 1000 * 60 * 5, // Los datos se consideran "frescos" por 5 minutos
  })
}

// Hook para REGISTRAR un nuevo espacio de trabajo (POST)
export const useCreateWorkspace = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (newWorkspace: EspacioTrabajoRequest) => {
      const { data } = await apiClient.post<void>(
        '/espaciotrabajo/registrar',
        newWorkspace
      )
      return data
    },
    onSuccess: () => {
      // Invalida el caché para que la sidebar se actualice automáticamente
      queryClient.invalidateQueries({ queryKey: ['workspaces'] })
    },
  })
}

// Hook para COMPARTIR espacio de trabajo (PUT)
export const useShareWorkspace = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({
      email,
      idEspacioTrabajo,
    }: {
      email: string
      idEspacioTrabajo: string  // UUID
    }) => {
      const { data } = await apiClient.put<void>(
        `/espaciotrabajo/compartir/${email}/${idEspacioTrabajo}`
      )
      return data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workspaces'] })
    },
  })
}
