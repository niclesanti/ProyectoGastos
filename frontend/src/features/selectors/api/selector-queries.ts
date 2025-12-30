import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { motivoService } from '@/services/motivo.service'
import { contactoService } from '@/services/contacto.service'
import { cuentaBancariaService } from '@/services/cuenta-bancaria.service'
import { tarjetaService } from '@/services/tarjeta.service'
import { transaccionService } from '@/services/transaccion.service'
import { compraCreditoService } from '@/services/compra-credito.service'
import type { 
  MotivoDTORequest, 
  ContactoDTORequest, 
  CuentaBancariaDTORequest, 
  TarjetaDTORequest,
  TransaccionDTORequest,
  CompraCreditoDTORequest,
  TransaccionBusquedaDTO
} from '@/types'

// ============ TRANSACCIONES ============

export const useCreateTransaccion = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (transaccion: TransaccionDTORequest) => transaccionService.registrarTransaccion(transaccion),
    onSuccess: () => {
      // Invalidar todas las queries relacionadas con transacciones
      queryClient.invalidateQueries({ queryKey: ['transacciones'] })
    },
  })
}

export const useRemoverTransaccion = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => transaccionService.removerTransaccion(id),
    onSuccess: () => {
      // Invalidar todas las queries relacionadas con transacciones
      queryClient.invalidateQueries({ queryKey: ['transacciones'] })
    },
  })
}

export const useBuscarTransacciones = () => {
  return useMutation({
    mutationFn: (busqueda: TransaccionBusquedaDTO) => transaccionService.buscarTransacciones(busqueda),
  })
}

export const useMotivosTransaccion = (idEspacioTrabajo: number | undefined) => {
  return useQuery({
    queryKey: ['motivos-transaccion', idEspacioTrabajo],
    queryFn: () => transaccionService.listarMotivos(idEspacioTrabajo!),
    enabled: !!idEspacioTrabajo,
    staleTime: 5 * 60 * 1000, // 5 minutos
  })
}

export const useContactosTransaccion = (idEspacioTrabajo: number | undefined) => {
  return useQuery({
    queryKey: ['contactos-transaccion', idEspacioTrabajo],
    queryFn: () => transaccionService.listarContactos(idEspacioTrabajo!),
    enabled: !!idEspacioTrabajo,
    staleTime: 5 * 60 * 1000, // 5 minutos
  })
}

// ============ MOTIVOS ============

export const useMotivos = (idEspacioTrabajo: number | undefined) => {
  return useQuery({
    queryKey: ['motivos', idEspacioTrabajo],
    queryFn: () => motivoService.listarMotivos(idEspacioTrabajo!),
    enabled: !!idEspacioTrabajo,
    staleTime: 5 * 60 * 1000, // 5 minutos
  })
}

export const useCreateMotivo = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (motivo: MotivoDTORequest) => motivoService.registrarMotivo(motivo),
    onSuccess: (_, variables) => {
      // Invalidar la caché de motivos para ese espacio de trabajo
      queryClient.invalidateQueries({ queryKey: ['motivos', variables.idEspacioTrabajo] })
    },
  })
}

// ============ CONTACTOS ============

export const useContactos = (idEspacioTrabajo: number | undefined) => {
  return useQuery({
    queryKey: ['contactos', idEspacioTrabajo],
    queryFn: () => contactoService.listarContactos(idEspacioTrabajo!),
    enabled: !!idEspacioTrabajo,
    staleTime: 5 * 60 * 1000, // 5 minutos
  })
}

export const useCreateContacto = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (contacto: ContactoDTORequest) => contactoService.registrarContacto(contacto),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['contactos', variables.idEspacioTrabajo] })
    },
  })
}

// ============ CUENTAS BANCARIAS ============

export const useCuentasBancarias = (idEspacioTrabajo: number | undefined) => {
  return useQuery({
    queryKey: ['cuentas', idEspacioTrabajo],
    queryFn: () => cuentaBancariaService.listarCuentas(idEspacioTrabajo!),
    enabled: !!idEspacioTrabajo,
    staleTime: 5 * 60 * 1000, // 5 minutos
  })
}

export const useCreateCuentaBancaria = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (cuenta: CuentaBancariaDTORequest) => cuentaBancariaService.crearCuenta(cuenta),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['cuentas', variables.idEspacioTrabajo] })
    },
  })
}

export const useTransferenciaCuentas = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ idCuentaOrigen, idCuentaDestino, monto }: { 
      idCuentaOrigen: number
      idCuentaDestino: number
      monto: number 
    }) => cuentaBancariaService.transferirEntreCuentas(idCuentaOrigen, idCuentaDestino, monto),
    onSuccess: () => {
      // Invalidar todas las cuentas para refrescar saldos
      queryClient.invalidateQueries({ queryKey: ['cuentas'] })
      // También invalidar transacciones si se listan transferencias
      queryClient.invalidateQueries({ queryKey: ['transacciones'] })
    },
  })
}

// ============ TARJETAS ============

export const useTarjetas = (idEspacioTrabajo: number | undefined) => {
  return useQuery({
    queryKey: ['tarjetas', idEspacioTrabajo],
    queryFn: () => tarjetaService.listarTarjetas(idEspacioTrabajo!),
    enabled: !!idEspacioTrabajo,
    staleTime: 5 * 60 * 1000, // 5 minutos
  })
}

export const useCreateTarjeta = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (tarjeta: TarjetaDTORequest) => tarjetaService.registrarTarjeta(tarjeta),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['tarjetas', variables.espacioTrabajoId] })
    },
  })
}

export const useCuotasTarjeta = (idTarjeta: number | undefined) => {
  return useQuery({
    queryKey: ['cuotas', idTarjeta],
    queryFn: () => tarjetaService.listarCuotasPorTarjeta(idTarjeta!),
    enabled: !!idTarjeta,
    staleTime: 2 * 60 * 1000, // 2 minutos (más fresco porque cambia con pagos)
  })
}

// ============ COMPRAS CON CRÉDITO ============

export const useCreateCompraCredito = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (compra: CompraCreditoDTORequest) => compraCreditoService.registrarCompraCredito(compra),
    onSuccess: () => {
      // Invalidar todas las queries relacionadas con compras y cuotas
      queryClient.invalidateQueries({ queryKey: ['compras'] })
      queryClient.invalidateQueries({ queryKey: ['cuotas'] })
    },
  })
}
