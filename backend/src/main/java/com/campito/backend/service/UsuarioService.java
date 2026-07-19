package com.campito.backend.service;

import com.campito.backend.dao.UsuarioRepository;
import com.campito.backend.dto.UsuarioDTO;
import com.campito.backend.model.Usuario;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDTO getUsuarioAutenticado(UUID userId) {
        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        return UsuarioDTO.fromUsuario(usuario);
    }
}
