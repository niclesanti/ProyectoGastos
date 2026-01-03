package com.campito.backend.dto;

import java.time.LocalDate;

import com.campito.backend.model.EstadoResumen;

public record ResumenDTOResponse(
    Long id,
    Integer anio,
    Integer mes,
    LocalDate fechaVencimiento,
    EstadoResumen estado,
    Float montoTotal,
    Long idTarjeta,
    String numeroTarjeta,
    String entidadFinanciera,
    String redDePago,
    Long idTransaccionAsociada,
    Integer cantidadCuotas
) {

}
