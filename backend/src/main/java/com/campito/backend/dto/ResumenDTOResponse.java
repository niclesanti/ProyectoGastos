package com.campito.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.campito.backend.model.EstadoResumen;

public record ResumenDTOResponse(
    Long id,
    Integer anio,
    Integer mes,
    LocalDate fechaVencimiento,
    EstadoResumen estado,
    BigDecimal montoTotal,
    Long idTarjeta,
    String numeroTarjeta,
    String entidadFinanciera,
    String redDePago,
    Long idTransaccionAsociada,
    Integer cantidadCuotas,
    List<CuotaResumenDTO> cuotas
) {

}
