package com.campito.backend.dto;

import java.time.LocalDate;

import com.campito.backend.validation.ValidDescripcion;
import com.campito.backend.validation.ValidMonto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record CompraCreditoDTORequest(
    @NotNull(message = "La fecha no puede ser nula")
    @PastOrPresent(message = "La fecha debe ser en el pasado o presente")
    LocalDate fechaCompra,
    @NotNull(message = "El monto no puede ser nulo")
    @DecimalMin(value = "0.009", message = "El monto debe ser mayor a 0")
    @ValidMonto
    Float montoTotal,
    @NotNull(message = "La cantidad de cuotas no puede ser nula")
    @Min(value = 1, message = "La cantidad de cuotas debe ser al menos 1")
    @Max(value = 12, message = "La cantidad de cuotas no puede exceder los 12")
    int cantidadCuotas,
    @Size(max = 100, message = "La descripción no puede exceder los 100 caracteres")
    @ValidDescripcion
    String descripcion,
    @Size(max = 100, message = "El nombre completo no puede exceder los 100 caracteres")
    @NotBlank(message = "El nombre completo no puede estar vacío")
    String nombreCompletoAuditoria,
    @NotNull(message = "El ID del espacio de trabajo no puede ser nulo")
    Long espacioTrabajoId,
    @NotNull(message = "El ID del motivo de la transacción no puede ser nulo")
    Long motivoId,
    Long comercioId,
    @NotNull(message = "El ID de la tarjeta no puede ser nulo")
    Long tarjetaId
) {

}
