package com.campito.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.campito.backend.usuarios.domain.entity.*;
import com.campito.backend.usuarios.repository.*;
import com.campito.backend.descuentos.domain.entity.Descuento;
import com.campito.backend.descuentos.repository.DescuentoRepository;
import com.campito.backend.exception.ForbiddenException;
import com.campito.backend.exception.UnauthorizedException;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class SecurityServiceImplTest {

    @Mock
    private EspacioTrabajoRepository espacioTrabajoRepository;

    @Mock
    private DescuentoRepository descuentoRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SecurityServiceImpl securityService;

    private UUID userId;
    private UUID idEspacioTrabajo;
    private Usuario usuario;
    private CustomOAuth2User oauthUser;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        idEspacioTrabajo = UUID.fromString("00000000-0000-0000-0000-000000000002");

        usuario = new Usuario();
        usuario.setId(userId);
        usuario.setEmail("user@test.com");
        usuario.setNombre("Test User");
        usuario.setProveedor(ProveedorAutenticacion.MANUAL);
        usuario.setRol("USER");
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDateTime.now());

        oauthUser = new CustomOAuth2User(null, null, usuario);
    }

    // =========================================================
    // validateDescuentoOwnership
    // =========================================================

    @Test
    void validateDescuentoOwnership_conIdNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> securityService.validateDescuentoOwnership(null));
    }

    @Test
    void validateDescuentoOwnership_descuentoNoEncontrado_lanzaEntityNotFoundException() {
        when(descuentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> securityService.validateDescuentoOwnership(99L));
    }

    @Test
    void validateDescuentoOwnership_conAccesoExitoso() {
        Descuento descuento = Descuento.builder()
            .id(1L)
            .idEspacioTrabajo(idEspacioTrabajo)
            .build();

        when(descuentoRepository.findById(1L)).thenReturn(Optional.of(descuento));

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockSecurityContext = mock(SecurityContext.class);
            Authentication mockAuth = mock(Authentication.class);
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);
            when(mockSecurityContext.getAuthentication()).thenReturn(mockAuth);
            when(mockAuth.isAuthenticated()).thenReturn(true);
            when(mockAuth.getPrincipal()).thenReturn(oauthUser);

            when(espacioTrabajoRepository
                .existsByIdAndUsuariosParticipantes_Id(idEspacioTrabajo, userId))
                .thenReturn(true);

            assertDoesNotThrow(() -> securityService.validateDescuentoOwnership(1L));

            verify(descuentoRepository, times(1)).findById(1L);
            verify(espacioTrabajoRepository, times(1))
                .existsByIdAndUsuariosParticipantes_Id(idEspacioTrabajo, userId);
        }
    }

    @Test
    void validateDescuentoOwnership_sinAcceso_lanzaForbiddenException() {
        Descuento descuento = Descuento.builder()
            .id(1L)
            .idEspacioTrabajo(idEspacioTrabajo)
            .build();

        when(descuentoRepository.findById(1L)).thenReturn(Optional.of(descuento));

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockSecurityContext = mock(SecurityContext.class);
            Authentication mockAuth = mock(Authentication.class);
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);
            when(mockSecurityContext.getAuthentication()).thenReturn(mockAuth);
            when(mockAuth.isAuthenticated()).thenReturn(true);
            when(mockAuth.getPrincipal()).thenReturn(oauthUser);

            when(espacioTrabajoRepository
                .existsByIdAndUsuariosParticipantes_Id(idEspacioTrabajo, userId))
                .thenReturn(false);

            assertThrows(ForbiddenException.class,
                () -> securityService.validateDescuentoOwnership(1L));
        }
    }

    @Test
    void validateDescuentoOwnership_usuarioNoAutenticado_lanzaUnauthorizedException() {
        Descuento descuento = Descuento.builder()
            .id(1L)
            .idEspacioTrabajo(idEspacioTrabajo)
            .build();

        when(descuentoRepository.findById(1L)).thenReturn(Optional.of(descuento));

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockSecurityContext = mock(SecurityContext.class);
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);
            when(mockSecurityContext.getAuthentication()).thenReturn(null);

            assertThrows(UnauthorizedException.class,
                () -> securityService.validateDescuentoOwnership(1L));
        }
    }
}
