package com.campito.backend.dto;

public record EspacioTrabajoDTOResponse(
    Long id,
    String nombre,
    Float saldo,
    Long usuarioAdminId
) {

}
