package com.campito.backend.dto;

/**
 * DTO simplificado para las cuotas dentro de un resumen.
 * Contiene solo la información relevante para mostrar en el detalle del resumen.
 */
public record CuotaResumenDTO(
    Long id,
    Integer numeroCuota,
    Float montoCuota,
    String descripcion,
    Integer totalCuotas,
    String motivo
) {

}
