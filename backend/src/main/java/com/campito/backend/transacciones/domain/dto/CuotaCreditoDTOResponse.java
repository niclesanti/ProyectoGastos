package com.campito.backend.transacciones.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CuotaCreditoDTOResponse(
    Long id,
    int numeroCuota,
    LocalDate fechaVencimiento,
    BigDecimal montoCuota,
    boolean pagada,
    Long idCompraCredito,
    Long idResumenAsociado
) {

}
