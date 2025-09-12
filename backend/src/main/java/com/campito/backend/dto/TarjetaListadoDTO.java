package com.campito.backend.dto;

import java.time.LocalDate;

public record TarjetaListadoDTO(
    Long id,
        String numeroTarjeta,
        String entidadFinanciera,
        String redDePago,
        LocalDate fechaCierre,
        LocalDate fechaVencimientoPago
) {

}
