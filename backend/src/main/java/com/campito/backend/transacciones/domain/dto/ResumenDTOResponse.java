package com.campito.backend.transacciones.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.campito.backend.transacciones.domain.entity.EstadoResumen;

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
