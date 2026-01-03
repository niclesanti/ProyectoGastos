package com.campito.backend.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campito.backend.dao.CompraCreditoRepository;
import com.campito.backend.dao.ContactoTransferenciaRepository;
import com.campito.backend.dao.CuotaCreditoRepository;
import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dao.MotivoTransaccionRepository;
import com.campito.backend.dao.ResumenRepository;
import com.campito.backend.dao.TarjetaRepository;
import com.campito.backend.dao.TransaccionRepository;
import com.campito.backend.dto.CompraCreditoDTORequest;
import com.campito.backend.dto.CompraCreditoDTOResponse;
import com.campito.backend.dto.CuotaCreditoDTOResponse;
import com.campito.backend.dto.ResumenDTOResponse;
import com.campito.backend.dto.TarjetaDTORequest;
import com.campito.backend.dto.TarjetaDTOResponse;
import com.campito.backend.dto.TransaccionDTORequest;
import com.campito.backend.dto.TransaccionDTOResponse;
import com.campito.backend.mapper.CompraCreditoMapper;
import com.campito.backend.mapper.CuotaCreditoMapper;
import com.campito.backend.mapper.ResumenMapper;
import com.campito.backend.mapper.TarjetaMapper;
import com.campito.backend.model.CompraCredito;

import lombok.RequiredArgsConstructor;
import com.campito.backend.model.ContactoTransferencia;
import com.campito.backend.model.CuotaCredito;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.MotivoTransaccion;
import com.campito.backend.model.Resumen;
import com.campito.backend.model.Tarjeta;
import com.campito.backend.model.Transaccion;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implementación del servicio para gestión de compras a crédito y tarjetas.
 * 
 * Proporciona métodos para registrar compras a crédito, gestionar tarjetas,
 * y manejar cuotas asociadas.
 */
@Service
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
public class CompraCreditoServiceImpl implements CompraCreditoService {

    private static final Logger logger = LoggerFactory.getLogger(CompraCreditoServiceImpl.class);

    private final CompraCreditoRepository compraCreditoRepository;
    private final EspacioTrabajoRepository espacioRepository;
    private final MotivoTransaccionRepository motivoRepository;
    private final ContactoTransferenciaRepository contactoRepository;
    private final CuotaCreditoRepository cuotaCreditoRepository;
    private final TarjetaRepository tarjetaRepository;
    private final TransaccionRepository transaccionRepository;
    private final ResumenRepository resumenRepository;

    private final CompraCreditoMapper compraCreditoMapper;
    private final TarjetaMapper tarjetaMapper;
    private final CuotaCreditoMapper cuotaCreditoMapper;
    private final ResumenMapper resumenMapper;

    private final TransaccionService transaccionService;

