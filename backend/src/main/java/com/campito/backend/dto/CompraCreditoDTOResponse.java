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
    String nombreEspacioTrabajo,
    Long motivoId,
    String nombreMotivo,
    Long comercioId,
    String nombreComercio,
    Long tarjetaId,
    String numeroTarjeta,
    String entidadFinanciera,
    String redDePago
) {

}
