package com.campito.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campito.backend.dao.CuentaBancariaRepository;
import com.campito.backend.dao.DescuentoRepository;
import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dto.CuentaBancariaDTORequest;
import com.campito.backend.dto.CuentaBancariaDTOResponse;
import com.campito.backend.dto.DescuentoDTORequest;
import com.campito.backend.dto.DescuentoDTOResponse;
import com.campito.backend.mapper.CuentaBancariaMapper;
import com.campito.backend.mapper.DescuentoMapper;
import com.campito.backend.model.CuentaBancaria;
import com.campito.backend.model.Descuento;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.TipoTransaccion;

import com.campito.backend.exception.EntidadDuplicadaException;
import com.campito.backend.exception.SaldoInsuficienteException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio para gestión de cuentas bancarias.
 * 
 * Proporciona métodos para crear cuentas bancarias, actualizar saldos,
 * listar cuentas y realizar transacciones entre cuentas.
 */
@Service
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
public class CuentaBancariaServiceImpl implements CuentaBancariaService {

    private static final Logger logger = LoggerFactory.getLogger(CuentaBancariaServiceImpl.class);

    private final CuentaBancariaRepository cuentaBancariaRepository;
    private final EspacioTrabajoRepository espacioTrabajoRepository;
    private final CuentaBancariaMapper cuentaBancariaMapper;
    private final DescuentoRepository descuentoRepository;
    private final DescuentoMapper descuentoMapper;

    /**
     * Crea una nueva cuenta bancaria.
     * 
     * @param cuentaBancariaDTO Datos de la cuenta bancaria a crear.
     * @throws IllegalArgumentException si la cuenta bancaria es nula.
     * @throws EntityNotFoundException si el espacio de trabajo no se encuentra.
     */
    @Override
    @Transactional
    public void crearCuentaBancaria(CuentaBancariaDTORequest cuentaBancariaDTO) {

        if (cuentaBancariaDTO == null) {
            logger.warn("Intento de crear una CuentaBancariaDTO nula.");
            throw new IllegalArgumentException("La cuenta bancaria no puede ser nula");
        }
        logger.info("Creando cuenta bancaria '{}' para entidad '{}'", cuentaBancariaDTO.nombre(), cuentaBancariaDTO.entidadFinanciera());
        // Validar que no exista una cuenta con el mismo nombre en el espacio de trabajo
        Optional<CuentaBancaria> cuentaExistente = cuentaBancariaRepository
                .findFirstByNombreAndEspacioTrabajo_Id(cuentaBancariaDTO.nombre(), cuentaBancariaDTO.idEspacioTrabajo());
        
        if (cuentaExistente.isPresent()) {
            String msg = String.format("Ya existe una cuenta bancaria con el nombre '%s' en este espacio de trabajo. Por favor, utiliza un nombre diferente.", 
                    cuentaBancariaDTO.nombre());
            logger.warn(msg);
            throw new EntidadDuplicadaException(msg);
        }
        EspacioTrabajo espacioTrabajo = espacioTrabajoRepository.findById(cuentaBancariaDTO.idEspacioTrabajo())
            .orElseThrow(() -> {
                String mensaje = "Espacio de trabajo con ID " + cuentaBancariaDTO.idEspacioTrabajo() + " no encontrado";
                logger.warn(mensaje);
                return new EntityNotFoundException(mensaje);
            });

        CuentaBancaria cuentaBancaria = cuentaBancariaMapper.toEntity(cuentaBancariaDTO);

        cuentaBancaria.setEspacioTrabajo(espacioTrabajo);
        cuentaBancariaRepository.save(cuentaBancaria);
        logger.info("Cuenta bancaria '{}' creada exitosamente.", cuentaBancaria.getNombre());
    }

