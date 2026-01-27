package com.campito.backend.dto;

import java.util.UUID;

import com.campito.backend.model.Usuario;

public record UsuarioDTO(
    UUID id,
    String nombre,
    String email,
    String fotoPerfil
) {
    public static UsuarioDTO fromUsuario(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getFotoPerfil());
    }
}
