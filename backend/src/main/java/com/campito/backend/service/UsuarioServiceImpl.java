package com.campito.backend.service;

import com.campito.backend.dao.UsuarioRepository;
import com.campito.backend.dto.UsuarioDTOResponse;
import com.campito.backend.mapper.UsuarioMapper;
import com.campito.backend.model.Usuario;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del servicio para gestión de usuarios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    /**
     * Obtiene los datos del usuario autenticado a partir de su ID.
     *
     * @param userId ID del usuario autenticado.
     * @return DTO con los datos del usuario.
     */
    @Override
    @Transactional(readOnly = true)
    public UsuarioDTOResponse getUsuarioAutenticado(UUID userId) {
        log.info("Obteniendo datos del usuario autenticado con ID: {}", userId);
        Usuario usuario = buscarUsuarioPorId(userId);
        return usuarioMapper.toResponse(usuario);
    }

    /*
    ===========================================================================
        MÉTODOS AUXILIARES PRIVADOS
    ===========================================================================
    */

    private Usuario buscarUsuarioPorId(UUID idUsuario) {
        return usuarioRepository.findById(idUsuario).orElseThrow(() -> {
            String mensaje = "Usuario con ID " + idUsuario + " no encontrado";
            log.warn(mensaje);
            return new EntityNotFoundException(mensaje);
        });
    }
}
