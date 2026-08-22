package com.campito.backend.service;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
@Slf4j
public class CuentaBancariaServiceImpl implements CuentaBancariaService {

    private final CuentaBancariaRepository cuentaBancariaRepository;
    private final EspacioTrabajoRepository espacioTrabajoRepository;
    private final CuentaBancariaMapper cuentaBancariaMapper;

    /**
     * Crea una nueva cuenta bancaria.
     * 
     * @param cuentaBancariaDTO Datos de la cuenta bancaria a crear.
     * @throws EntityNotFoundException si el espacio de trabajo no se encuentra.
     */
    @Override
    @Transactional
    public void crearCuentaBancaria(CuentaBancariaDTORequest cuentaBancariaDTO) {

        log.info("Creando cuenta bancaria '{}' para entidad '{}'", cuentaBancariaDTO.nombre(), cuentaBancariaDTO.entidadFinanciera());
        // Validar que no exista una cuenta con el mismo nombre en el espacio de trabajo
        Optional<CuentaBancaria> cuentaExistente = cuentaBancariaRepository
                .findFirstByNombreAndEspacioTrabajo_Id(cuentaBancariaDTO.nombre(), cuentaBancariaDTO.idEspacioTrabajo());
        
        if (cuentaExistente.isPresent()) {
            String msg = String.format("Ya existe una cuenta bancaria con el nombre '%s' en este espacio de trabajo. Por favor, utiliza un nombre diferente.", 
                    cuentaBancariaDTO.nombre());
            log.warn(msg);
            throw new EntidadDuplicadaException(msg);
        }
        EspacioTrabajo espacioTrabajo = buscarEspacioTrabajoPorId(cuentaBancariaDTO.idEspacioTrabajo());

        CuentaBancaria cuentaBancaria = cuentaBancariaMapper.toEntity(cuentaBancariaDTO);

        cuentaBancaria.setEspacioTrabajo(espacioTrabajo);
        cuentaBancariaRepository.save(cuentaBancaria);
        log.info("Cuenta bancaria '{}' creada exitosamente.", cuentaBancaria.getNombre());
    }

    /**
     * Actualiza el saldo de una cuenta bancaria según el tipo de transacción.
     * 
     * @param id ID de la cuenta bancaria a actualizar.
     * @param tipo Tipo de transacción (INGRESO o GASTO).
     * @param monto Monto de la transacción.
     * @return Entidad CuentaBancaria actualizada.
     * @throws EntityNotFoundException si la cuenta bancaria no se encuentra.
     * @throws SaldoInsuficienteException si el saldo de la cuenta es insuficiente para realizar un gasto.
     */
    @Override
    @Transactional
    public CuentaBancaria actualizarCuentaBancaria(Long id, TipoTransaccion tipo, BigDecimal monto) {

        log.info("Actualizando saldo de cuenta bancaria ID: {} a monto: {}", id, monto);

        CuentaBancaria cuenta = buscarCuentaBancariaPorId(id);

        if (tipo.equals(TipoTransaccion.GASTO) && cuenta.getSaldoActual().compareTo(monto) < 0) {
            log.warn("Saldo insuficiente en la cuenta bancaria ID: {} para realizar la actualización de monto: {}", id, monto);
            throw new SaldoInsuficienteException(
                String.format("Saldo insuficiente en la cuenta '%s'. Saldo actual: $%.2f, Monto requerido: $%.2f", 
                    cuenta.getNombre(), cuenta.getSaldoActual(), monto));
        }

        cuenta.actualizarSaldoNuevaTransaccion(monto, tipo);

        cuentaBancariaRepository.save(cuenta);
        log.info("Saldo de cuenta bancaria ID: {} actualizado a {}.", id, cuenta.getSaldoActual());
        return cuenta;
    }

    /**
     * Lista todas las cuentas bancarias de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo.
     * @return Lista de cuentas bancarias del espacio de trabajo.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CuentaBancariaDTOResponse> listarCuentasBancarias(UUID idEspacioTrabajo) {

        log.info("Listando cuentas bancarias para el espacio de trabajo ID: {}", idEspacioTrabajo);

        List<CuentaBancariaDTOResponse> cuentas = cuentaBancariaRepository.findByEspacioTrabajo_IdOrderByFechaModificacionDesc(idEspacioTrabajo).stream()
            .map(cuentaBancariaMapper::toResponse)
            .toList();
        log.info("Encontradas {} cuentas bancarias para el espacio de trabajo ID: {} (ordenadas por última modificación).", cuentas.size(), idEspacioTrabajo);
        return cuentas;
    }

    /**
     * Realiza una transacción de transferencia entre dos cuentas bancarias.
     * 
     * @param idCuentaOrigen ID de la cuenta bancaria origen.
     * @param idCuentaDestino ID de la cuenta bancaria destino.
     * @param monto Monto a transferir.
     * @throws EntityNotFoundException si alguna de las cuentas no se encuentra.
     * @throws SaldoInsuficienteException si el saldo de la cuenta origen es insuficiente.
     */
    @Override
    @Transactional
    public void transaccionEntreCuentas(Long idCuentaOrigen, Long idCuentaDestino, BigDecimal monto) {

        CuentaBancaria cuentaOrigen = buscarCuentaBancariaPorId(idCuentaOrigen);
        CuentaBancaria cuentaDestino = buscarCuentaBancariaPorId(idCuentaDestino);

        if(cuentaOrigen.getSaldoActual().compareTo(monto) < 0) {
            log.warn("Saldo insuficiente en la cuenta origen ID: {} para realizar la transacción de monto: {}", idCuentaOrigen, monto);
            throw new SaldoInsuficienteException(
                String.format("Saldo insuficiente en la cuenta origen '%s'. Saldo actual: $%.2f, Monto requerido: $%.2f", 
                    cuentaOrigen.getNombre(), cuentaOrigen.getSaldoActual(), monto));
        }

        cuentaOrigen.setSaldoActual(cuentaOrigen.getSaldoActual().subtract(monto));
        cuentaDestino.setSaldoActual(cuentaDestino.getSaldoActual().add(monto));

        cuentaBancariaRepository.save(cuentaOrigen);
        cuentaBancariaRepository.save(cuentaDestino);

        log.info("Transacción de {} realizada exitosamente entre cuentas ID: {} y ID: {}.", monto, idCuentaOrigen, idCuentaDestino);
    }

    /*
    ===========================================================================
        MÉTODOS AUXILIARES PRIVADOS
    ===========================================================================
    */

    private EspacioTrabajo buscarEspacioTrabajoPorId(UUID idEspacioTrabajo) {
        return espacioTrabajoRepository.findById(idEspacioTrabajo)
            .orElseThrow(() -> {
                String mensaje = "Espacio de trabajo con ID " + idEspacioTrabajo + " no encontrado";
                log.warn(mensaje);
                return new EntityNotFoundException(mensaje);
            });
    }

    private CuentaBancaria buscarCuentaBancariaPorId(Long idCuenta) {
        return cuentaBancariaRepository.findById(idCuenta)
            .orElseThrow(() -> {
                String mensaje = "Cuenta bancaria con ID " + idCuenta + " no encontrada";
                log.warn(mensaje);
                return new EntityNotFoundException(mensaje);
            });
    }

}




