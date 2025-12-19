package com.campito.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CompraCreditoDTOResponse(
    Long id,
    LocalDate fechaCompra,
    Float montoTotal,
    int cantidadCuotas,
    int cuotasPagadas,
    String descripcion,
    String nombreCompletoAuditoria,
    LocalDateTime fechaCreacion,
    Long espacioTrabajoId,
    Long motivoId,
    Long comercioId,
    Long tarjetaId
) {

}