    /**
     * Registra una compra a crédito en el sistema.
     * 
     * @param compraCreditoDTO Datos de la compra a crédito a registrar.
     * @return Respuesta con los detalles de la compra registrada.
     */
    @Override
    @Transactional
    public CompraCreditoDTOResponse registrarCompraCredito(CompraCreditoDTORequest compraCreditoDTO) {
        if (compraCreditoDTO == null) {
            logger.warn("Intento de registrar una compraCreditoDTO nula.");
            throw new IllegalArgumentException("La compra credito no puede ser nula");
        }
        logger.info("Iniciando registro de compraCredito por monto {} con cantidad de cuotas {} en espacio ID {}", compraCreditoDTO.montoTotal(), compraCreditoDTO.cantidadCuotas(), compraCreditoDTO.espacioTrabajoId());

        try {
            if (compraCreditoDTO.espacioTrabajoId() == null) {
                logger.warn("ID de espacio de trabajo nulo al registrar compra credito.");
                throw new IllegalArgumentException("El espacio de trabajo de la compra credito no puede ser nulo");
            }
            if (compraCreditoDTO.motivoId() == null) {
                logger.warn("ID de motivo nulo al registrar compra credito.");
                throw new IllegalArgumentException("El motivo de la compra credito no puede ser nulo");
            }
            if (compraCreditoDTO.tarjetaId() == null) {
                logger.warn("ID de tarjeta nulo al registrar compra credito.");
                throw new IllegalArgumentException("La tarjeta de la compra credito no puede ser nulo");
            }

            EspacioTrabajo espacio = espacioRepository.findById(compraCreditoDTO.espacioTrabajoId()).orElseThrow(() -> {
                String msg = "Espacio de trabajo con ID " + compraCreditoDTO.espacioTrabajoId() + " no encontrado";
                logger.warn(msg);
                return new EntityNotFoundException(msg);
            });
            MotivoTransaccion motivo = motivoRepository.findById(compraCreditoDTO.motivoId()).orElseThrow(() -> {
                String msg = "Motivo de transaccion con ID " + compraCreditoDTO.motivoId() + " no encontrado";
                logger.warn(msg);
                return new EntityNotFoundException(msg);
            });
            Tarjeta tarjeta = tarjetaRepository.findById(compraCreditoDTO.tarjetaId()).orElseThrow(() -> {
                String msg = "Tarjeta con ID " + compraCreditoDTO.tarjetaId() + " no encontrado";
                logger.warn(msg);
                return new EntityNotFoundException(msg);
            });

            CompraCredito compraCredito = compraCreditoMapper.toEntity(compraCreditoDTO);

            if (compraCreditoDTO.comercioId() != null) {
                ContactoTransferencia comercio = contactoRepository.findById(compraCreditoDTO.comercioId()).orElseThrow(() -> {
                    String msg = "Comercio con ID " + compraCreditoDTO.comercioId() + " no encontrado";
                    logger.warn(msg);
                    return new EntityNotFoundException(msg);
                });
                compraCredito.setComercio(comercio);
            }

            ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");
            ZonedDateTime nowInBuenosAires = ZonedDateTime.now(buenosAiresZone);
            compraCredito.setFechaCreacion(nowInBuenosAires.toLocalDateTime());

            compraCredito.setEspacioTrabajo(espacio);
            compraCredito.setMotivo(motivo);
            compraCredito.setTarjeta(tarjeta);

            CompraCredito compraCreditoGuardada = compraCreditoRepository.save(compraCredito);
            crearCuotas(compraCreditoGuardada);
            logger.info("Compra credito ID {} registrada exitosamente en espacio ID {}.", compraCreditoGuardada.getId(), espacio.getId());
            
            return compraCreditoMapper.toResponse(compraCreditoGuardada);

        } catch (Exception e) {
            logger.error("Error inesperado al registrar una compra credito en espacio ID {}: {}", compraCreditoDTO.espacioTrabajoId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Metodo privado que crea las cuotas asociadas a una compra a crédito.
     * @param compraCredito
     */
    private void crearCuotas(CompraCredito compraCredito) {
        Tarjeta tarjeta = compraCredito.getTarjeta();

        if (compraCredito.getCantidadCuotas() <= 0) {
            logger.warn("Intento de crear cuotas para una compra con 0 o menos cuotas. Compra ID: {}", compraCredito.getId());
            return;
        }

        Float montoCuota = compraCredito.getMontoTotal() / compraCredito.getCantidadCuotas();
        
        LocalDate fechaCompra = compraCredito.getFechaCompra();
        Integer diaCierre = tarjeta.getDiaCierre();
        Integer diaVencimiento = tarjeta.getDiaVencimientoPago();

        LocalDate primerVencimiento;

        // Si la compra se realiza después del día de cierre de la tarjeta en el mes de la compra
        if (fechaCompra.getDayOfMonth() > diaCierre) {
            // La cuota entrará en el resumen del mes siguiente, y vencerá el mes subsiguiente.
            // Ejemplo: Cierre día 25. Compra día 28 de Julio. Entra en resumen que cierra 25 de Agosto. Vence en Septiembre.
            primerVencimiento = fechaCompra.plusMonths(2).withDayOfMonth(diaVencimiento);
        } else {
            // La cuota entrará en el resumen de este mes, y vencerá el mes siguiente.
            // Ejemplo: Cierre día 25. Compra día 20 de Julio. Entra en resumen que cierra 25 de Julio. Vence en Agosto.
            primerVencimiento = fechaCompra.plusMonths(1).withDayOfMonth(diaVencimiento);
        }

        for (int i = 0; i < compraCredito.getCantidadCuotas(); i++) {
            CuotaCredito cuota = CuotaCredito.builder()
                .compraCredito(compraCredito)
                .numeroCuota(i + 1)
                .pagada(false)
                .montoCuota(montoCuota)
                .fechaVencimiento(primerVencimiento.plusMonths(i))
                .build();
            cuotaCreditoRepository.save(cuota);
        }
        logger.info("Se crearon {} cuotas para la compra a crédito ID {}", compraCredito.getCantidadCuotas(), compraCredito.getId());
    }

    /**
     * Metodo que registra una nueva tarjeta en un espacio de trabajo.
     * @param tarjetaDTO Datos de la tarjeta a registrar.
     * @return Respuesta con los detalles de la tarjeta registrada
    */
    @Override
    @Transactional
    public TarjetaDTOResponse registrarTarjeta(TarjetaDTORequest tarjetaDTO) {
        if(tarjetaDTO == null) {
            logger.warn("Intento de registrar una tarjeta nula.");
            throw new IllegalArgumentException("La tarjeta no puede ser nula");
        }
        logger.info("Iniciando registro de tarjeta {} en espacio ID {}", tarjetaDTO.numeroTarjeta(), tarjetaDTO.espacioTrabajoId());
        try {
            if(tarjetaDTO.espacioTrabajoId() == null) {
                logger.warn("ID de espacio de trabajo nulo al registrar tarjeta.");
                throw new IllegalArgumentException("El espacio de trabajo de la tarjeta no puede ser nulo");
            }

            EspacioTrabajo espacio = espacioRepository.findById(tarjetaDTO.espacioTrabajoId()).orElseThrow(() -> {
                String msg = "Espacio de trabajo con ID " + tarjetaDTO.espacioTrabajoId() + " no encontrado";
                logger.warn(msg);
                return new EntityNotFoundException(msg);
            });

            Tarjeta tarjeta = tarjetaMapper.toEntity(tarjetaDTO);
            tarjeta.setEspacioTrabajo(espacio);

            Tarjeta tarjetaGuardada = tarjetaRepository.save(tarjeta);
            logger.info("Tarjeta ID {} registrada exitosamente en espacio ID {}.", tarjetaGuardada.getId(), espacio.getId());
            
            return tarjetaMapper.toResponse(tarjetaGuardada);

        } catch (Exception e) {
            logger.error("Error inesperado al registrar una tarjeta en espacio ID {}: {}", tarjetaDTO.espacioTrabajoId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Remueve una compra a crédito del sistema.
     * Solo se permite eliminar si ninguna cuota ha sido pagada.
     * 
     * @param id ID de la compra a crédito a eliminar
     * @throws EntityNotFoundException si la compra no existe
     * @throws IllegalStateException si alguna cuota ya fue pagada
     */
    @Override
    @Transactional
    public void removerCompraCredito(Long id) {
        if (id == null) {
            logger.warn("Intento de remover una compra crédito con ID nulo.");
            throw new IllegalArgumentException("El ID de la compra crédito no puede ser nulo");
        }
        logger.info("Iniciando remoción de compra crédito ID {}", id);

        try {
            if (!compraCreditoRepository.existsById(id)) {
                String msg = "Compra crédito con ID " + id + " no encontrada";
                logger.warn(msg);
                throw new EntityNotFoundException(msg);
            }

            // Verificar si alguna cuota ya fue pagada
            List<CuotaCredito> cuotasPagadas = cuotaCreditoRepository.findByCompraCredito_IdAndPagada(id, true);
            if (!cuotasPagadas.isEmpty()) {
                String msg = "No se puede eliminar la compra crédito ID " + id + " porque tiene cuotas pagadas";
                logger.warn(msg);
                throw new IllegalStateException(msg);
            }

            // Eliminar todas las cuotas asociadas
            cuotaCreditoRepository.deleteByCompraCredito_Id(id);
            logger.info("Cuotas de la compra crédito ID {} eliminadas", id);

            // Eliminar la compra crédito
            compraCreditoRepository.deleteById(id);
            logger.info("Compra crédito ID {} eliminada exitosamente", id);

        } catch (Exception e) {
            logger.error("Error inesperado al remover compra crédito ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lista todas las compras a crédito que tienen cuotas pendientes de pago.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo
     * @return Lista de compras a crédito con cuotas pendientes
     */
    @Override
    @Transactional(readOnly = true)
    public List<CompraCreditoDTOResponse> listarComprasCreditoDebeCuotas(Long idEspacioTrabajo) {
        if (idEspacioTrabajo == null) {
            logger.warn("Intento de listar compras crédito con ID de espacio nulo.");
            throw new IllegalArgumentException("El ID del espacio de trabajo no puede ser nulo");
        }
        logger.info("Listando compras crédito con cuotas pendientes en espacio ID {}", idEspacioTrabajo);

        try {
            List<CompraCredito> comprasCredito = compraCreditoRepository.findByEspacioTrabajo_IdAndCuotasPendientes(idEspacioTrabajo);
            
            List<CompraCreditoDTOResponse> comprasConCuotasPendientes = comprasCredito.stream()
                .map(compraCreditoMapper::toResponse)
                .collect(Collectors.toList());

            logger.info("Se encontraron {} compras crédito con cuotas pendientes en espacio ID {}", 
                comprasConCuotasPendientes.size(), idEspacioTrabajo);
            
            return comprasConCuotasPendientes;

        } catch (Exception e) {
            logger.error("Error inesperado al listar compras crédito con cuotas pendientes en espacio ID {}: {}", 
                idEspacioTrabajo, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Busca y lista todas las compras a crédito de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo
     * @return Lista de todas las compras a crédito
     */
    @Override
    @Transactional(readOnly = true)
    public List<CompraCreditoDTOResponse> BuscarComprasCredito(Long idEspacioTrabajo) {
        if (idEspacioTrabajo == null) {
            logger.warn("Intento de buscar compras crédito con ID de espacio nulo.");
            throw new IllegalArgumentException("El ID del espacio de trabajo no puede ser nulo");
        }
        logger.info("Buscando compras crédito en espacio ID {}", idEspacioTrabajo);

        try {
            List<CompraCredito> comprasCredito = compraCreditoRepository.findByEspacioTrabajo_Id(idEspacioTrabajo);
            
            List<CompraCreditoDTOResponse> comprasCreditoResponse = comprasCredito.stream()
                .map(compraCreditoMapper::toResponse)
                .collect(Collectors.toList());

            logger.info("Se encontraron {} compras crédito en espacio ID {}", 
                comprasCreditoResponse.size(), idEspacioTrabajo);
            
            return comprasCreditoResponse;

        } catch (Exception e) {
            logger.error("Error inesperado al buscar compras crédito en espacio ID {}: {}", 
                idEspacioTrabajo, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Remueve una tarjeta del sistema.
     * Solo se permite eliminar si no tiene compras a crédito asociadas.
     * 
     * @param id ID de la tarjeta a eliminar
     * @throws EntityNotFoundException si la tarjeta no existe
     * @throws IllegalStateException si la tarjeta tiene compras asociadas
     */
    @Override
    @Transactional
    public void removerTarjeta(Long id) {
        if (id == null) {
            logger.warn("Intento de remover una tarjeta con ID nulo.");
            throw new IllegalArgumentException("El ID de la tarjeta no puede ser nulo");
        }
        logger.info("Iniciando remoción de tarjeta ID {}", id);

        try {
            if (!tarjetaRepository.existsById(id)) {
                String msg = "Tarjeta con ID " + id + " no encontrada";
                logger.warn(msg);
                throw new EntityNotFoundException(msg);
            }

            // Verificar si la tarjeta tiene compras asociadas
            if (tieneComprasAsociadas(id)) {
                String msg = "No se puede eliminar la tarjeta ID " + id + " porque tiene compras a crédito asociadas";
                logger.warn(msg);
                throw new IllegalStateException(msg);
            }

            tarjetaRepository.deleteById(id);
            logger.info("Tarjeta ID {} eliminada exitosamente", id);

        } catch (Exception e) {
            logger.error("Error inesperado al remover tarjeta ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lista todas las tarjetas de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo
     * @return Lista de tarjetas
     */
    @Override
    @Transactional(readOnly = true)
    public List<TarjetaDTOResponse> listarTarjetas(Long idEspacioTrabajo) {
        if (idEspacioTrabajo == null) {
            logger.warn("Intento de listar tarjetas con ID de espacio nulo.");
            throw new IllegalArgumentException("El ID del espacio de trabajo no puede ser nulo");
        }
        logger.info("Listando tarjetas en espacio ID {}", idEspacioTrabajo);

        try {
            List<Tarjeta> tarjetas = tarjetaRepository.findByEspacioTrabajo_Id(idEspacioTrabajo);
            
            List<TarjetaDTOResponse> tarjetasResponse = tarjetas.stream()
                .map(tarjetaMapper::toResponse)
                .collect(Collectors.toList());

            logger.info("Se encontraron {} tarjetas en espacio ID {}", tarjetasResponse.size(), idEspacioTrabajo);
            
            return tarjetasResponse;

        } catch (Exception e) {
            logger.error("Error inesperado al listar tarjetas en espacio ID {}: {}", 
                idEspacioTrabajo, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lista las cuotas de una tarjeta para el resumen del período actual.
     * Incluye cuotas cuyo vencimiento está entre el último cierre y el próximo cierre.
     * La fecha actual se calcula según la zona horaria de Buenos Aires.
     * 
     * @param idTarjeta ID de la tarjeta
     * @return Lista de cuotas del período
     */
    @Override
    @Transactional(readOnly = true)
    public List<CuotaCreditoDTOResponse> listarCuotasPorTarjeta(Long idTarjeta) {
        if (idTarjeta == null) {
            logger.warn("Intento de listar cuotas con ID de tarjeta nulo.");
            throw new IllegalArgumentException("El ID de la tarjeta no puede ser nulo");
        }
        
        // Calcular fecha actual en zona horaria de Buenos Aires
        ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");
        LocalDate fechaActual = LocalDate.now(buenosAiresZone);
        
        logger.info("Listando cuotas para tarjeta ID {} en fecha {}", idTarjeta, fechaActual);

        try {
            Tarjeta tarjeta = tarjetaRepository.findById(idTarjeta).orElseThrow(() -> {
                String msg = "Tarjeta con ID " + idTarjeta + " no encontrada";
                logger.warn(msg);
                return new EntityNotFoundException(msg);
            });

            // Calcular el rango de fechas del resumen actual
            LocalDate fechaCierreAnterior = calcularFechaCierreAnterior(fechaActual, tarjeta.getDiaCierre());
            LocalDate fechaCierreActual = calcularFechaCierreActual(fechaActual, tarjeta.getDiaCierre());

            logger.info("Buscando cuotas entre {} y {} para tarjeta ID {}", 
                fechaCierreAnterior, fechaCierreActual, idTarjeta);

            List<CuotaCredito> cuotas = cuotaCreditoRepository.findByTarjetaAndFechaVencimientoBetween(
                idTarjeta, fechaCierreAnterior, fechaCierreActual);
            
            List<CuotaCreditoDTOResponse> cuotasResponse = cuotas.stream()
                .map(cuotaCreditoMapper::toResponse)
                .collect(Collectors.toList());

            logger.info("Se encontraron {} cuotas para tarjeta ID {} en el período actual", 
                cuotasResponse.size(), idTarjeta);
            
            return cuotasResponse;

        } catch (Exception e) {
            logger.error("Error inesperado al listar cuotas para tarjeta ID {}: {}", 
                idTarjeta, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Registra el pago de un resumen de tarjeta.
     * Marca el resumen como pagado, actualiza todas sus cuotas asociadas,
     * y registra la transacción del pago.
     * 
     * @param idResumen ID del resumen a pagar
     * @param transaccion Datos de la transacción del pago
     */
    @Override
    @Transactional
    public void pagarResumenTarjeta(Long idResumen, TransaccionDTORequest transaccion) {
        logger.info("Procesando pago total del resumen ID: {} por un monto de {}", 
            idResumen, transaccion.monto());
        
        // 1. Registrar la transacción del pago
        TransaccionDTOResponse transaccionResponse = transaccionService.registrarTransaccion(transaccion);
        Transaccion transaccionEntity = transaccionRepository.findById(transaccionResponse.id())
            .orElseThrow(() -> new EntityNotFoundException(
                "Transacción no encontrada con ID: " + transaccionResponse.id()));
        
        // 2. Buscar el resumen
        Resumen resumen = resumenRepository.findById(idResumen)
            .orElseThrow(() -> new EntityNotFoundException(
                "Resumen no encontrado con ID: " + idResumen));
        
        // 3. Actualizar el resumen
        resumen.asociarTransaccion(transaccionEntity);
        resumenRepository.save(resumen);
        
        logger.info("Resumen ID: {} marcado como PAGADO", idResumen);
        
        // 4. Obtener todas las cuotas asociadas al resumen
        List<CuotaCredito> cuotasDelResumen = cuotaCreditoRepository
            .findByResumenAsociado_Id(idResumen);
        
        logger.info("Encontradas {} cuotas asociadas al resumen", cuotasDelResumen.size());
        
        // 5. Marcar cada cuota como pagada y actualizar la compra correspondiente
        for (CuotaCredito cuota : cuotasDelResumen) {
            cuota.pagarCuota();
            
            // Actualizar el contador de cuotas pagadas en la compra
            CompraCredito compra = cuota.getCompraCredito();
            compra.pagarCuota();
            compraCreditoRepository.save(compra);
            
            logger.debug("Cuota {} de CompraCredito {} marcada como pagada", 
                cuota.getNumeroCuota(), compra.getId());
        }
        
        // 6. Guardar todas las cuotas actualizadas
        cuotaCreditoRepository.saveAll(cuotasDelResumen);
        
        logger.info("Pago del resumen ID: {} procesado exitosamente. Total: ${}",
            idResumen, resumen.getMontoTotal());
    }

    /**
     * Lista todos los resúmenes de una tarjeta específica.
     * 
     * @param idTarjeta ID de la tarjeta
     * @return Lista de resúmenes ordenados por fecha descendente
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResumenDTOResponse> listarResumenesPorTarjeta(Long idTarjeta) {
        logger.info("Listando resúmenes para tarjeta ID: {}", idTarjeta);
        
        List<Resumen> resumenes = resumenRepository.findByTarjetaId(idTarjeta);
        
        logger.info("Se encontraron {} resúmenes", resumenes.size());
        
        return resumenes.stream()
            .map(resumenMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Lista todos los resúmenes de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo
     * @return Lista de resúmenes ordenados por fecha descendente
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResumenDTOResponse> listarResumenesPorEspacioTrabajo(Long idEspacioTrabajo) {
        logger.info("Listando resúmenes para espacio de trabajo ID: {}", idEspacioTrabajo);
        
        List<Resumen> resumenes = resumenRepository.findByEspacioTrabajoId(idEspacioTrabajo);
        
        logger.info("Se encontraron {} resúmenes", resumenes.size());
        
        return resumenes.stream()
            .map(resumenMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Método privado auxiliar para verificar si una tarjeta tiene compras asociadas.
     * 
     * @param idTarjeta ID de la tarjeta a verificar
     * @return true si tiene compras asociadas, false en caso contrario
     */
    private boolean tieneComprasAsociadas(Long idTarjeta) {
        return compraCreditoRepository.existsByTarjeta_Id(idTarjeta);
    }

    /**
     * Calcula la fecha del cierre anterior al período actual.
     * 
     * @param fechaActual Fecha de referencia
     * @param diaCierre Día del mes en que cierra la tarjeta
     * @return Fecha del cierre anterior
     */
    private LocalDate calcularFechaCierreAnterior(LocalDate fechaActual, Integer diaCierre) {
        LocalDate fechaCierre;
        
        if (fechaActual.getDayOfMonth() > diaCierre) {
            // Si estamos después del cierre, el cierre anterior fue este mes
            fechaCierre = fechaActual.withDayOfMonth(diaCierre);
        } else {
            // Si estamos antes del cierre, el cierre anterior fue el mes pasado
            fechaCierre = fechaActual.minusMonths(1).withDayOfMonth(diaCierre);
        }
        
        return fechaCierre;
    }

    /**
     * Calcula la fecha del cierre actual del período.
     * 
     * @param fechaActual Fecha de referencia
     * @param diaCierre Día del mes en que cierra la tarjeta
     * @return Fecha del cierre actual
     */
    private LocalDate calcularFechaCierreActual(LocalDate fechaActual, Integer diaCierre) {
        LocalDate fechaCierre;
        
        if (fechaActual.getDayOfMonth() > diaCierre) {
            // Si estamos después del cierre, el próximo cierre es el mes siguiente
            fechaCierre = fechaActual.plusMonths(1).withDayOfMonth(diaCierre);
        } else {
            // Si estamos antes del cierre, el próximo cierre es este mes
            fechaCierre = fechaActual.withDayOfMonth(diaCierre);
        }
        
        return fechaCierre;
    }

}
