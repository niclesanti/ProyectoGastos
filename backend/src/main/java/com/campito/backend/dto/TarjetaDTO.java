package com.campito.backend.dto;

import java.time.LocalDate;

import com.campito.backend.model.Tarjeta;

import jakarta.validation.constraints.FutureOrPresent;
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
        @FutureOrPresent(message = "La fecha debe ser en el presente o futuro")
        LocalDate fechaCierre,
        @NotNull(message = "La fecha no puede ser nula")
        @FutureOrPresent(message = "La fecha debe ser en el presente o futuro")
        LocalDate fechaVencimientoPago
) {
    public Tarjeta toTarjeta() {
        return new Tarjeta(
                this.numeroTarjeta,
                this.entidadFinanciera,
                this.redDePago,
                this.fechaCierre,
                this.fechaVencimientoPago
        );
    }
}
