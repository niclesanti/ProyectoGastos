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

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

// Dependencias a revisar para reducir el acomplamiento con otros módulos
import com.campito.backend.usuarios.repository.EspacioTrabajoRepository;
import com.campito.backend.dashboard.repository.GastosIngresosMensualesRepository;
import com.campito.backend.usuarios.domain.entity.EspacioTrabajo;
import com.campito.backend.dashboard.domain.entity.GastosIngresosMensuales;

import com.campito.backend.transacciones.repository.ContactoTransferenciaRepository;
import com.campito.backend.transacciones.repository.CuentaBancariaRepository;
import com.campito.backend.transacciones.repository.MotivoTransaccionRepository;
import com.campito.backend.transacciones.repository.TransaccionRepository;
import com.campito.backend.transacciones.domain.dto.ContactoDTORequest;
import com.campito.backend.transacciones.domain.dto.ContactoDTOResponse;
import com.campito.backend.transacciones.domain.dto.MotivoDTORequest;
import com.campito.backend.transacciones.domain.dto.MotivoDTOResponse;
import com.campito.backend.transacciones.domain.dto.PaginatedResponse;
import com.campito.backend.transacciones.domain.dto.TransaccionBusquedaDTO;
import com.campito.backend.transacciones.domain.dto.TransaccionDTORequest;
import com.campito.backend.transacciones.domain.dto.TransaccionDTOResponse;
import com.campito.backend.transacciones.mapper.ContactoTransferenciaMapper;
import com.campito.backend.transacciones.mapper.MotivoTransaccionMapper;
import com.campito.backend.transacciones.mapper.TransaccionMapper;
import com.campito.backend.transacciones.domain.entity.ContactoTransferencia;
import com.campito.backend.transacciones.domain.entity.CuentaBancaria;
import com.campito.backend.transacciones.domain.entity.MotivoTransaccion;
import com.campito.backend.transacciones.domain.entity.TipoTransaccion;
import com.campito.backend.transacciones.domain.entity.Transaccion;

import com.campito.backend.exception.EntidadDuplicadaException;
import com.campito.backend.exception.SaldoInsuficienteException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import com.campito.backend.config.MetricsConfig;

/**
 * Implementación del servicio para gestión de transacciones.
 * 
 * Proporciona métodos para registrar transacciones, removerlas, buscarlas,
 * y gestionar contactos y motivos de transacciones.
 */