    /**
     * Actualiza el saldo de una cuenta bancaria según el tipo de transacción.
     * 
     * @param id ID de la cuenta bancaria a actualizar.
     * @param tipo Tipo de transacción (INGRESO o GASTO).
     * @param monto Monto de la transacción.
     * @return Entidad CuentaBancaria actualizada.
     * @throws IllegalArgumentException si el ID o monto son nulos.
     * @throws EntityNotFoundException si la cuenta bancaria no se encuentra.
     * @throws SaldoInsuficienteException si el saldo de la cuenta es insuficiente para realizar un gasto.
     */
    @Override
    @Transactional
    public CuentaBancaria actualizarCuentaBancaria(Long id, TipoTransaccion tipo, BigDecimal monto) {

        if (id == null || monto == null || tipo == null) {
            logger.warn("Intento de actualizar cuenta bancaria con parametros nulos.");
            throw new IllegalArgumentException("El ID, el tipo y el monto no pueden ser nulos");
        }
        logger.info("Actualizando saldo de cuenta bancaria ID: {} a monto: {}", id, monto);
        
        CuentaBancaria cuenta = cuentaBancariaRepository.findById(id)
            .orElseThrow(() -> {
                String mensaje = "Cuenta bancaria con ID " + id + " no encontrada";
                logger.warn(mensaje);
                return new EntityNotFoundException(mensaje);
            });

        if (tipo.equals(TipoTransaccion.GASTO) && cuenta.getSaldoActual().compareTo(monto) < 0) {
            logger.warn("Saldo insuficiente en la cuenta bancaria ID: {} para realizar la actualización de monto: {}", id, monto);
            throw new SaldoInsuficienteException(
                String.format("Saldo insuficiente en la cuenta '%s'. Saldo actual: $%.2f, Monto requerido: $%.2f", 
                    cuenta.getNombre(), cuenta.getSaldoActual(), monto));
        }

        cuenta.actualizarSaldoNuevaTransaccion(monto, tipo);

        cuentaBancariaRepository.save(cuenta);
        logger.info("Saldo de cuenta bancaria ID: {} actualizado a {}.", id, cuenta.getSaldoActual());
        return cuenta;
    }

    /**
     * Lista todas las cuentas bancarias de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo.
     * @return Lista de cuentas bancarias del espacio de trabajo.
     * @throws IllegalArgumentException si el ID del espacio es nulo.
     */
    @Override
    public List<CuentaBancariaDTOResponse> listarCuentasBancarias(UUID idEspacioTrabajo) {

        if (idEspacioTrabajo == null) {
            logger.warn("Intento de listar cuentas bancarias con idEspacioTrabajo nulo.");
            throw new IllegalArgumentException("El id del espacio de trabajo no puede ser nulo");
        }
        logger.info("Listando cuentas bancarias para el espacio de trabajo ID: {}", idEspacioTrabajo);

        List<CuentaBancariaDTOResponse> cuentas = cuentaBancariaRepository.findByEspacioTrabajo_IdOrderByFechaModificacionDesc(idEspacioTrabajo).stream()
            .map(cuentaBancariaMapper::toResponse)
            .toList();
        logger.info("Encontradas {} cuentas bancarias para el espacio de trabajo ID: {} (ordenadas por última modificación).", cuentas.size(), idEspacioTrabajo);
        return cuentas;
    }

