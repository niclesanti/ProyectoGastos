package com.campito.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.campito.backend.validation.ValidMonto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CuotaCreditoDTORequest(
    @NotNull(message = "El numero de cuota no puede ser nulo")
    @Min(value = 0, message = "El numero de cuota no puede ser negativo")
    @Max(value = 100, message = "El numero de cuota no puede exceder los 100")
    int numeroCuota,
    @NotNull(message = "La fecha no puede ser nula")
    @FutureOrPresent(message = "La fecha debe ser en el presente o futuro")
    LocalDate fechaVencimiento,
    @NotNull(message = "El monto no puede ser nulo")
    @DecimalMin(value = "0.009", message = "El monto debe ser mayor a 0")
    @ValidMonto
    BigDecimal montoCuota,
    @NotNull(message = "El ID de la compra de crédito no puede ser nulo")
    Long idCompraCredito,
    Long idResumenAsociado
) {

}
