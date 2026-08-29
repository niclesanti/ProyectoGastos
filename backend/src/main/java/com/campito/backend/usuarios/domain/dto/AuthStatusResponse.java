package com.campito.backend.usuarios.domain.dto;

public record AuthStatusResponse(
    boolean authenticated,
    UsuarioDTOResponse user,
    String token
) {}
