package com.campito.backend.transacciones.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TarjetaDTOUpdate(
    @NotNull(message = "La fecha no puede ser nula")
    @Min(value = 1, message = "El minimo valor es 1")
    @Max(value = 31, message = "El maximo valor es 31")
    Integer diaCierre,
    @NotNull(message = "La fecha no puede ser nula")
    @Min(value = 1, message = "El minimo valor es 1")
    @Max(value = 31, message = "El maximo valor es 31")
    Integer diaVencimientoPago
) {

}
