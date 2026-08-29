package com.campito.backend.transacciones.service;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campito.backend.usuarios.api.EspacioTrabajoApi;
import com.campito.backend.shared.event.CompraCreditoEliminadaEvent;
import com.campito.backend.shared.event.CompraCreditoRegistradaEvent;
import com.campito.backend.shared.event.ResumenPagadoEvent;

import com.campito.backend.transacciones.repository.CompraCreditoRepository;
import com.campito.backend.transacciones.repository.ContactoTransferenciaRepository;
import com.campito.backend.transacciones.repository.CuentaBancariaRepository;
import com.campito.backend.transacciones.repository.CuotaCreditoRepository;
import com.campito.backend.transacciones.repository.MotivoTransaccionRepository;
import com.campito.backend.transacciones.repository.ResumenRepository;
import com.campito.backend.transacciones.repository.TarjetaRepository;
import com.campito.backend.transacciones.repository.TransaccionRepository;
import com.campito.backend.transacciones.domain.dto.CompraCreditoDTORequest;
import com.campito.backend.transacciones.domain.dto.CompraCreditoBusquedaDTO;
import com.campito.backend.transacciones.domain.dto.CompraCreditoDTOResponse;
import com.campito.backend.transacciones.domain.dto.CuotaCreditoDTOResponse;
import com.campito.backend.transacciones.domain.dto.CuotaResumenDTO;
import com.campito.backend.transacciones.domain.dto.PaginatedResponse;
import com.campito.backend.transacciones.domain.dto.PagarResumenTarjetaRequest;
import com.campito.backend.transacciones.domain.dto.ResumenDTOResponse;
import com.campito.backend.transacciones.domain.dto.TarjetaDTORequest;
import com.campito.backend.transacciones.domain.dto.TarjetaDTOResponse;
import com.campito.backend.transacciones.domain.dto.TarjetaDTOUpdate;
import com.campito.backend.transacciones.domain.dto.TransaccionDTORequest;
import com.campito.backend.transacciones.domain.dto.TransaccionDTOResponse;
import com.campito.backend.exception.EntidadDuplicadaException;
import com.campito.backend.exception.OperacionNoPermitidaException;
import com.campito.backend.transacciones.mapper.CompraCreditoMapper;
import com.campito.backend.transacciones.mapper.CuotaCreditoMapper;
import com.campito.backend.transacciones.mapper.ResumenMapper;
import com.campito.backend.transacciones.mapper.TarjetaMapper;
import com.campito.backend.transacciones.domain.entity.CompraCredito;

import lombok.RequiredArgsConstructor;
import com.campito.backend.transacciones.domain.entity.ContactoTransferencia;
import com.campito.backend.transacciones.domain.entity.CuentaBancaria;
import com.campito.backend.transacciones.domain.entity.CuotaCredito;
import com.campito.backend.transacciones.domain.entity.EstadoResumen;
import com.campito.backend.transacciones.domain.entity.MotivoTransaccion;
import com.campito.backend.transacciones.domain.entity.Resumen;
import com.campito.backend.transacciones.domain.entity.Tarjeta;
import com.campito.backend.transacciones.domain.entity.TipoTransaccion;
import com.campito.backend.transacciones.domain.entity.Transaccion;
import com.campito.backend.util.MoneyUtils;

import jakarta.persistence.EntityNotFoundException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import com.campito.backend.config.MetricsConfig;

/**
 * Implementación del servicio para gestión de compras a crédito y tarjetas.
 * 
 * Proporciona métodos para registrar compras a crédito, gestionar tarjetas,
 * y manejar cuotas asociadas.
 */
@Service
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
@Slf4j
public class CompraCreditoServiceImpl implements CompraCreditoService {

    private final CompraCreditoRepository compraCreditoRepository;
    private final EspacioTrabajoApi espacioTrabajoApi;
    private final MotivoTransaccionRepository motivoRepository;
    private final ContactoTransferenciaRepository contactoRepository;
    private final CuentaBancariaRepository cuentaBancariaRepository;
    private final CuotaCreditoRepository cuotaCreditoRepository;
    private final TarjetaRepository tarjetaRepository;
    private final TransaccionRepository transaccionRepository;
    private final ResumenRepository resumenRepository;

    private final CompraCreditoMapper compraCreditoMapper;
    private final TarjetaMapper tarjetaMapper;
    private final CuotaCreditoMapper cuotaCreditoMapper;
    private final ResumenMapper resumenMapper;

