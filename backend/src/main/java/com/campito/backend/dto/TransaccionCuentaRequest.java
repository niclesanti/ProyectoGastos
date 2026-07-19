package com.campito.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import com.campito.backend.validation.ValidMonto;

import java.math.BigDecimal;

public record TransaccionCuentaRequest(
    @NotNull(message = "La cuenta de origen es obligatoria")
    Long idCuentaOrigen,

    @NotNull(message = "La cuenta de destino es obligatoria")
    Long idCuentaDestino,

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.009", message = "El monto debe ser mayor a 0")
    @ValidMonto
    BigDecimal monto
) {}
