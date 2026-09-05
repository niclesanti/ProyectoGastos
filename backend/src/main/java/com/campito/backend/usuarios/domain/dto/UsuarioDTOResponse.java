package com.campito.backend.usuarios.domain.dto;

import java.util.UUID;

public record UsuarioDTOResponse(
    UUID id,
    String nombre,
    String email,
    String fotoPerfil
) {

}
