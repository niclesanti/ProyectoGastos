package com.campito.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campito.backend.dao.CuentaBancariaRepository;
import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dto.CuentaBancariaDTORequest;
import com.campito.backend.dto.CuentaBancariaDTOResponse;
import com.campito.backend.mapper.CuentaBancariaMapper;
import com.campito.backend.model.CuentaBancaria;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.TipoTransaccion;

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

    /**
     * Crea una nueva cuenta bancaria.
     * 
     * @param cuentaBancariaDTO Datos de la cuenta bancaria a crear.
     * @throws IllegalArgumentException si la cuenta bancaria es nula.
     * @throws EntityNotFoundException si el espacio de trabajo no se encuentra.
     * @throws Exception para cualquier otro error inesperado.
     */
    @Override
    @Transactional
    public void crearCuentaBancaria(CuentaBancariaDTORequest cuentaBancariaDTO) {

        if (cuentaBancariaDTO == null) {
            logger.warn("Intento de crear una CuentaBancariaDTO nula.");
            throw new IllegalArgumentException("La cuenta bancaria no puede ser nula");
        }
        logger.info("Creando cuenta bancaria '{}' para entidad '{}'", cuentaBancariaDTO.nombre(), cuentaBancariaDTO.entidadFinanciera());
        try {

            EspacioTrabajo espacioTrabajo = espacioTrabajoRepository.findById(cuentaBancariaDTO.idEspacioTrabajo())
                .orElseThrow(() -> {
                    String mensaje = "Espacio de trabajo con ID " + cuentaBancariaDTO.idEspacioTrabajo() + " no encontrado";
                    logger.warn(mensaje);
                    return new EntityNotFoundException(mensaje);
                });

            CuentaBancaria cuentaBancaria = cuentaBancariaMapper.toEntity(cuentaBancariaDTO);
            cuentaBancaria.setSaldoActual(0f);
            cuentaBancaria.setEspacioTrabajo(espacioTrabajo);
            cuentaBancariaRepository.save(cuentaBancaria);
            logger.info("Cuenta bancaria '{}' creada exitosamente.", cuentaBancaria.getNombre());
        } catch (Exception e) {
            logger.error("Error inesperado al crear cuenta bancaria '{}'.", cuentaBancariaDTO.nombre(), e);
            throw e;
        }
    }

    /**
     * Actualiza el saldo de una cuenta bancaria según el tipo de transacción.
     * 
     * @param id ID de la cuenta bancaria a actualizar.
     * @param tipo Tipo de transacción (INGRESO o GASTO).
     * @param monto Monto de la transacción.
     * @return Entidad CuentaBancaria actualizada.
     * @throws IllegalArgumentException si el ID o monto son nulos, o si el saldo es insuficiente.
     * @throws EntityNotFoundException si la cuenta bancaria no se encuentra.
     * @throws Exception para cualquier otro error inesperado.
     */
    @Override
    @Transactional
    public CuentaBancaria actualizarCuentaBancaria(Long id, TipoTransaccion tipo, Float monto) {

        if (id == null || monto == null || tipo == null) {
            logger.warn("Intento de actualizar cuenta bancaria con parametros nulos.");
            throw new IllegalArgumentException("El ID, el tipo y el monto no pueden ser nulos");
        }
        logger.info("Actualizando saldo de cuenta bancaria ID: {} a monto: {}", id, monto);
        
        try {
            CuentaBancaria cuenta = cuentaBancariaRepository.findById(id)
                .orElseThrow(() -> {
                    String mensaje = "Cuenta bancaria con ID " + id + " no encontrada";
                    logger.warn(mensaje);
                    return new EntityNotFoundException(mensaje);
                });

            if (tipo.equals(TipoTransaccion.GASTO) && cuenta.getSaldoActual() < monto) {
                logger.warn("Saldo insuficiente en la cuenta bancaria ID: {} para realizar la actualización de monto: {}", id, monto);
                throw new IllegalArgumentException("Saldo insuficiente en la cuenta bancaria");
                
            }

            cuenta.actualizarSaldoNuevaTransaccion(monto, tipo);

            cuentaBancariaRepository.save(cuenta);
            logger.info("Saldo de cuenta bancaria ID: {} actualizado a {}.", id, cuenta.getSaldoActual());
            return cuenta;
        } catch (Exception e) {
            logger.error("Error al actualizar cuenta bancaria ID: {}. Causa: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lista todas las cuentas bancarias de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo.
     * @return Lista de cuentas bancarias del espacio de trabajo.
     * @throws IllegalArgumentException si el ID del espacio es nulo.
     * @throws Exception para cualquier otro error inesperado.
     */
    @Override
    public List<CuentaBancariaDTOResponse> listarCuentasBancarias(Long idEspacioTrabajo) {

        if (idEspacioTrabajo == null) {
            logger.warn("Intento de listar cuentas bancarias con idEspacioTrabajo nulo.");
            throw new IllegalArgumentException("El id del espacio de trabajo no puede ser nulo");
        }
        logger.info("Listando cuentas bancarias para el espacio de trabajo ID: {}", idEspacioTrabajo);

        try {
            List<CuentaBancariaDTOResponse> cuentas = cuentaBancariaRepository.findByEspacioTrabajo_Id(idEspacioTrabajo).stream()
                .map(cuentaBancariaMapper::toResponse)
                .toList();
            logger.info("Encontradas {} cuentas bancarias para el espacio de trabajo ID: {}.", cuentas.size(), idEspacioTrabajo);
            return cuentas;
        } catch (Exception e) {
            logger.error("Error al listar cuentas bancarias para el espacio de trabajo ID: {}. Causa: {}", idEspacioTrabajo, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Realiza una transacción de transferencia entre dos cuentas bancarias.
     * 
     * @param idCuentaOrigen ID de la cuenta bancaria origen.
     * @param idCuentaDestino ID de la cuenta bancaria destino.
     * @param monto Monto a transferir.
     * @throws IllegalArgumentException si algún parámetro es nulo o si el saldo origen es insuficiente.
     * @throws EntityNotFoundException si alguna de las cuentas no se encuentra.
     * @throws Exception para cualquier otro error inesperado.
     */
    @Override
    public void transaccionEntreCuentas(Long idCuentaOrigen, Long idCuentaDestino, Float monto) {
        
        if(idCuentaOrigen == null || idCuentaDestino == null || monto == null) {
            logger.warn("Intento de realizar transacción entre cuentas con parámetros nulos. Origen: {}, Destino: {}, Monto: {}", idCuentaOrigen, idCuentaDestino, monto);
            throw new IllegalArgumentException("Los IDs de las cuentas y el monto no pueden ser nulos");
        }

        try {
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

            if(cuentaOrigen.getSaldoActual() < monto) {
                logger.warn("Saldo insuficiente en la cuenta origen ID: {} para realizar la transacción de monto: {}", idCuentaOrigen, monto);
                throw new IllegalArgumentException("Saldo insuficiente en la cuenta origen");
            }

            cuentaOrigen.setSaldoActual(cuentaOrigen.getSaldoActual() - monto);
            cuentaDestino.setSaldoActual(cuentaDestino.getSaldoActual() + monto);

            cuentaBancariaRepository.save(cuentaOrigen);
            cuentaBancariaRepository.save(cuentaDestino);

            logger.info("Transacción de {} realizada exitosamente entre cuentas ID: {} y ID: {}.", monto, idCuentaOrigen, idCuentaDestino);
        } catch (Exception e) {
            logger.error("Error al realizar transacción entre cuentas. Causa: {}", e.getMessage(), e);
            throw e;
        }
    }

}