    private final TransaccionService transaccionService;

    private final ApplicationEventPublisher eventPublisher;

    private final MeterRegistry meterRegistry;  // Para métricas de Prometheus/Grafana

    /**
     * Registra una compra a crédito en el sistema.
     * 
     * @param compraCreditoDTO Datos de la compra a crédito a registrar.
     * @return Respuesta con los detalles de la compra registrada.
     * @throws EntityNotFoundException si el espacio de trabajo, motivo, comercio o tarjeta no existen.
     */
    @Override
    @Transactional
    public CompraCreditoDTOResponse registrarCompraCredito(CompraCreditoDTORequest compraCreditoDTO) {

        log.info("Iniciando registro de compraCredito por monto {} con cantidad de cuotas {} en espacio ID {}", compraCreditoDTO.montoTotal(), compraCreditoDTO.cantidadCuotas(), compraCreditoDTO.espacioTrabajoId());

        UUID idEspacio = compraCreditoDTO.espacioTrabajoId();
        if (!espacioTrabajoApi.existe(idEspacio)) {
            String msg = "Espacio de trabajo con ID " + idEspacio + " no encontrado";
            log.warn(msg);
            throw new EntityNotFoundException(msg);
        }
        MotivoTransaccion motivo = buscarMotivoPorId(compraCreditoDTO.motivoId());
        Tarjeta tarjeta = buscarTarjetaPorId(compraCreditoDTO.tarjetaId());

        CompraCredito compraCredito = compraCreditoMapper.toEntity(compraCreditoDTO);
        compraCredito.setIdEspacioTrabajo(idEspacio);
        compraCredito.setNombreEspacioTrabajo(espacioTrabajoApi.obtenerNombre(idEspacio));

        if (compraCreditoDTO.comercioId() != null) {
            ContactoTransferencia comercio = buscarComercioPorId(compraCreditoDTO.comercioId());

            // Actualizar manualmente fecha_modificacion para que el comercio aparezca primero
            comercio.setFechaModificacion(LocalDateTime.now());
            ContactoTransferencia comercioGuardado = contactoRepository.save(comercio);
            log.info("Contacto ID {} actualizado tras registro de transaccion", comercioGuardado.getId());

            compraCredito.setComercio(comercioGuardado);
        }

        ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");
        ZonedDateTime nowInBuenosAires = ZonedDateTime.now(buenosAiresZone);
        compraCredito.setFechaCreacion(nowInBuenosAires.toLocalDateTime());

        // Actualizar manualmente fecha_modificacion para que el motivo aparezca primero
        motivo.setFechaModificacion(LocalDateTime.now());
        MotivoTransaccion motivoGuardado = motivoRepository.save(motivo);
        log.info("Motivo ID {} actualizado tras registro de transaccion", motivoGuardado.getId());

        // Actualizar manualmente fecha_modificacion para que la tarjeta aparezca primero
        tarjeta.setFechaModificacion(LocalDateTime.now());
        Tarjeta tarjetaGuardada = tarjetaRepository.save(tarjeta);
        log.info("Tarjeta ID {} actualizado tras registro de transaccion", tarjetaGuardada.getId());

        compraCredito.setMotivo(motivoGuardado);
        compraCredito.setTarjeta(tarjetaGuardada);

        CompraCredito compraCreditoGuardada = compraCreditoRepository.save(compraCredito);
        crearCuotas(compraCreditoGuardada);
        eventPublisher.publishEvent(new CompraCreditoRegistradaEvent(
                idEspacio, compraCreditoGuardada.getMontoTotal(), compraCreditoGuardada.getFechaCompra()));
        log.info("Compra credito ID {} registrada exitosamente en espacio ID {}.", compraCreditoGuardada.getId(), idEspacio);
        
        // 📊 MÉTRICA: Incrementar contador de compras a crédito registradas
        Counter.builder(MetricsConfig.MetricNames.COMPRAS_CREDITO_CREADAS)
                .description("Total de compras a crédito registradas exitosamente")
                .tag(MetricsConfig.TagNames.ESPACIO_TRABAJO, idEspacio.toString())
                .tag("tarjeta_id", tarjeta.getId().toString())
                .tag("cuotas", String.valueOf(compraCreditoGuardada.getCantidadCuotas()))
                .register(meterRegistry)
                .increment();
        
        return compraCreditoMapper.toResponse(compraCreditoGuardada);
    }

