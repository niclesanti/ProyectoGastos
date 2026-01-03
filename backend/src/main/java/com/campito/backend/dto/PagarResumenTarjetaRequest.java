package com.campito.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para la solicitud de pago de resumen de tarjeta.
 * Contiene el ID del resumen a pagar y la transacción asociada.
 */
public record PagarResumenTarjetaRequest(
    @NotNull(message = "El ID del resumen no puede ser nulo")
    Long idResumen,
    
    @NotNull(message = "La transacción no puede ser nula")
    @Valid
    TransaccionDTORequest transaccion
) {
}
