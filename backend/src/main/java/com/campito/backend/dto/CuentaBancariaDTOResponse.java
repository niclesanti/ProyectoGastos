package com.campito.backend.dto;

import java.math.BigDecimal;

public record CuentaBancariaDTOResponse(
    Long id,
    String nombre,
    String entidadFinanciera,
    BigDecimal saldoActual
) {

}
