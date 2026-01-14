package com.campito.backend.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.campito.backend.dao.ContactoTransferenciaRepository;
import com.campito.backend.dao.CuentaBancariaRepository;
import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dao.GastosIngresosMensualesRepository;
import com.campito.backend.dao.MotivoTransaccionRepository;
import com.campito.backend.dao.TransaccionRepository;
import com.campito.backend.dto.ContactoDTORequest;
import com.campito.backend.dto.ContactoDTOResponse;
import com.campito.backend.dto.MotivoDTORequest;
import com.campito.backend.dto.MotivoDTOResponse;
import com.campito.backend.dto.TransaccionBusquedaDTO;
import com.campito.backend.dto.TransaccionDTORequest;
import com.campito.backend.dto.TransaccionDTOResponse;
import com.campito.backend.mapper.ContactoTransferenciaMapper;
import com.campito.backend.mapper.MotivoTransaccionMapper;
import com.campito.backend.mapper.TransaccionMapper;
import com.campito.backend.model.ContactoTransferencia;
import com.campito.backend.model.CuentaBancaria;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.GastosIngresosMensuales;
import com.campito.backend.model.MotivoTransaccion;
import com.campito.backend.model.TipoTransaccion;
import com.campito.backend.model.Transaccion;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio para gestión de transacciones.
 * 
 * Proporciona métodos para registrar transacciones, removerlas, buscarlas,
 * y gestionar contactos y motivos de transacciones.
 */
@Service
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
public class TransaccionServiceImpl implements TransaccionService {

    private static final Logger logger = LoggerFactory.getLogger(TransaccionServiceImpl.class);

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

    /**
     * Registra una nueva transacción.
     * 
     * @param transaccionDTO Datos de la transacción a registrar.
     * @return DTO de respuesta con los datos de la transacción registrada.
     * @throws IllegalArgumentException si la transacción es nula.
     * @throws EntityNotFoundException si el espacio de trabajo, motivo o contacto no se encuentran.
     */
    @Override
    @Transactional
    public TransaccionDTOResponse registrarTransaccion(TransaccionDTORequest transaccionDTO) {

        if (transaccionDTO == null) {
            logger.warn("Intento de registrar una TransaccionDTO nula.");
            throw new IllegalArgumentException("La transaccion no puede ser nula");
        }
        logger.info("Iniciando registro de transaccion tipo {} por monto {} en espacio ID {}", transaccionDTO.tipo(), transaccionDTO.monto(), transaccionDTO.idEspacioTrabajo());

        EspacioTrabajo espacio = espacioRepository.findById(transaccionDTO.idEspacioTrabajo()).orElseThrow(() -> {
            String msg = "Espacio de trabajo con ID " + transaccionDTO.idEspacioTrabajo() + " no encontrado";
            logger.warn(msg);
            return new EntityNotFoundException(msg);
        });
        MotivoTransaccion motivo = motivoRepository.findById(transaccionDTO.idMotivo()).orElseThrow(() -> {
            String msg = "Motivo de transaccion con ID " + transaccionDTO.idMotivo() + " no encontrado";
            logger.warn(msg);
            return new EntityNotFoundException(msg);
        });

        Transaccion transaccion = transaccionMapper.toEntity(transaccionDTO);

        gastosIgresosMesAnotar(transaccion.getTipo(), transaccion.getMonto(), espacio.getId());

        if (transaccionDTO.idContacto() != null) {
            ContactoTransferencia contacto = contactoRepository.findById(transaccionDTO.idContacto()).orElseThrow(() -> {
                String msg = "Contacto de transferencia con ID " + transaccionDTO.idContacto() + " no encontrado";
                logger.warn(msg);
                return new EntityNotFoundException(msg);
            });
            transaccion.setContacto(contacto);
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

        transaccion.setEspacioTrabajo(espacio);
        transaccion.setMotivo(motivo);

        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);
        logger.info("Transaccion ID {} registrada exitosamente en espacio ID {}. Nuevo saldo: {}", transaccionGuardada.getId(), espacio.getId(), espacio.getSaldo());
        
        return transaccionMapper.toResponse(transaccionGuardada);

    }