    /**
     * Metodo que registra una nueva tarjeta en un espacio de trabajo.
     * 
     * @param tarjetaDTO Datos de la tarjeta a registrar.
     * @return Respuesta con los detalles de la tarjeta registrada.
     * @throws EntityNotFoundException si el espacio de trabajo no existe.
    */
    @Override
    @Transactional
    public TarjetaDTOResponse registrarTarjeta(TarjetaDTORequest tarjetaDTO) {

        log.info("Iniciando registro de tarjeta {} en espacio ID {}", tarjetaDTO.numeroTarjeta(), tarjetaDTO.espacioTrabajoId());

        // Validar que no exista una tarjeta con la misma combinación en el espacio de trabajo
        Optional<Tarjeta> tarjetaExistente = tarjetaRepository
                .findFirstByNumeroTarjetaAndEntidadFinancieraAndRedDePagoAndIdEspacioTrabajo(
                    tarjetaDTO.numeroTarjeta(), 
                    tarjetaDTO.entidadFinanciera(), 
                    tarjetaDTO.redDePago(), 
                    tarjetaDTO.espacioTrabajoId());
        
        if (tarjetaExistente.isPresent()) {
            String msg = String.format("Ya existe una tarjeta %s terminada en %s de %s en este espacio de trabajo. Por favor, verifica los datos.", 
                    tarjetaDTO.redDePago(), 
                    tarjetaDTO.numeroTarjeta(), 
                    tarjetaDTO.entidadFinanciera());
            log.warn(msg);
            throw new EntidadDuplicadaException(msg);
        }

        if (!espacioTrabajoApi.existe(tarjetaDTO.espacioTrabajoId())) {
            String msg = "Espacio de trabajo con ID " + tarjetaDTO.espacioTrabajoId() + " no encontrado";
            log.warn(msg);
            throw new EntityNotFoundException(msg);
        }

        Tarjeta tarjeta = tarjetaMapper.toEntity(tarjetaDTO);
        tarjeta.setIdEspacioTrabajo(tarjetaDTO.espacioTrabajoId());

        Tarjeta tarjetaGuardada = tarjetaRepository.save(tarjeta);
        log.info("Tarjeta ID {} registrada exitosamente en espacio ID {}.", tarjetaGuardada.getId(), tarjetaDTO.espacioTrabajoId());
        
        return tarjetaMapper.toResponse(tarjetaGuardada);
    }

    /**
     * Remueve una compra a crédito del sistema.
     * Solo se permite eliminar si ninguna cuota ha sido pagada.
     * 
     * @param id ID de la compra a crédito a eliminar
     * @throws EntityNotFoundException si la compra no existe
     * @throws OperacionNoPermitidaException si alguna cuota ya fue pagada
     */
    @Override
    @Transactional
    public void removerCompraCredito(Long id) {

        log.info("Iniciando remoción de compra crédito ID {}", id);

        CompraCredito compraCredito = buscarCompraCreditoPorId(id);

        // Verificar si alguna cuota ya fue pagada
        List<CuotaCredito> cuotasPagadas = cuotaCreditoRepository.findByCompraCredito_IdAndPagada(id, true);
        if (!cuotasPagadas.isEmpty()) {
            String msg = String.format("No se puede eliminar esta compra a crédito porque ya tiene %d cuota(s) pagada(s). Solo se pueden eliminar compras sin cuotas pagadas.", cuotasPagadas.size());
            log.warn(msg);
            throw new OperacionNoPermitidaException(msg);
        }

        // Eliminar todas las cuotas asociadas
        cuotaCreditoRepository.deleteByCompraCredito_Id(id);
        log.info("Cuotas de la compra crédito ID {} eliminadas", id);

        // Revertir el impacto en GastosIngresosMensuales via evento síncrono del dashboard
        eventPublisher.publishEvent(new CompraCreditoEliminadaEvent(
                compraCredito.getIdEspacioTrabajo(), compraCredito.getMontoTotal(), compraCredito.getFechaCompra()));

        // Eliminar la compra crédito
        compraCreditoRepository.deleteById(id);
        log.info("Compra crédito ID {} eliminada exitosamente", id);
    }

