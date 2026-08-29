package com.campito.backend.transacciones.domain.dto;

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