    /**
     * Realiza una transacción de transferencia entre dos cuentas bancarias.
     * 
     * @param idCuentaOrigen ID de la cuenta bancaria origen.
     * @param idCuentaDestino ID de la cuenta bancaria destino.
     * @param monto Monto a transferir.
     * @throws IllegalArgumentException si algún parámetro es nulo.
     * @throws EntityNotFoundException si alguna de las cuentas no se encuentra.
     * @throws SaldoInsuficienteException si el saldo de la cuenta origen es insuficiente.
     */
    @Override
    public void transaccionEntreCuentas(Long idCuentaOrigen, Long idCuentaDestino, BigDecimal monto) {
        
        if(idCuentaOrigen == null || idCuentaDestino == null || monto == null) {
            logger.warn("Intento de realizar transacción entre cuentas con parámetros nulos. Origen: {}, Destino: {}, Monto: {}", idCuentaOrigen, idCuentaDestino, monto);
            throw new IllegalArgumentException("Los IDs de las cuentas y el monto no pueden ser nulos");
        }

        CuentaBancaria cuentaOrigen = cuentaBancariaRepository.findById(idCuentaOrigen)
            .orElseThrow(() -> {
                String mensaje = "Cuenta bancaria origen con ID " + idCuentaOrigen + " no encontrada";
                logger.warn(mensaje);
                return new EntityNotFoundException(mensaje);
            });

        CuentaBancaria cuentaDestino = cuentaBancariaRepository.findById(idCuentaDestino)
            .orElseThrow(() -> {
                String mensaje = "Cuenta bancaria destino con ID " + idCuentaDestino + " no encontrada";
                logger.warn(mensaje);
                return new EntityNotFoundException(mensaje);
            });

        if(cuentaOrigen.getSaldoActual().compareTo(monto) < 0) {
            logger.warn("Saldo insuficiente en la cuenta origen ID: {} para realizar la transacción de monto: {}", idCuentaOrigen, monto);
            throw new SaldoInsuficienteException(
                String.format("Saldo insuficiente en la cuenta origen '%s'. Saldo actual: $%.2f, Monto requerido: $%.2f", 
                    cuentaOrigen.getNombre(), cuentaOrigen.getSaldoActual(), monto));
        }

        cuentaOrigen.setSaldoActual(cuentaOrigen.getSaldoActual().subtract(monto));
        cuentaDestino.setSaldoActual(cuentaDestino.getSaldoActual().add(monto));

        cuentaBancariaRepository.save(cuentaOrigen);
        cuentaBancariaRepository.save(cuentaDestino);

        logger.info("Transacción de {} realizada exitosamente entre cuentas ID: {} y ID: {}.", monto, idCuentaOrigen, idCuentaDestino);
    }

    // =========================================================
    // Operaciones de Descuentos
    // =========================================================

    /**
     * Crea un nuevo descuento para el espacio de trabajo indicado.
     *
     * @param dto Datos del descuento a crear.
     * @throws EntityNotFoundException si el espacio de trabajo no existe.
     */
    @Override
    @Transactional
    public DescuentoDTOResponse crearDescuento(DescuentoDTORequest dto) {
        logger.info("Creando descuento '{}' para banco '{}' en espacio de trabajo ID: {}", dto.comercio(), dto.banco(), dto.idEspacioTrabajo());

        EspacioTrabajo espacioTrabajo = espacioTrabajoRepository.findById(dto.idEspacioTrabajo())
            .orElseThrow(() -> {
                String mensaje = "Espacio de trabajo con ID " + dto.idEspacioTrabajo() + " no encontrado";
                logger.warn(mensaje);
                return new EntityNotFoundException(mensaje);
            });

        Descuento descuento = descuentoMapper.toEntity(dto);
        descuento.setEspacioTrabajo(espacioTrabajo);
        Descuento descuentoGuardado = descuentoRepository.save(descuento);
        logger.info("Descuento '{}' creado exitosamente.", dto.comercio());
        return descuentoMapper.toResponse(descuentoGuardado);
    }

    /**
     * Lista todos los descuentos de un espacio de trabajo.
     *
     * @param idEspacioTrabajo UUID del espacio de trabajo.
     * @return Lista de descuentos del espacio de trabajo.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DescuentoDTOResponse> listarDescuentos(UUID idEspacioTrabajo) {
        logger.info("Listando descuentos para el espacio de trabajo ID: {}", idEspacioTrabajo);

        List<DescuentoDTOResponse> descuentos = descuentoRepository
            .findByEspacioTrabajo_IdOrderByDiaAsc(idEspacioTrabajo)
            .stream()
            .map(descuentoMapper::toResponse)
            .toList();

        logger.info("Encontrados {} descuentos para el espacio de trabajo ID: {}", descuentos.size(), idEspacioTrabajo);
        return descuentos;
    }

    /**
     * Elimina un descuento por su ID.
     *
     * @param id ID del descuento a eliminar.
     * @throws EntityNotFoundException si el descuento no existe.
     */
    @Override
    @Transactional
    public void eliminarDescuento(Long id) {
        logger.info("Eliminando descuento ID: {}", id);

        if (!descuentoRepository.existsById(id)) {
            String mensaje = "Descuento con ID " + id + " no encontrado";
            logger.warn(mensaje);
            throw new EntityNotFoundException(mensaje);
        }

        descuentoRepository.deleteById(id);
        logger.info("Descuento ID: {} eliminado exitosamente.", id);
    }

}
