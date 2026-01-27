package com.campito.backend.dto;

import java.time.LocalDate;

public record CuotaCreditoDTOResponse(
    Long id,
    int numeroCuota,
    LocalDate fechaVencimiento,
    Float montoCuota,
    boolean pagada,
    Long idCompraCredito,
    Long idResumenAsociado
) {

}
