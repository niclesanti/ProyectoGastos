package com.campito.backend.transacciones.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.campito.backend.transacciones.domain.dto.CuentaBancariaDTORequest;
import com.campito.backend.transacciones.domain.dto.CuentaBancariaDTOResponse;
import com.campito.backend.transacciones.domain.entity.CuentaBancaria;
import com.campito.backend.common.domain.TipoTransaccion;

public interface CuentaBancariaService {
    public void crearCuentaBancaria(CuentaBancariaDTORequest cuentaBancariaDTO);
    public CuentaBancaria actualizarCuentaBancaria(Long id, TipoTransaccion tipo, BigDecimal monto);
    public List<CuentaBancariaDTOResponse> listarCuentasBancarias(UUID idEspacioTrabajo);
    public void transaccionEntreCuentas(Long idCuentaOrigen, Long idCuentaDestino, BigDecimal monto);
}
