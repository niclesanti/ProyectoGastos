package com.campito.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;
import com.campito.backend.validation.ValidNombre;
import com.campito.backend.validation.ValidSaldoActual;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CuentaBancariaDTORequest(
    @Size(max = 50, message = "El nombre de la cuenta no puede exceder los 50 caracteres")
    @NotBlank(message = "El nombre de la cuenta no puede estar vacío")
    @ValidNombre
    String nombre,
    @Size(max = 50, message = "La entidad financiera no puede exceder los 50 caracteres")
    @NotBlank(message = "La entidad financiera no puede estar vacía")
    String entidadFinanciera,
    @NotNull(message = "El ID del espacio de trabajo no puede ser nulo")
    UUID idEspacioTrabajo,
    @NotNull(message = "El saldo actual no puede ser nulo")
    @ValidSaldoActual
    BigDecimal saldoActual
) {
}
