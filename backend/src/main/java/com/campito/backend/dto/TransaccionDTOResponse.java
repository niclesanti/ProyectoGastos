package com.campito.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.campito.backend.model.TipoTransaccion;

public record TransaccionDTOResponse(
    Long id,
    LocalDate fecha,
    Float monto,
    TipoTransaccion tipo,
    String descripcion,
    String nombreCompletoAuditoria,
    LocalDateTime fechaCreacion,
    UUID idEspacioTrabajo,
    String nombreEspacioTrabajo,
    Long idMotivo,
    String nombreMotivo,
    Long idContacto,
    String nombreContacto,
    String nombreCuentaBancaria
) {

}
