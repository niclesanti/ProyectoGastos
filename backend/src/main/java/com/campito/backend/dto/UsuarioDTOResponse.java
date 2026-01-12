package com.campito.backend.dto;

public record UsuarioDTOResponse(
    Long id,
    String nombre,
    String email,
    String fotoPerfil
) {

}
