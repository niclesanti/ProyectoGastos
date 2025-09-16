package com.campito.backend.dto;

import java.time.LocalDate;

public record TarjetaListadoDTO(
    Long id,
        String numeroTarjeta,
        String entidadFinanciera,
        String redDePago,
        Integer diaCierre,
        Integer diaVencimientoPago
) {

}
