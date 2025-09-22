package com.campito.backend.dto;


import com.campito.backend.model.Tarjeta;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TarjetaDTO(
        Long id,
        @Size(max = 4, message = "El número no puede exceder los 4 caracteres")
        @NotBlank(message = "El número no puede estar vacío")
        String numeroTarjeta,
        @Size(max = 50, message = "La entidad financiera no puede exceder los 50 caracteres")
        @NotBlank(message = "La entidad financiera no puede estar vacía")
        String entidadFinanciera,
        @Size(max = 50, message = "La red de pago no puede exceder los 50 caracteres")
        @NotBlank(message = "La red de pago no puede estar vacía")
        String redDePago,
        @NotNull(message = "La fecha no puede ser nula")
        @Min(value = 1, message = "El minimo valor es 1")
        @Max(value = 29, message = "El maximo valor es 29")
        Integer diaCierre,
        @NotNull(message = "La fecha no puede ser nula")
        @Min(value = 1, message = "El minimo valor es 1")
        @Max(value = 29, message = "El maximo valor es 29")
        Integer diaVencimientoPago,
        @NotNull(message = "El ID del espacio de trabajo no puede ser nulo")
        Long espacioTrabajoId
) {
    public Tarjeta toTarjeta() {
        return new Tarjeta(
                this.numeroTarjeta,
                this.entidadFinanciera,
                this.redDePago,
                this.diaCierre,
                this.diaVencimientoPago
        );
    }
}