@Service
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
@Slf4j
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final EspacioTrabajoRepository espacioRepository;
    private final MotivoTransaccionRepository motivoRepository;
    private final ContactoTransferenciaRepository contactoRepository;
    private final CuentaBancariaRepository cuentaBancariaRepository;
    private final GastosIngresosMensualesRepository gastosIngresosMensualesRepository;
    private final CuentaBancariaService cuentaBancariaService;
    private final TransaccionMapper transaccionMapper;
    private final ContactoTransferenciaMapper contactoTransferenciaMapper;
    private final MotivoTransaccionMapper motivoTransaccionMapper;
    private final MeterRegistry meterRegistry;  // Para métricas de Prometheus/Grafana

    /**
     * Registra una nueva transacción.
     * 
     * @param transaccionDTO Datos de la transacción a registrar.
     * @return DTO de respuesta con los datos de la transacción registrada.
     * @throws EntityNotFoundException si el espacio de trabajo, motivo o contacto no se encuentran.
     */
    @Override
    @Transactional
    public TransaccionDTOResponse registrarTransaccion(TransaccionDTORequest transaccionDTO) {

        log.info("Iniciando registro de transaccion tipo {} por monto {} en espacio ID {}", transaccionDTO.tipo(), transaccionDTO.monto(), transaccionDTO.idEspacioTrabajo());

        EspacioTrabajo espacio = buscarEspacioTrabajoPorId(transaccionDTO.idEspacioTrabajo());
        MotivoTransaccion motivo = buscarMotivoPorId(transaccionDTO.idMotivo());

        Transaccion transaccion = transaccionMapper.toEntity(transaccionDTO);

        gastosIgresosMesAnotar(transaccion.getTipo(), transaccion.getMonto(), espacio.getId(), transaccion.getFecha());

        if (transaccionDTO.idContacto() != null) {
            ContactoTransferencia contacto = buscarContactoPorId(transaccionDTO.idContacto());

            // Actualizar manualmente fecha_modificacion para que el contacto aparezca primero
            contacto.setFechaModificacion(LocalDateTime.now());
            ContactoTransferencia contactoGuardado = contactoRepository.save(contacto);
            log.info("Contacto ID {} actualizado tras registro de transaccion", contactoGuardado.getId());

            transaccion.setContacto(contactoGuardado);
        }

        if(transaccionDTO.idCuentaBancaria() != null) {
            CuentaBancaria cuenta = cuentaBancariaService.actualizarCuentaBancaria(transaccionDTO.idCuentaBancaria(), transaccionDTO.tipo(), transaccionDTO.monto());
            transaccion.setCuentaBancaria(cuenta);
        }

        ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");
        ZonedDateTime nowInBuenosAires = ZonedDateTime.now(buenosAiresZone);
        transaccion.setFechaCreacion(nowInBuenosAires.toLocalDateTime());

        espacio.actualizarSaldoNuevaTransaccion(transaccion.getMonto(), transaccion.getTipo());
        espacioRepository.save(espacio);

        // Actualizar manualmente fecha_modificacion para que el motivo aparezca primero
        motivo.setFechaModificacion(LocalDateTime.now());
        MotivoTransaccion motivoGuardado = motivoRepository.save(motivo);
        log.info("Motivo ID {} actualizado tras registro de transaccion", motivoGuardado.getId());

        transaccion.setEspacioTrabajo(espacio);
        transaccion.setMotivo(motivoGuardado);

        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);
        log.info("Transaccion ID {} registrada exitosamente en espacio ID {}. Nuevo saldo: {}", transaccionGuardada.getId(), espacio.getId(), espacio.getSaldo());
        
        // 📊 MÉTRICA: Incrementar contador de transacciones creadas
        Counter.builder(MetricsConfig.MetricNames.TRANSACCIONES_CREADAS)
                .description("Total de transacciones registradas exitosamente")
                .tag(MetricsConfig.TagNames.TIPO_TRANSACCION, transaccion.getTipo().name())
                .tag(MetricsConfig.TagNames.ESPACIO_TRABAJO, espacio.getId().toString())
                .register(meterRegistry)
                .increment();
        
        return transaccionMapper.toResponse(transaccionGuardada);

    }

    /**
     * Remueve una transacción existente y revierte el impacto en el saldo.
     * 
     * @param id ID de la transacción a remover.
     * @throws EntityNotFoundException si la transacción no se encuentra.
     */
    @Override
    @Transactional
    public void removerTransaccion(Long id) {

        log.info("Iniciando remocion de transaccion ID: {}", id);

        Transaccion transaccion = buscarTransaccionPorId(id);

        EspacioTrabajo espacio = transaccion.getEspacioTrabajo();
        espacio.actualizarSaldoEliminarTransaccion(transaccion.getMonto(), transaccion.getTipo());

        if(transaccion.getCuentaBancaria() != null) {
            CuentaBancaria cuenta = transaccion.getCuentaBancaria();
            
            cuenta.actualizarSaldoEliminarTransaccion(transaccion.getMonto(), transaccion.getTipo());
            cuentaBancariaRepository.save(cuenta);
            log.info("Saldo de cuenta bancaria ID {} actualizado a {} tras remocion de transaccion ID {}", cuenta.getId(), cuenta.getSaldoActual(), id);
        }

        gastosIngresosMesDelete(transaccion.getTipo(), transaccion.getMonto(), espacio.getId(), transaccion.getFecha());

        transaccionRepository.delete(transaccion);
        espacioRepository.save(espacio);
        log.info("Transaccion ID {} removida exitosamente. Saldo del espacio ID {} actualizado a {}", id, espacio.getId(), espacio.getSaldo());
        
        // 📊 MÉTRICA: Incrementar contador de transacciones eliminadas
        Counter.builder(MetricsConfig.MetricNames.TRANSACCIONES_ELIMINADAS)
                .description("Total de transacciones eliminadas")
                .tag(MetricsConfig.TagNames.TIPO_TRANSACCION, transaccion.getTipo().name())
                .tag(MetricsConfig.TagNames.ESPACIO_TRABAJO, espacio.getId().toString())
                .register(meterRegistry)
                .increment();
    }

    /**
     * Busca transacciones aplicando filtros opcionales con soporte de paginación.
     * 
     * @param datosBusqueda Criterios de búsqueda (espacio de trabajo, año, mes, motivo, contacto, página, tamaño).
     * @return Respuesta paginada con las transacciones que cumplen los criterios.
     * @throws IllegalArgumentException si se especifica mes sin año.
     */
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<TransaccionDTOResponse> buscarTransaccion(TransaccionBusquedaDTO datosBusqueda) {

        log.info("Iniciando busqueda de transacciones para espacio ID {} con criterios: {}", datosBusqueda.idEspacioTrabajo(), datosBusqueda);

        // Valores por defecto para paginación
        int page = datosBusqueda.page() != null ? datosBusqueda.page() : 0;
        int size = datosBusqueda.size() != null ? datosBusqueda.size() : 10;
        
        // Crear el Pageable con ordenamiento por fecha descendente
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCreacion"));

        Specification<Transaccion> spec = (root, query, cb) -> cb.equal(root.get("espacioTrabajo").get("id"), datosBusqueda.idEspacioTrabajo());

        if (datosBusqueda.anio() != null) {
            int anio = datosBusqueda.anio();
            int mes = datosBusqueda.mes() != null ? datosBusqueda.mes() : 1;
            java.time.LocalDate desde = java.time.LocalDate.of(anio, mes, 1);
            java.time.LocalDate hasta;
            if (datosBusqueda.mes() != null) {
                hasta = desde.withDayOfMonth(desde.lengthOfMonth());
            } else {
                hasta = java.time.LocalDate.of(anio, 12, 31);
            }
            spec = spec.and((root, query, cb) -> cb.between(root.get("fecha"), desde, hasta));
        } else if(datosBusqueda.mes() != null){
            log.warn("Se especifico mes sin anio en la busqueda de transacciones para espacio ID {}.", datosBusqueda.idEspacioTrabajo());
            throw new IllegalArgumentException("Si no se especifica el año, no se puede especificar el mes");
        }

        if (datosBusqueda.motivo() != null && !datosBusqueda.motivo().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("motivo").get("motivo")), "%" + datosBusqueda.motivo().toLowerCase() + "%"));
        }
        if (datosBusqueda.contacto() != null && !datosBusqueda.contacto().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("contacto").get("nombre")), "%" + datosBusqueda.contacto().toLowerCase() + "%"));
        }

        var transaccionesPage = transaccionRepository.findAll(spec, pageable);
        log.info("Busqueda de transacciones para espacio ID {} finalizada. Se encontraron {} resultados en la página {} de {}.", 
            datosBusqueda.idEspacioTrabajo(), transaccionesPage.getTotalElements(), page, transaccionesPage.getTotalPages());
        
        // Convertir las transacciones a DTOs
        var transaccionesDTO = transaccionesPage.map(transaccionMapper::toResponse);
        
        return new PaginatedResponse<>(transaccionesDTO);
    }

    /**
     * Registra un nuevo contacto de transferencia.
     * 
     * @param contactoDTO Datos del contacto a registrar.
     * @return DTO de respuesta con los datos del contacto registrado.
     * @throws EntityNotFoundException si el espacio de trabajo no se encuentra.
     */
    @Override
    @Transactional
    public ContactoDTOResponse registrarContactoTransferencia(ContactoDTORequest contactoDTO) {

        log.info("Iniciando registro de contacto '{}' en espacio ID {}", contactoDTO.nombre(), contactoDTO.idEspacioTrabajo());

        // Validar que no exista un contacto con el mismo nombre en el espacio de trabajo
        Optional<ContactoTransferencia> contactoExistente = contactoRepository
                .findFirstByNombreAndEspacioTrabajo_Id(contactoDTO.nombre(), contactoDTO.idEspacioTrabajo());
        
        if (contactoExistente.isPresent()) {
            String msg = String.format("Ya existe un contacto con el nombre '%s' en este espacio de trabajo. Por favor, utiliza un nombre diferente.", 
                    contactoDTO.nombre());
            log.warn(msg);
            throw new EntidadDuplicadaException(msg);
        }

        ContactoTransferencia contacto = contactoTransferenciaMapper.toEntity(contactoDTO);

        EspacioTrabajo espacio = buscarEspacioTrabajoPorId(contactoDTO.idEspacioTrabajo());
        contacto.setEspacioTrabajo(espacio);

        ContactoTransferencia contactoGuardado = contactoRepository.save(contacto);
        log.info("Contacto '{}' (ID: {}) registrado exitosamente en espacio ID {}.", contactoGuardado.getNombre(), contactoGuardado.getId(), espacio.getId());
        return contactoTransferenciaMapper.toResponse(contactoGuardado);
    }

    /**
     * Registra un nuevo motivo de transacción.
     * 
     * @param motivoDTO Datos del motivo a registrar.
     * @return DTO de respuesta con los datos del motivo registrado.
     * @throws EntityNotFoundException si el espacio de trabajo no se encuentra.
     */
    @Override
    @Transactional
    public MotivoDTOResponse nuevoMotivoTransaccion(MotivoDTORequest motivoDTO) {

        log.info("Iniciando registro de motivo '{}' en espacio ID {}", motivoDTO.motivo(), motivoDTO.idEspacioTrabajo());

        // Validar que no exista un motivo con el mismo nombre en el espacio de trabajo
        Optional<MotivoTransaccion> motivoExistente = motivoRepository
                .findFirstByMotivoAndEspacioTrabajo_Id(motivoDTO.motivo(), motivoDTO.idEspacioTrabajo());
        
        if (motivoExistente.isPresent()) {
            String msg = String.format("Ya existe un motivo con el nombre '%s' en este espacio de trabajo. Por favor, utiliza un nombre diferente.", 
                    motivoDTO.motivo());
            log.warn(msg);
            throw new EntidadDuplicadaException(msg);
        }

        MotivoTransaccion motivo = motivoTransaccionMapper.toEntity(motivoDTO);

        EspacioTrabajo espacio = buscarEspacioTrabajoPorId(motivoDTO.idEspacioTrabajo());
        motivo.setEspacioTrabajo(espacio);

        MotivoTransaccion motivoGuardado = motivoRepository.save(motivo);
        log.info("Motivo '{}' (ID: {}) registrado exitosamente en espacio ID {}.", motivoGuardado.getMotivo(), motivoGuardado.getId(), espacio.getId());
        return motivoTransaccionMapper.toResponse(motivoGuardado);
    }

    /**
     * Lista todos los contactos de transferencia de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo.
     * @return Lista de contactos del espacio de trabajo.
     */
    @Override
    public List<ContactoDTOResponse> listarContactos(UUID idEspacioTrabajo) {

        log.info("Listando contactos para el espacio de trabajo ID: {}", idEspacioTrabajo);

        List<ContactoDTOResponse> contactos = contactoRepository.findByEspacioTrabajo_IdOrderByFechaModificacionDesc(idEspacioTrabajo).stream()
                .map(contactoTransferenciaMapper::toResponse)
                .toList();
        log.info("Se encontraron {} contactos para el espacio ID {} (ordenados por última modificación).", contactos.size(), idEspacioTrabajo);
        return contactos;
    }

    /**
     * Lista todos los motivos de transacción de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo.
     * @return Lista de motivos del espacio de trabajo.
     */
    @Override
    public List<MotivoDTOResponse> listarMotivos(UUID idEspacioTrabajo) {

        log.info("Listando motivos para el espacio de trabajo ID: {}", idEspacioTrabajo);

        List<MotivoDTOResponse> motivos = motivoRepository.findByEspacioTrabajo_IdOrderByFechaModificacionDesc(idEspacioTrabajo).stream()
                .map(motivoTransaccionMapper::toResponse)
                .toList();
        log.info("Se encontraron {} motivos para el espacio ID {} (ordenados por última modificación).", motivos.size(), idEspacioTrabajo);
        return motivos;
    }

    /**
     * Busca las últimas 6 transacciones recientes de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo.
     * @return Lista de las últimas 6 transacciones ordenadas por fecha de creación.
     */
    @Override
    public List<TransaccionDTOResponse> buscarTransaccionesRecientes(UUID idEspacioTrabajo) {

        log.info("Buscando ultimas 6 transacciones para el espacio de trabajo ID: {}", idEspacioTrabajo);

        ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");
        ZonedDateTime nowInBuenosAires = ZonedDateTime.now(buenosAiresZone);
        LocalDateTime fechaActual = nowInBuenosAires.toLocalDateTime();

        Specification<Transaccion> spec = (root, query, cb) -> cb.and(
            cb.equal(root.get("espacioTrabajo").get("id"), idEspacioTrabajo),
            cb.lessThanOrEqualTo(root.get("fechaCreacion"), fechaActual)
        );

        Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        List<Transaccion> transacciones = transaccionRepository.findAll(spec, pageable).getContent();
        log.info("Se encontraron {} transacciones recientes para el espacio ID {}.", transacciones.size(), idEspacioTrabajo);
        return transacciones.stream()
            .map(transaccionMapper::toResponse)
            .toList();
    }

    /*
    ===========================================================================
        MÉTODOS AUXILIARES PRIVADOS
    ===========================================================================
    */

    /**
     * Método auxiliar para anotar gastos e ingresos por mes.
     * Usa la fecha real de la transacción para determinar el anio/mes del registro.
     */
    private void gastosIgresosMesAnotar(TipoTransaccion tipo, BigDecimal monto, UUID idEspacioTrabajo, LocalDate fecha) {

        Integer anio = fecha.getYear();
        Integer mes = fecha.getMonthValue();

        Optional<GastosIngresosMensuales> opt = gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(idEspacioTrabajo, anio, mes);

        GastosIngresosMensuales registro = opt.orElseGet(() -> {
            EspacioTrabajo espacio = buscarEspacioTrabajoPorId(idEspacioTrabajo);
            return GastosIngresosMensuales.builder()
                    .anio(anio)
                    .mes(mes)
                    .gastos(BigDecimal.ZERO)
                    .ingresos(BigDecimal.ZERO)
                    .comprasCredito(BigDecimal.ZERO)
                    .pagoResumen(BigDecimal.ZERO)
                    .espacioTrabajo(espacio)
                    .build();
        });

        if (tipo.equals(TipoTransaccion.GASTO)) {
            registro.actualizarGastos(monto);
        } else {
            registro.actualizarIngresos(monto);
        }

        gastosIngresosMensualesRepository.save(registro);
        log.info("Gastos/Ingresos mensuales anotados: espacioId={}, anio={}, mes={}, gastos={}, ingresos={}",
                idEspacioTrabajo, anio, mes, registro.getGastos(), registro.getIngresos());
    }

    /**
     * Método auxiliar para eliminar gastos e ingresos por mes porque se eliminó una transacción.
     * Usa la fecha real de la transacción para determinar el anio/mes del registro.
     */
    private void gastosIngresosMesDelete(TipoTransaccion tipo, BigDecimal monto, UUID idEspacioTrabajo, LocalDate fecha) {

        Integer anio = fecha.getYear();
        Integer mes = fecha.getMonthValue();

        Optional<GastosIngresosMensuales> opt = gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(idEspacioTrabajo, anio, mes);

        GastosIngresosMensuales registro = opt.orElseThrow(() -> {
            String msg = "Registro de GastosIngresosMensuales no encontrado para espacioId=" + idEspacioTrabajo + ", anio=" + anio + ", mes=" + mes;
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });

        if (tipo.equals(TipoTransaccion.GASTO)) {
            if (registro.getGastos().compareTo(monto) < 0) {
                String msg = String.format("No se puede eliminar la transacción. El monto a eliminar ($%.2f) es mayor que los gastos registrados en este mes ($%.2f).", monto, registro.getGastos());
                log.warn(msg);
                throw new SaldoInsuficienteException(msg);
            }
            registro.eliminarGastos(monto);
        } else {
            if (registro.getIngresos().compareTo(monto) < 0) {
                String msg = String.format("No se puede eliminar la transacción. El monto a eliminar ($%.2f) es mayor que los ingresos registrados en este mes ($%.2f).", monto, registro.getIngresos());
                log.warn(msg);
                throw new SaldoInsuficienteException(msg);
            }
            registro.eliminarIngresos(monto);
        }

        gastosIngresosMensualesRepository.save(registro);
        log.info("Gastos/Ingresos mensuales anotados: espacioId={}, anio={}, mes={}, gastos={}, ingresos={}",
                idEspacioTrabajo, anio, mes, registro.getGastos(), registro.getIngresos());
    }

    private EspacioTrabajo buscarEspacioTrabajoPorId(UUID idEspacioTrabajo) {
        return espacioRepository.findById(idEspacioTrabajo).orElseThrow(() -> {
            String msg = "Espacio de trabajo con ID " + idEspacioTrabajo + " no encontrado";
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });
    }

    private MotivoTransaccion buscarMotivoPorId(Long idMotivo) {
        return motivoRepository.findById(idMotivo).orElseThrow(() -> {
            String msg = "Motivo de transaccion con ID " + idMotivo + " no encontrado";
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });
    }

    private ContactoTransferencia buscarContactoPorId(Long idContacto) {
        return contactoRepository.findById(idContacto).orElseThrow(() -> {
            String msg = "Contacto de transferencia con ID " + idContacto + " no encontrado";
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });
    }

    private Transaccion buscarTransaccionPorId(Long idTransaccion) {
        return transaccionRepository.findById(idTransaccion).orElseThrow(() -> {
            String msg = "Transaccion con ID " + idTransaccion + " no encontrada";
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });
    }
}