    /**
     * Remueve una transacción existente y revierte el impacto en el saldo.
     * 
     * @param id ID de la transacción a remover.
     * @throws IllegalArgumentException si el ID es nulo.
     * @throws EntityNotFoundException si la transacción no se encuentra.
     */
    @Override
    @Transactional
    public void removerTransaccion(Long id) {

        if (id == null) {
            logger.warn("Intento de remover transaccion con ID nulo.");
            throw new IllegalArgumentException("El ID de la transacción no puede ser nulo");
        }
        logger.info("Iniciando remocion de transaccion ID: {}", id);

        Transaccion transaccion = transaccionRepository.findById(id)
            .orElseThrow(() -> {
                String msg = "Transaccion con ID " + id + " no encontrada";
                logger.warn(msg);
                return new EntityNotFoundException(msg);
            });

        EspacioTrabajo espacio = transaccion.getEspacioTrabajo();
        espacio.actualizarSaldoEliminarTransaccion(transaccion.getMonto(), transaccion.getTipo());

        if(transaccion.getCuentaBancaria() != null) {
            CuentaBancaria cuenta = transaccion.getCuentaBancaria();
            
            cuenta.actualizarSaldoEliminarTransaccion(transaccion.getMonto(), transaccion.getTipo());
            cuentaBancariaRepository.save(cuenta);
            logger.info("Saldo de cuenta bancaria ID {} actualizado a {} tras remocion de transaccion ID {}", cuenta.getId(), cuenta.getSaldoActual(), id);
        }

        gastosIngresosMesDelete(transaccion.getTipo(), transaccion.getMonto(), espacio.getId());

        transaccionRepository.delete(transaccion);
        espacioRepository.save(espacio);
        logger.info("Transaccion ID {} removida exitosamente. Saldo del espacio ID {} actualizado a {}", id, espacio.getId(), espacio.getSaldo());
    }

    /**
     * Busca transacciones aplicando filtros opcionales.
     * 
     * @param datosBusqueda Criterios de búsqueda (espacio de trabajo, año, mes, motivo, contacto).
     * @return Lista de transacciones que cumplen los criterios.
     * @throws IllegalArgumentException si los datos de búsqueda son nulos.
     */
    @Override
    public List<TransaccionDTOResponse> buscarTransaccion(TransaccionBusquedaDTO datosBusqueda) {

        if (datosBusqueda == null) {
            logger.warn("Intento de buscar transacciones con DTO de busqueda nulo.");
            throw new IllegalArgumentException("Los datos de búsqueda no pueden ser nulos");
        }
        logger.info("Iniciando busqueda de transacciones para espacio ID {} con criterios: {}", datosBusqueda.idEspacioTrabajo(), datosBusqueda);

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
            logger.warn("Se especifico mes sin anio en la busqueda de transacciones para espacio ID {}.", datosBusqueda.idEspacioTrabajo());
            throw new IllegalArgumentException("Si no se especifica el año, no se puede especificar el mes");
        }

