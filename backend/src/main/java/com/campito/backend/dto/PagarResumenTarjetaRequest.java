package com.campito.backend.dto;

import java.time.LocalDate;

import com.campito.backend.validation.ValidMonto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de pago de resumen de tarjeta.
 * 
 */
public record PagarResumenTarjetaRequest(
    @NotNull(message = "El ID del resumen no puede ser nulo")
    Long idResumen,
    @NotNull(message = "La fecha no puede ser nula")
    @PastOrPresent(message = "La fecha debe ser en el pasado o presente")
    LocalDate fecha,
    @NotNull(message = "El monto no puede ser nulo")
    @DecimalMin(value = "0.009", message = "El monto debe ser mayor a 0")
    @ValidMonto
    Float monto,
    @NotBlank(message = "El nombre del usuario no puede estar vacío")
    @Size(max = 100, message = "El nombre completo del usuario no puede exceder los 100 caracteres")
    String nombreCompletoAuditoria,
    @NotNull(message = "El ID del espacio de trabajo no puede ser nulo")
    Long idEspacioTrabajo,
    Long idCuentaBancaria
) {
}
