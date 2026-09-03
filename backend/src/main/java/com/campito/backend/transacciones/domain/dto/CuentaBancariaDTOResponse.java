package com.campito.backend.transacciones.domain.dto;

import java.math.BigDecimal;

public record CuentaBancariaDTOResponse(
    Long id,
    String nombre,
    String entidadFinanciera,
    BigDecimal saldoActual
) {

}
