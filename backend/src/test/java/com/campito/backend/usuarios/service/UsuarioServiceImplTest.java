package com.campito.backend.usuarios.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campito.backend.usuarios.domain.entity.Usuario;
import com.campito.backend.usuarios.domain.entity.ProveedorAutenticacion;
import com.campito.backend.usuarios.domain.dto.UsuarioDTOResponse;
import com.campito.backend.usuarios.mapper.UsuarioMapper;
import com.campito.backend.usuarios.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        usuario = Usuario.builder()
            .id(userId)
            .nombre("Test User")
            .email("test@test.com")
            .proveedor(ProveedorAutenticacion.GOOGLE)
            .rol("USER")
            .activo(true)
            .build();
    }

    @Test
    void getUsuarioAutenticado_usuarioExistente_retornaDto() {
        UsuarioDTOResponse expectedResponse = new UsuarioDTOResponse(userId, "Test User", "test@test.com", null);

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponse(usuario)).thenReturn(expectedResponse);

        UsuarioDTOResponse result = usuarioService.getUsuarioAutenticado(userId);

        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("Test User", result.nombre());
    }

    @Test
    void getUsuarioAutenticado_usuarioNoExistente_lanzaEntityNotFoundException() {
        when(usuarioRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> usuarioService.getUsuarioAutenticado(userId));
    }
}
