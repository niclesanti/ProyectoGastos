package com.campito.backend.dto;

import java.util.UUID;

public record TarjetaDTOResponse(
    Long id,
    String numeroTarjeta,
    String entidadFinanciera,
    String redDePago,
    Integer diaCierre,
    Integer diaVencimientoPago,
    UUID espacioTrabajoId
) {

}
