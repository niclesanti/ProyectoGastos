package com.campito.backend.dto;

import java.time.LocalDate;

import com.campito.backend.model.TipoTransaccion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record TransaccionDTORequest(
    @NotNull(message = "La fecha no puede ser nula")
    @PastOrPresent(message = "La fecha debe ser en el pasado o presente")
    LocalDate fecha,
    @NotNull(message = "El monto no puede ser nulo")
    @Min(value = 0, message = "El monto no puede ser negativo")
    @Max(value = 9999999999999L, message = "El monto no puede exceder los 9.999.999.999.999,99")
    Float monto,
    @NotNull(message = "El tipo de transacción no puede ser nulo")
    TipoTransaccion tipo,
    @Size(max = 100, message = "La descripción no puede exceder los 100 caracteres")
    String descripcion,
    @NotBlank(message = "El nombre del usuario no puede estar vacío")
    @Size(max = 100, message = "El nombre completo del usuario no puede exceder los 100 caracteres")
    String nombreCompletoAuditoria,
    @NotNull(message = "El ID del espacio de trabajo no puede ser nulo")
    Long idEspacioTrabajo,
    @NotNull(message = "El ID del motivo no puede ser nulo")
    Long idMotivo,
    Long idContacto,
    Long idCuentaBancaria
) {

}
