package com.campito.backend.dto;

import java.util.UUID;

public record EspacioTrabajoDTOResponse(
    UUID id,
    String nombre,
    Float saldo,
    UUID usuarioAdminId
) {

}
