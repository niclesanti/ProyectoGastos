package com.campito.backend.dto;

public record TarjetaDTOResponse(
    Long id,
    String numeroTarjeta,
    String entidadFinanciera,
    String redDePago,
    Integer diaCierre,
    Integer diaVencimientoPago,
    Long espacioTrabajoId
) {

}
