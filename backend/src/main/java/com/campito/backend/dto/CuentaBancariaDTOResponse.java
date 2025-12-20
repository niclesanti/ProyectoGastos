package com.campito.backend.dto;

public record CuentaBancariaDTOResponse(
    Long id,
    String nombre,
    String entidadFinanciera,
    Float saldoActual
) {

}
