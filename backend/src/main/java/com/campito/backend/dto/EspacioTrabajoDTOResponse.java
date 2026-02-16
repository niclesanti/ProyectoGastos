package com.campito.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record EspacioTrabajoDTOResponse(
    UUID id,
    String nombre,
    BigDecimal saldo,
    UUID usuarioAdminId
) {

}