    /**
     * Lista todas las compras a crédito que tienen cuotas pendientes de pago con soporte de paginación.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo
     * @param page Número de página (basado en 0)
     * @param size Tamaño de página
     * @return Respuesta paginada con las compras a crédito con cuotas pendientes
     */
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CompraCreditoDTOResponse> listarComprasCreditoDebeCuotas(
            UUID idEspacioTrabajo, Integer page, Integer size) {

        // Valores por defecto para paginación
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        
        log.info("Listando compras crédito con cuotas pendientes en espacio ID {} (página {}, tamaño {})", 
            idEspacioTrabajo, pageNumber, pageSize);

        // Crear el Pageable con ordenamiento por fecha de compra descendente
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "fechaCompra"));
        
        Page<CompraCredito> comprasCreditoPage = compraCreditoRepository
            .findByIdEspacioTrabajoAndCuotasPendientesPageable(idEspacioTrabajo, pageable);
        
        Page<CompraCreditoDTOResponse> comprasDTOPage = comprasCreditoPage.map(compraCreditoMapper::toResponse);

        log.info("Se encontraron {} compras crédito con cuotas pendientes en espacio ID {} (página {} de {})", 
            comprasCreditoPage.getTotalElements(), idEspacioTrabajo, pageNumber, comprasCreditoPage.getTotalPages());
        
        return new PaginatedResponse<>(comprasDTOPage);
    }

    /**
     * Busca y lista todas las compras a crédito de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo
     * @return Lista de todas las compras a crédito
     */
    @Override
    @Transactional(readOnly = true)
    public List<CompraCreditoDTOResponse> BuscarComprasCredito(UUID idEspacioTrabajo) {

        log.info("Buscando compras crédito en espacio ID {}", idEspacioTrabajo);

        List<CompraCredito> comprasCredito = compraCreditoRepository.findByIdEspacioTrabajo(idEspacioTrabajo);
        
        List<CompraCreditoDTOResponse> comprasCreditoResponse = comprasCredito.stream()
            .map(compraCreditoMapper::toResponse)
            .collect(Collectors.toList());

        log.info("Se encontraron {} compras crédito en espacio ID {}", 
            comprasCreditoResponse.size(), idEspacioTrabajo);
        
        return comprasCreditoResponse;
    }

    /**
     * Busca compras a crédito aplicando filtros opcionales con soporte de paginación.
     *
     * @param datosBusqueda Criterios de búsqueda (espacio de trabajo, año, mes, motivo, contacto, página, tamaño).
     * @return Respuesta paginada con las compras a crédito que cumplen los criterios.
     * @throws IllegalArgumentException si se especifica mes sin año.
     */
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CompraCreditoDTOResponse> buscarComprasCredito(CompraCreditoBusquedaDTO datosBusqueda) {

        log.info("Iniciando busqueda de compras crédito para espacio ID {} con criterios: {}",
            datosBusqueda.idEspacioTrabajo(), datosBusqueda);

        int page = datosBusqueda.page() != null ? datosBusqueda.page() : 0;
        int size = datosBusqueda.size() != null ? datosBusqueda.size() : 10;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCompra"));

        Specification<CompraCredito> spec = (root, query, cb) ->
            cb.equal(root.get("idEspacioTrabajo"), datosBusqueda.idEspacioTrabajo());

        if (datosBusqueda.anio() != null) {
            int anio = datosBusqueda.anio();
            int mes = datosBusqueda.mes() != null ? datosBusqueda.mes() : 1;
            LocalDate desde = LocalDate.of(anio, mes, 1);
            LocalDate hasta = datosBusqueda.mes() != null
                ? desde.withDayOfMonth(desde.lengthOfMonth())
                : LocalDate.of(anio, 12, 31);
            spec = spec.and((root, query, cb) -> cb.between(root.get("fechaCompra"), desde, hasta));
        } else if (datosBusqueda.mes() != null) {
            log.warn("Se especifico mes sin anio en la busqueda de compras crédito para espacio ID {}.",
                datosBusqueda.idEspacioTrabajo());
            throw new IllegalArgumentException("Si no se especifica el año, no se puede especificar el mes");
        }

        if (datosBusqueda.motivo() != null && !datosBusqueda.motivo().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("motivo").get("motivo")), "%" + datosBusqueda.motivo().toLowerCase() + "%"));
        }
        if (datosBusqueda.contacto() != null && !datosBusqueda.contacto().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("comercio").get("nombre")), "%" + datosBusqueda.contacto().toLowerCase() + "%"));
        }

        var comprasPage = compraCreditoRepository.findAll(spec, pageable);
        log.info(
            "Busqueda de compras crédito para espacio ID {} finalizada. Se encontraron {} resultados en la página {} de {}.",
            datosBusqueda.idEspacioTrabajo(), comprasPage.getTotalElements(), page, comprasPage.getTotalPages());

        var comprasDTO = comprasPage.map(compraCreditoMapper::toResponse);
        return new PaginatedResponse<>(comprasDTO);
    }

    /**
     * Remueve una tarjeta del sistema.
     * Solo se permite eliminar si no tiene compras asociadas.
     * 
     * @param id ID de la tarjeta a eliminar
     * @throws EntityNotFoundException si la tarjeta no existe
     * @throws OperacionNoPermitidaException si la tarjeta tiene compras asociadas
     */
    @Override
    @Transactional
    public void removerTarjeta(Long id) {

        log.info("Iniciando remoción de tarjeta ID {}", id);

        if (!tarjetaRepository.existsById(id)) {
            String msg = "Tarjeta con ID " + id + " no encontrada";
            log.warn(msg);
            throw new EntityNotFoundException(msg);
        }

        // Verificar si la tarjeta tiene compras asociadas
        if (tieneComprasAsociadas(id)) {
            String msg = "No se puede eliminar esta tarjeta porque tiene compras a crédito asociadas.";
            log.warn(msg);
            throw new OperacionNoPermitidaException(msg);
        }

        tarjetaRepository.deleteById(id);
        log.info("Tarjeta ID {} eliminada exitosamente", id);
    }

    /**
     * Lista todas las tarjetas de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo
     * @return Lista de tarjetas
     */
    @Override
    @Transactional(readOnly = true)
    public List<TarjetaDTOResponse> listarTarjetas(UUID idEspacioTrabajo) {

        log.info("Listando tarjetas en espacio ID {}", idEspacioTrabajo);

        List<Tarjeta> tarjetas = tarjetaRepository.findByIdEspacioTrabajo(idEspacioTrabajo);
        
        List<TarjetaDTOResponse> tarjetasResponse = tarjetas.stream()
            .map(tarjetaMapper::toResponse)
            .collect(Collectors.toList());

        log.info("Se encontraron {} tarjetas en espacio ID {}", tarjetasResponse.size(), idEspacioTrabajo);
        
        return tarjetasResponse;
    }

    /**
     * Lista las cuotas de una tarjeta para el resumen del período actual.
     * Incluye cuotas cuyo vencimiento está entre el último cierre y el próximo cierre.
     * La fecha actual se calcula según la zona horaria de Buenos Aires.
     * 
     * @param idTarjeta ID de la tarjeta
     * @return Lista de cuotas del período
     * @throws EntityNotFoundException si la tarjeta no existe
     */
    @Override
    @Transactional(readOnly = true)
    public List<CuotaCreditoDTOResponse> listarCuotasPorTarjeta(Long idTarjeta) {

        // Calcular fecha actual en zona horaria de Buenos Aires
        ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");
        LocalDate fechaActual = LocalDate.now(buenosAiresZone);
        
        log.info("Listando cuotas para tarjeta ID {} en fecha {}", idTarjeta, fechaActual);

        Tarjeta tarjeta = buscarTarjetaPorId(idTarjeta);

        // Calcular el rango de fechas del resumen actual
        LocalDate fechaCierreAnterior = calcularFechaCierreAnterior(fechaActual, tarjeta.getDiaCierre());
        LocalDate fechaCierreActual = calcularFechaCierreActual(fechaActual, tarjeta.getDiaCierre());

        log.info("Buscando cuotas entre {} y {} para tarjeta ID {}", 
            fechaCierreAnterior, fechaCierreActual, idTarjeta);

        List<CuotaCredito> cuotas = cuotaCreditoRepository.findByTarjetaAndFechaVencimientoBetween(
            idTarjeta, fechaCierreAnterior, fechaCierreActual);
        
        List<CuotaCreditoDTOResponse> cuotasResponse = cuotas.stream()
            .map(cuotaCreditoMapper::toResponse)
            .collect(Collectors.toList());

        log.info("Se encontraron {} cuotas para tarjeta ID {} en el período actual", 
            cuotasResponse.size(), idTarjeta);
        
        return cuotasResponse;
    }

    /**
     * Registra el pago de un resumen de tarjeta.
     * Marca el resumen como pagado, actualiza todas sus cuotas asociadas,
     * y registra la transacción del pago.
     * 
     * @param request Datos del pago del resumen
     * @throws EntityNotFoundException si el resumen, cuenta bancaria o transacción no existen
     * @throws IllegalStateException si el resumen ya está pagado o aún no cerró
     * @throws IllegalArgumentException si los datos no son válidos
     */
    @Override
    @Transactional
    public void pagarResumenTarjeta(PagarResumenTarjetaRequest request) {
        log.info("Procesando pago del resumen ID: {} por un monto de {}", 
            request.idResumen(), request.monto());
        
        // 1. Buscar el resumen y validar su estado
        Resumen resumen = buscarResumenPorId(request.idResumen());
        validarEstadoResumen(resumen, request);

        // 2. Buscar o crear el motivo "Pago de tarjeta" usando Optional.orElseGet()
        MotivoTransaccion motivo = motivoRepository
            .findFirstByMotivoAndIdEspacioTrabajo("Pago de tarjeta", request.idEspacioTrabajo())
            .orElseGet(() -> {
                log.info("Creando motivo 'Pago de tarjeta' para espacio de trabajo ID: {}", 
                    request.idEspacioTrabajo());
                MotivoTransaccion nuevoMotivo = MotivoTransaccion.builder()
                    .motivo("Pago de tarjeta")
                    .idEspacioTrabajo(resumen.getTarjeta().getIdEspacioTrabajo())
                    .build();
                return motivoRepository.save(nuevoMotivo);
            });

        // 3. Registrar la transacción del pago
        TransaccionDTORequest transaccionDTO = new TransaccionDTORequest(
            request.fecha(),
            request.monto(),
            TipoTransaccion.GASTO,
            "Pago resumen " + resumen.getMes() + "/" + resumen.getAnio() + 
                " - " + resumen.getTarjeta().getNumeroTarjeta(),
            request.nombreCompletoAuditoria(),
            request.idEspacioTrabajo(),
            motivo.getId(),
            null,
            request.idCuentaBancaria()
        );
        
        TransaccionDTOResponse transaccionResponse = transaccionService.registrarTransaccion(transaccionDTO);
        Transaccion transaccion = buscarTransaccionPorId(transaccionResponse.id());
        
        // 4. Actualizar el resumen (asociar transacción y cambiar estado)
        resumen.asociarTransaccion(transaccion);
        resumenRepository.save(resumen);
        
        log.info("Resumen ID: {} marcado como PAGADO", request.idResumen());
        
        // 5. Obtener y marcar las cuotas como pagadas
        List<CuotaCredito> cuotasDelResumen = cuotaCreditoRepository
            .findByResumenAsociado_Id(request.idResumen());
        
        if (cuotasDelResumen.isEmpty()) {
            log.warn("No se encontraron cuotas asociadas al resumen ID: {}", request.idResumen());
        }
        
        log.info("Encontradas {} cuotas asociadas al resumen", cuotasDelResumen.size());
        
        // 6. Marcar cada cuota como pagada y actualizar la compra correspondiente
        for (CuotaCredito cuota : cuotasDelResumen) {
            if (cuota.isPagada()) {
                log.warn("La cuota ID: {} ya estaba marcada como pagada", cuota.getId());
                continue;
            }
            
            cuota.pagarCuota();
            
            CompraCredito compra = cuota.getCompraCredito();
            compra.pagarCuota();
            compraCreditoRepository.save(compra);
            
            log.debug("Cuota {} de CompraCredito {} marcada como pagada", 
                cuota.getNumeroCuota(), compra.getId());
        }
        
        cuotaCreditoRepository.saveAll(cuotasDelResumen);
        
        // Anotar el pago del resumen en GastosIngresosMensuales del mes del resumen via evento síncrono
        eventPublisher.publishEvent(new ResumenPagadoEvent(
                request.idEspacioTrabajo(), resumen.getMontoTotal(), resumen.getAnio(), resumen.getMes()));

        // 📊 MÉTRICA: Incrementar contador de resúmenes pagados
        Counter.builder(MetricsConfig.MetricNames.RESUMENES_PAGADOS)
                .description("Total de resúmenes de tarjetas pagados exitosamente")
                .tag(MetricsConfig.TagNames.ESPACIO_TRABAJO, resumen.getTarjeta().getIdEspacioTrabajo().toString())
                .tag("tarjeta_id", resumen.getTarjeta().getId().toString())
                .register(meterRegistry)
                .increment();
        
        log.info("Pago del resumen ID: {} procesado exitosamente. Total: ${}", 
            request.idResumen(), resumen.getMontoTotal());
    }

    /**
     * Lista todos los resúmenes pendientes de pago de una tarjeta específica.
     * 
     * @param idTarjeta ID de la tarjeta
     * @return Lista de resúmenes ordenados por fecha descendente
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResumenDTOResponse> listarResumenesPorTarjeta(Long idTarjeta) {

        log.info("Listando resúmenes pendientes de pago para tarjeta ID: {}", idTarjeta);
        
        // Solo devolver resúmenes con estado CERRADO o PAGADO_PARCIAL (excluir PAGADO y ABIERTO)
        List<Resumen> resumenes = resumenRepository.findByTarjetaIdAndEstadoIn(
            idTarjeta, 
            List.of(EstadoResumen.CERRADO, EstadoResumen.PAGADO_PARCIAL)
        );
        
        log.info("Se encontraron {} resúmenes pendientes de pago", resumenes.size());
        
        return resumenes.stream()
            .map(this::mapearResumenConCuotas)
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
    public List<ResumenDTOResponse> listarResumenesPorEspacioTrabajo(UUID idEspacioTrabajo) {

        log.info("Listando resúmenes para espacio de trabajo ID: {}", idEspacioTrabajo);
        
        List<Resumen> resumenes = resumenRepository.findByEspacioTrabajoId(idEspacioTrabajo);
    
        log.info("Se encontraron {} resúmenes", resumenes.size());
        
        return resumenes.stream()
            .map(resumenMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Modifica los días de cierre y vencimiento de pago de una tarjeta existente.
     * 
     * @param id ID de la tarjeta a modificar
     * @param tarjetaUpdate DTO con los campos a actualizar
     * @return Respuesta con los detalles de la tarjeta modificada
     * @throws EntityNotFoundException si la tarjeta no existe
     */
    @Override
    @Transactional
    public TarjetaDTOResponse modificarTarjeta(Long id, TarjetaDTOUpdate tarjetaUpdate) {
        log.info("Iniciando modificación de tarjeta ID {}", id);

        Tarjeta tarjeta = buscarTarjetaPorId(id);

        tarjetaMapper.updateEntity(tarjetaUpdate, tarjeta);

        // Actualizar manualmente fecha_modificacion para que la tarjeta aparezca primero
        tarjeta.setFechaModificacion(LocalDateTime.now());
        Tarjeta tarjetaGuardada = tarjetaRepository.save(tarjeta);
        log.info("Tarjeta ID {} modificada exitosamente.", tarjetaGuardada.getId());
        
        return tarjetaMapper.toResponse(tarjetaGuardada);
    }

    /*
    ===========================================================================
        MÉTODOS AUXILIARES PRIVADOS
    ===========================================================================
    */
    
    /**
     * Metodo privado que crea las cuotas asociadas a una compra a crédito.
     * @param compraCredito
     */
    private void crearCuotas(CompraCredito compraCredito) {
        Tarjeta tarjeta = compraCredito.getTarjeta();

        if (compraCredito.getCantidadCuotas() <= 0) {
            log.warn("Intento de crear cuotas para una compra con 0 o menos cuotas. Compra ID: {}", compraCredito.getId());
            return;
        }

        BigDecimal montoCuota = MoneyUtils.divide(compraCredito.getMontoTotal(), compraCredito.getCantidadCuotas());
        
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
        log.info("Se crearon {} cuotas para la compra a crédito ID {}", compraCredito.getCantidadCuotas(), compraCredito.getId());
    }

    /**
     * Método privado auxiliar para mapear un Resumen a ResumenDTOResponse incluyendo sus cuotas.
     * 
     * @param resumen Entidad Resumen
     * @return ResumenDTOResponse con cuotas cargadas
     */
    private ResumenDTOResponse mapearResumenConCuotas(Resumen resumen) {
        // Obtener las cuotas asociadas al resumen
        List<CuotaCredito> cuotas = cuotaCreditoRepository.findByResumenAsociado_Id(resumen.getId());
        
        // Mapear las cuotas a CuotaResumenDTO
        List<CuotaResumenDTO> cuotasDTO = cuotas.stream()
            .map(cuota -> new CuotaResumenDTO(
                cuota.getId(),
                cuota.getNumeroCuota(),
                cuota.getMontoCuota(),
                cuota.getCompraCredito().getDescripcion() != null 
                    ? cuota.getCompraCredito().getDescripcion() 
                    : "Compra",
                cuota.getCompraCredito().getCantidadCuotas(),
                cuota.getCompraCredito().getMotivo().getMotivo()
            ))
            .collect(Collectors.toList());
        
        // Construir el ResumenDTOResponse directamente desde la entidad
        return new ResumenDTOResponse(
            resumen.getId(),
            resumen.getAnio(),
            resumen.getMes(),
            resumen.getFechaVencimiento(),
            resumen.getEstado(),
            resumen.getMontoTotal(),
            resumen.getTarjeta().getId(),
            resumen.getTarjeta().getNumeroTarjeta(),
            resumen.getTarjeta().getEntidadFinanciera(),
            resumen.getTarjeta().getRedDePago(),
            resumen.getTransaccionAsociada() != null ? resumen.getTransaccionAsociada().getId() : null,
            cuotasDTO.size(),
            cuotasDTO
        );
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

    private MotivoTransaccion buscarMotivoPorId(Long idMotivo) {
        return motivoRepository.findById(idMotivo).orElseThrow(() -> {
            String msg = "Motivo de transaccion con ID " + idMotivo + " no encontrado";
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });
    }

    private ContactoTransferencia buscarComercioPorId(Long idComercio) {
        return contactoRepository.findById(idComercio).orElseThrow(() -> {
            String msg = "Comercio con ID " + idComercio + " no encontrado";
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });
    }

    private Tarjeta buscarTarjetaPorId(Long idTarjeta) {
        return tarjetaRepository.findById(idTarjeta).orElseThrow(() -> {
            String msg = "Tarjeta con ID " + idTarjeta + " no encontrada";
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });
    }

    private CompraCredito buscarCompraCreditoPorId(Long idCompraCredito) {
        return compraCreditoRepository.findById(idCompraCredito).orElseThrow(() -> {
            String msg = "Compra crédito con ID " + idCompraCredito + " no encontrada";
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });
    }

    private Resumen buscarResumenPorId(Long idResumen) {
        return resumenRepository.findById(idResumen).orElseThrow(() -> {
            String msg = "Resumen con ID " + idResumen + " no encontrado";
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });
    }

    private Transaccion buscarTransaccionPorId(Long idTransaccion) {
        return transaccionRepository.findById(idTransaccion).orElseThrow(() -> {
            String msg = "Transacción con ID " + idTransaccion + " no encontrada";
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });
    }

    private void validarEstadoResumen(Resumen resumen, PagarResumenTarjetaRequest request) {
        // Validar que el resumen esté en estado válido para pago
        if (resumen.getEstado().equals(EstadoResumen.PAGADO)) {
            throw new IllegalStateException(
                "El resumen ID " + request.idResumen() + " ya está pagado");
        }
        
        if (resumen.getEstado().equals(EstadoResumen.ABIERTO)) {
            throw new IllegalStateException(
                "No se puede pagar un resumen que aún no cerró");
        }
        
        // Validar que el espacio de trabajo coincida
        if (!resumen.getTarjeta().getIdEspacioTrabajo().equals(request.idEspacioTrabajo())) {
            throw new IllegalArgumentException(
                "El resumen no pertenece al espacio de trabajo especificado");
        }
        
        if (request.monto().compareTo(resumen.getMontoTotal()) != 0) {
            throw new IllegalArgumentException(
                "El monto a pagar debe ser igual al total del resumen: $" + resumen.getMontoTotal());
        }
        
        // Validar cuenta bancaria si se especificó
        if (request.idCuentaBancaria() != null) {
            CuentaBancaria cuenta = cuentaBancariaRepository.findById(request.idCuentaBancaria())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Cuenta bancaria no encontrada con ID: " + request.idCuentaBancaria()));
            
            if (!cuenta.getIdEspacioTrabajo().equals(request.idEspacioTrabajo())) {
                throw new IllegalArgumentException(
                    "La cuenta bancaria no pertenece al espacio de trabajo especificado");
            }
        }
    }
    
}
