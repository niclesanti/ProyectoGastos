package com.campito.backend.dto;


public record TarjetaListadoDTO(
    Long id,
        String numeroTarjeta,
        String entidadFinanciera,
        String redDePago,
        Integer diaCierre,
        Integer diaVencimientoPago
) {

}
