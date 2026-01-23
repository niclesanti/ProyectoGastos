package com.campito.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CompraCreditoDTOResponse(
    Long id,
    LocalDate fechaCompra,
    Float montoTotal,
    int cantidadCuotas,
    int cuotasPagadas,
    String descripcion,
    String nombreCompletoAuditoria,
    LocalDateTime fechaCreacion,
    UUID espacioTrabajoId,
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
