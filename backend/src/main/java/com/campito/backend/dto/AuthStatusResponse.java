package com.campito.backend.dto;

public record AuthStatusResponse(
    boolean authenticated,
    UsuarioDTOResponse user,
    String token
) {}