        if (datosBusqueda.motivo() != null && !datosBusqueda.motivo().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("motivo").get("motivo")), "%" + datosBusqueda.motivo().toLowerCase() + "%"));
        }
        if (datosBusqueda.contacto() != null && !datosBusqueda.contacto().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("contacto").get("nombre")), "%" + datosBusqueda.contacto().toLowerCase() + "%"));
        }

        List<Transaccion> transacciones = transaccionRepository.findAll(spec);
        logger.info("Busqueda de transacciones para espacio ID {} finalizada. Se encontraron {} resultados.", datosBusqueda.idEspacioTrabajo(), transacciones.size());
        return transacciones.stream()
            .map(transaccionMapper::toResponse)
            .toList();
    }

    /**
     * Registra un nuevo contacto de transferencia.
     * 
     * @param contactoDTO Datos del contacto a registrar.
     * @return DTO de respuesta con los datos del contacto registrado.
     * @throws IllegalArgumentException si el contacto es nulo.
     * @throws EntityNotFoundException si el espacio de trabajo no se encuentra.
     */
    @Override
    @Transactional
    public ContactoDTOResponse registrarContactoTransferencia(ContactoDTORequest contactoDTO) {

        if (contactoDTO == null) {
            logger.warn("Intento de registrar un ContactoDTO nulo.");
            throw new IllegalArgumentException("El contacto no puede ser nulo");
        }
        logger.info("Iniciando registro de contacto '{}' en espacio ID {}", contactoDTO.nombre(), contactoDTO.idEspacioTrabajo());

        ContactoTransferencia contacto = contactoTransferenciaMapper.toEntity(contactoDTO);

        EspacioTrabajo espacio = espacioRepository.findById(contactoDTO.idEspacioTrabajo()).orElseThrow(() -> {
            String msg = "Espacio de trabajo con ID " + contactoDTO.idEspacioTrabajo() + " no encontrado";
            logger.warn(msg);
            return new EntityNotFoundException(msg);
        });
        contacto.setEspacioTrabajo(espacio);

        ContactoTransferencia contactoGuardado = contactoRepository.save(contacto);
        logger.info("Contacto '{}' (ID: {}) registrado exitosamente en espacio ID {}.", contactoGuardado.getNombre(), contactoGuardado.getId(), espacio.getId());
        return contactoTransferenciaMapper.toResponse(contactoGuardado);
    }

    /**
     * Registra un nuevo motivo de transacción.
     * 
     * @param motivoDTO Datos del motivo a registrar.
     * @return DTO de respuesta con los datos del motivo registrado.
     * @throws IllegalArgumentException si el motivo es nulo.
     * @throws EntityNotFoundException si el espacio de trabajo no se encuentra.
     */
    @Override
    @Transactional
    public MotivoDTOResponse nuevoMotivoTransaccion(MotivoDTORequest motivoDTO) {

        if (motivoDTO == null) {
            logger.warn("Intento de registrar un MotivoDTO nulo.");
            throw new IllegalArgumentException("El motivo no puede ser nulo");
        }
        logger.info("Iniciando registro de motivo '{}' en espacio ID {}", motivoDTO.motivo(), motivoDTO.idEspacioTrabajo());

        MotivoTransaccion motivo = motivoTransaccionMapper.toEntity(motivoDTO);

        EspacioTrabajo espacio = espacioRepository.findById(motivoDTO.idEspacioTrabajo()).orElseThrow(() -> {
            String msg = "Espacio de trabajo con ID " + motivoDTO.idEspacioTrabajo() + " no encontrado";
            logger.warn(msg);
            return new EntityNotFoundException(msg);
        });
        motivo.setEspacioTrabajo(espacio);

        MotivoTransaccion motivoGuardado = motivoRepository.save(motivo);
        logger.info("Motivo '{}' (ID: {}) registrado exitosamente en espacio ID {}.", motivoGuardado.getMotivo(), motivoGuardado.getId(), espacio.getId());
        return motivoTransaccionMapper.toResponse(motivoGuardado);
    }

    /**
     * Lista todos los contactos de transferencia de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo.
     * @return Lista de contactos del espacio de trabajo.
     * @throws IllegalArgumentException si el ID del espacio es nulo.
     */
    @Override
    public List<ContactoDTOResponse> listarContactos(Long idEspacioTrabajo) {

        if (idEspacioTrabajo == null) {
            logger.warn("Intento de listar contactos con ID de espacio de trabajo nulo.");
            throw new IllegalArgumentException("El id del espacio de trabajo no puede ser nulo");
        }
        logger.info("Listando contactos para el espacio de trabajo ID: {}", idEspacioTrabajo);

        List<ContactoDTOResponse> contactos = contactoRepository.findByEspacioTrabajo_Id(idEspacioTrabajo).stream()
                .map(contactoTransferenciaMapper::toResponse)
                .toList();
        logger.info("Se encontraron {} contactos para el espacio ID {}.", contactos.size(), idEspacioTrabajo);
        return contactos;
    }

    /**
     * Lista todos los motivos de transacción de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo.
     * @return Lista de motivos del espacio de trabajo.
     * @throws IllegalArgumentException si el ID del espacio es nulo.
     */
    @Override
    public List<MotivoDTOResponse> listarMotivos(Long idEspacioTrabajo) {

        if (idEspacioTrabajo == null) {
            logger.warn("Intento de listar motivos con ID de espacio de trabajo nulo.");
            throw new IllegalArgumentException("El id del espacio de trabajo no puede ser nulo");
        }
        logger.info("Listando motivos para el espacio de trabajo ID: {}", idEspacioTrabajo);

        List<MotivoDTOResponse> motivos = motivoRepository.findByEspacioTrabajo_Id(idEspacioTrabajo).stream()
                .map(motivoTransaccionMapper::toResponse)
                .toList();
        logger.info("Se encontraron {} motivos para el espacio ID {}.", motivos.size(), idEspacioTrabajo);
        return motivos;
    }

    /**
     * Busca las últimas 6 transacciones recientes de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo.
     * @return Lista de las últimas 6 transacciones ordenadas por fecha de creación.
     * @throws IllegalArgumentException si el ID del espacio es nulo.
     */
    @Override
    public List<TransaccionDTOResponse> buscarTransaccionesRecientes(Long idEspacioTrabajo) {

        if (idEspacioTrabajo == null) {
            logger.warn("Intento de buscar transacciones recientes con ID de espacio de trabajo nulo.");
            throw new IllegalArgumentException("El id del espacio de trabajo no puede ser nulo");
        }
        logger.info("Buscando ultimas 6 transacciones para el espacio de trabajo ID: {}", idEspacioTrabajo);

        ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");
        ZonedDateTime nowInBuenosAires = ZonedDateTime.now(buenosAiresZone);
        LocalDateTime fechaActual = nowInBuenosAires.toLocalDateTime();

        Specification<Transaccion> spec = (root, query, cb) -> cb.and(
            cb.equal(root.get("espacioTrabajo").get("id"), idEspacioTrabajo),
            cb.lessThanOrEqualTo(root.get("fechaCreacion"), fechaActual)
        );

        Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        List<Transaccion> transacciones = transaccionRepository.findAll(spec, pageable).getContent();
        logger.info("Se encontraron {} transacciones recientes para el espacio ID {}.", transacciones.size(), idEspacioTrabajo);
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
     * Método auxiliar para anotar gastos e ingresos por mes
     */
    @Transactional
    private void gastosIgresosMesAnotar(TipoTransaccion tipo, Float monto, Long idEspacioTrabajo) {

        if (tipo == null || monto == null || idEspacioTrabajo == null) {
            logger.warn("Argumentos inválidos para anotar gastos/ingresos: tipo={}, monto={}, espacioId={}", tipo, monto, idEspacioTrabajo);
            throw new IllegalArgumentException("Tipo, monto y idEspacioTrabajo no pueden ser nulos");
        }

        ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");
        ZonedDateTime nowInBuenosAires = ZonedDateTime.now(buenosAiresZone);
        Integer anio = nowInBuenosAires.getYear();
        Integer mes = nowInBuenosAires.getMonthValue();

        Optional<GastosIngresosMensuales> opt = gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(idEspacioTrabajo, anio, mes);

        GastosIngresosMensuales registro = opt.orElseGet(() -> {
            EspacioTrabajo espacio = espacioRepository.findById(idEspacioTrabajo).orElseThrow(() -> {
                String msg = "Espacio de trabajo con ID " + idEspacioTrabajo + " no encontrado";
                logger.warn(msg);
                return new EntityNotFoundException(msg);
            });
            return GastosIngresosMensuales.builder()
                    .anio(anio)
                    .mes(mes)
                    .gastos(0f)
                    .ingresos(0f)
                    .espacioTrabajo(espacio)
                    .build();
        });

        if (tipo.equals(TipoTransaccion.GASTO)) {
            registro.actualizarGastos(monto);
        } else {
            registro.actualizarIngresos(monto);
        }

        gastosIngresosMensualesRepository.save(registro);
        logger.info("Gastos/Ingresos mensuales anotados: espacioId={}, anio={}, mes={}, gastos={}, ingresos={}",
                idEspacioTrabajo, anio, mes, registro.getGastos(), registro.getIngresos());
    }

    /**
     * Método auxiliar para eliminar gastos e ingresos por mes porque se eliminó una transacción
     */
    @Transactional
    private void gastosIngresosMesDelete(TipoTransaccion tipo, Float monto, Long idEspacioTrabajo) {
        
        if (tipo == null || monto == null || idEspacioTrabajo == null) {
            logger.warn("Argumentos inválidos para anotar gastos/ingresos: tipo={}, monto={}, espacioId={}", tipo, monto, idEspacioTrabajo);
            throw new IllegalArgumentException("Tipo, monto y idEspacioTrabajo no pueden ser nulos");
        }

        ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");
        ZonedDateTime nowInBuenosAires = ZonedDateTime.now(buenosAiresZone);
        Integer anio = nowInBuenosAires.getYear();
        Integer mes = nowInBuenosAires.getMonthValue();

        Optional<GastosIngresosMensuales> opt = gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(idEspacioTrabajo, anio, mes);

        GastosIngresosMensuales registro = opt.orElseThrow(() -> {
            String msg = "Registro de GastosIngresosMensuales no encontrado para espacioId=" + idEspacioTrabajo + ", anio=" + anio + ", mes=" + mes;
            logger.warn(msg);
            return new EntityNotFoundException(msg);
        });

        if (tipo.equals(TipoTransaccion.GASTO)) {
            if (registro.getGastos() < monto) {
                String msg = "No se pueden eliminar gastos mensuales: el monto a eliminar es mayor que los gastos registrados.";
                logger.warn(msg);
                throw new IllegalArgumentException(msg);
            }
            registro.eliminarGastos(monto);
        } else {
            if (registro.getIngresos() < monto) {
                String msg = "No se pueden eliminar ingresos mensuales: el monto a eliminar es mayor que los ingresos registrados.";
                logger.warn(msg);
                throw new IllegalArgumentException(msg);
            }
            registro.eliminarIngresos(monto);
        }

        gastosIngresosMensualesRepository.save(registro);
        logger.info("Gastos/Ingresos mensuales anotados: espacioId={}, anio={}, mes={}, gastos={}, ingresos={}",
                idEspacioTrabajo, anio, mes, registro.getGastos(), registro.getIngresos());
    }
}
