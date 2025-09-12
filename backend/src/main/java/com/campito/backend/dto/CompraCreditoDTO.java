package com.campito.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.campito.backend.model.CompraCredito;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record CompraCreditoDTO(
    Long id,
    @NotNull(message = "La fecha no puede ser nula")
    @PastOrPresent(message = "La fecha debe ser en el pasado o presente")
    LocalDate fechaCompra,
    @NotNull(message = "El monto no puede ser nulo")
    @Min(value = 0, message = "El monto no puede ser negativo")
    @Max(value = 9999999999999L, message = "El monto no puede exceder los 9.999.999.999.999,99")
    Float montoTotal,
    @NotNull(message = "La cantidad de cuotas no puede ser nula")
    @Min(value = 0, message = "La cantidad de cuotas no puede ser negativa")
    @Max(value = 100, message = "La cantidad de cuotas no puede exceder los 100")
    int cantidadCuotas,
    @Size(max = 100, message = "La descripción no puede exceder los 100 caracteres")
    @NotBlank(message = "La descripción no puede estar vacía")
    String descripcion,
    @Size(max = 100, message = "El nombre completo no puede exceder los 100 caracteres")
    @NotBlank(message = "El nombre completo no puede estar vacío")
    String nombreCompletoAuditoria,
    @NotNull(message = "La fecha no puede ser nula")
    @PastOrPresent(message = "La fecha debe ser en el pasado o presente")
    LocalDateTime fechaCreacion,
    @NotNull(message = "El ID del espacio de trabajo no puede ser nulo")
    Long espacioTrabajoId,
    @NotNull(message = "El ID del motivo de la transacción no puede ser nulo")
    Long motivoId,
    Long comercioId,
    @NotNull(message = "El ID de la tarjeta no puede ser nulo")
    Long tarjetaId
) {
    public CompraCredito toCompraCredito() {
        return new CompraCredito(
            this.fechaCompra,
            this.montoTotal,
            this.cantidadCuotas,
            this.descripcion,
            this.nombreCompletoAuditoria,
            this.fechaCreacion
        );
    }
}
