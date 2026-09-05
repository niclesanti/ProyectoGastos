package com.campito.backend.usuarios.service;

import java.util.UUID;

import com.campito.backend.usuarios.domain.dto.UsuarioDTOResponse;

public interface UsuarioService {

    /**
     * Obtiene los datos del usuario autenticado a partir de su ID.
     *
     * @param userId ID del usuario autenticado.
     * @return DTO con los datos del usuario.
     */
    UsuarioDTOResponse getUsuarioAutenticado(UUID userId);
}
