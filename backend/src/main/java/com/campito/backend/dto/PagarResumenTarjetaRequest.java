package com.campito.backend.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para la solicitud de pago de resumen de tarjeta.
 * Contiene la lista de cuotas a pagar y la transacción asociada.
 */
public record PagarResumenTarjetaRequest(
    @NotNull(message = "La lista de cuotas no puede ser nula")
    @NotEmpty(message = "La lista de cuotas no puede estar vacía")
    @Valid
    List<CuotaCreditoDTORequest> cuotas,
    
    @NotNull(message = "La transacción no puede ser nula")
    @Valid
    TransaccionDTORequest transaccion
) {
}
