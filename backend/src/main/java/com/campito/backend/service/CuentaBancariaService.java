package com.campito.backend.service;

import java.util.List;
import java.util.UUID;

import com.campito.backend.dto.CuentaBancariaDTORequest;
import com.campito.backend.dto.CuentaBancariaDTOResponse;
import com.campito.backend.model.CuentaBancaria;
import com.campito.backend.model.TipoTransaccion;

public interface CuentaBancariaService {
    public void crearCuentaBancaria(CuentaBancariaDTORequest cuentaBancariaDTO);
    public CuentaBancaria actualizarCuentaBancaria(Long id, TipoTransaccion tipo, Float monto);
    public List<CuentaBancariaDTOResponse> listarCuentasBancarias(UUID idEspacioTrabajo);
    public void transaccionEntreCuentas(Long idCuentaOrigen, Long idCuentaDestino, Float monto);
}
