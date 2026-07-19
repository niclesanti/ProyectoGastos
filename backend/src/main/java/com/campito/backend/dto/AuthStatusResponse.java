package com.campito.backend.dto;

public record AuthStatusResponse(
    boolean authenticated,
    UsuarioDTO user,
    String token
) {}
