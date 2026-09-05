package com.campito.backend.security;

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
import com.campito.backend.transacciones.domain.entity.*;
import com.campito.backend.transacciones.repository.*;
import com.campito.backend.notificaciones.domain.entity.Notificacion;
import com.campito.backend.notificaciones.repository.NotificacionRepository;
import com.campito.backend.descuentos.domain.entity.Descuento;
import com.campito.backend.descuentos.repository.DescuentoRepository;
import com.campito.backend.common.exception.ForbiddenException;
import com.campito.backend.common.exception.UnauthorizedException;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class SecurityServiceImplTest {

    @Mock
    private EspacioTrabajoRepository espacioTrabajoRepository;
    @Mock
    private TransaccionRepository transaccionRepository;
    @Mock
    private CompraCreditoRepository compraCreditoRepository;
    @Mock
    private CuentaBancariaRepository cuentaBancariaRepository;
    @Mock
    private TarjetaRepository tarjetaRepository;
    @Mock
    private NotificacionRepository notificacionRepository;
    @Mock
    private SolicitudPendienteEspacioTrabajoRepository solicitudPendienteRepository;
    @Mock
    private DescuentoRepository descuentoRepository;

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
    // Helper: setup SecurityContextHolder mock
    // =========================================================
    private MockedStatic<SecurityContextHolder> mockSecurity(UUID uid) {
        MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class);
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        mocked.when(SecurityContextHolder::getContext).thenReturn(ctx);
        when(ctx.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);

        if (uid != null) {
            when(auth.getPrincipal()).thenReturn(oauthUser);
        }
        return mocked;
    }

    private void mockWorkspaceExists(UUID workspaceId, UUID uid, boolean exists) {
        when(espacioTrabajoRepository.existsByIdAndUsuariosParticipantes_Id(workspaceId, uid))
            .thenReturn(exists);
    }

    // =========================================================
    // getAuthenticatedUserId
    // =========================================================

    @Test
    void getAuthenticatedUserId_autenticado_retornaUserId() {
        try (var mocked = mockSecurity(userId)) {
            assertEquals(userId, securityService.getAuthenticatedUserId());
        }
    }

    @Test
    void getAuthenticatedUserId_authNulo_lanzaUnauthorizedException() {
        try (var mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext ctx = mock(SecurityContext.class);
            mocked.when(SecurityContextHolder::getContext).thenReturn(ctx);
            when(ctx.getAuthentication()).thenReturn(null);

            assertThrows(UnauthorizedException.class,
                () -> securityService.getAuthenticatedUserId());
        }
    }

    @Test
    void getAuthenticatedUserId_authNoAutenticado_lanzaUnauthorizedException() {
        try (var mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext ctx = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);
            mocked.when(SecurityContextHolder::getContext).thenReturn(ctx);
            when(ctx.getAuthentication()).thenReturn(auth);
            when(auth.isAuthenticated()).thenReturn(false);

            assertThrows(UnauthorizedException.class,
                () -> securityService.getAuthenticatedUserId());
        }
    }

    @Test
    void getAuthenticatedUserId_principalNoEsOAuthUser_lanzaUnauthorizedException() {
        try (var mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext ctx = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);
            mocked.when(SecurityContextHolder::getContext).thenReturn(ctx);
            when(ctx.getAuthentication()).thenReturn(auth);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn("stringPrincipal");

            assertThrows(UnauthorizedException.class,
                () -> securityService.getAuthenticatedUserId());
        }
    }

    // =========================================================
    // validateWorkspaceAccess
    // =========================================================

    @Test
    void validateWorkspaceAccess_idNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> securityService.validateWorkspaceAccess(null));
    }

    @Test
    void validateWorkspaceAccess_conAcceso_noLanzaExcepcion() {
        try (var mocked = mockSecurity(userId)) {
            mockWorkspaceExists(idEspacioTrabajo, userId, true);

            assertDoesNotThrow(() -> securityService.validateWorkspaceAccess(idEspacioTrabajo));
        }
    }

    @Test
    void validateWorkspaceAccess_sinAcceso_lanzaForbiddenException() {
        try (var mocked = mockSecurity(userId)) {
            mockWorkspaceExists(idEspacioTrabajo, userId, false);

            assertThrows(ForbiddenException.class,
                () -> securityService.validateWorkspaceAccess(idEspacioTrabajo));
        }
    }

    // =========================================================
    // validateWorkspaceAdmin
    // =========================================================

    @Test
    void validateWorkspaceAdmin_idNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> securityService.validateWorkspaceAdmin(null));
    }

    @Test
    void validateWorkspaceAdmin_noEncontrado_lanzaEntityNotFoundException() {
        try (var mocked = mockSecurity(userId)) {
            when(espacioTrabajoRepository.findById(idEspacioTrabajo)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                () -> securityService.validateWorkspaceAdmin(idEspacioTrabajo));
        }
    }

    @Test
    void validateWorkspaceAdmin_noEsAdmin_lanzaForbiddenException() {
        UUID otroUsuario = UUID.fromString("00000000-0000-0000-0000-000000000099");
        Usuario admin = new Usuario();
        admin.setId(otroUsuario);

        EspacioTrabajo espacio = new EspacioTrabajo();
        espacio.setUsuarioAdmin(admin);

        try (var mocked = mockSecurity(userId)) {
            when(espacioTrabajoRepository.findById(idEspacioTrabajo)).thenReturn(Optional.of(espacio));

            assertThrows(ForbiddenException.class,
                () -> securityService.validateWorkspaceAdmin(idEspacioTrabajo));
        }
    }

    @Test
    void validateWorkspaceAdmin_esAdmin_noLanzaExcepcion() {
        EspacioTrabajo espacio = new EspacioTrabajo();
        espacio.setUsuarioAdmin(usuario);

        try (var mocked = mockSecurity(userId)) {
            when(espacioTrabajoRepository.findById(idEspacioTrabajo)).thenReturn(Optional.of(espacio));

            assertDoesNotThrow(() -> securityService.validateWorkspaceAdmin(idEspacioTrabajo));
        }
    }

    // =========================================================
    // validateTransactionOwnership
    // =========================================================

    @Test
    void validateTransactionOwnership_idNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> securityService.validateTransactionOwnership(null));
    }

    @Test
    void validateTransactionOwnership_noEncontrado_lanzaEntityNotFoundException() {
        when(transaccionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> securityService.validateTransactionOwnership(99L));
    }

    @Test
    void validateTransactionOwnership_conAcceso_noLanzaExcepcion() {
        Transaccion transaccion = new Transaccion();
        transaccion.setIdEspacioTrabajo(idEspacioTrabajo);

        try (var mocked = mockSecurity(userId)) {
            when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccion));
            mockWorkspaceExists(idEspacioTrabajo, userId, true);

            assertDoesNotThrow(() -> securityService.validateTransactionOwnership(1L));
        }
    }

    @Test
    void validateTransactionOwnership_sinAcceso_lanzaForbiddenException() {
        Transaccion transaccion = new Transaccion();
        transaccion.setIdEspacioTrabajo(idEspacioTrabajo);

        try (var mocked = mockSecurity(userId)) {
            when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccion));
            mockWorkspaceExists(idEspacioTrabajo, userId, false);

            assertThrows(ForbiddenException.class,
                () -> securityService.validateTransactionOwnership(1L));
        }
    }

    // =========================================================
    // validateCompraCreditoOwnership
    // =========================================================

    @Test
    void validateCompraCreditoOwnership_idNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> securityService.validateCompraCreditoOwnership(null));
    }

    @Test
    void validateCompraCreditoOwnership_noEncontrado_lanzaEntityNotFoundException() {
        when(compraCreditoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> securityService.validateCompraCreditoOwnership(99L));
    }

    @Test
    void validateCompraCreditoOwnership_conAcceso_noLanzaExcepcion() {
        CompraCredito compra = new CompraCredito();
        compra.setIdEspacioTrabajo(idEspacioTrabajo);

        try (var mocked = mockSecurity(userId)) {
            when(compraCreditoRepository.findById(1L)).thenReturn(Optional.of(compra));
            mockWorkspaceExists(idEspacioTrabajo, userId, true);

            assertDoesNotThrow(() -> securityService.validateCompraCreditoOwnership(1L));
        }
    }

    @Test
    void validateCompraCreditoOwnership_sinAcceso_lanzaForbiddenException() {
        CompraCredito compra = new CompraCredito();
        compra.setIdEspacioTrabajo(idEspacioTrabajo);

        try (var mocked = mockSecurity(userId)) {
            when(compraCreditoRepository.findById(1L)).thenReturn(Optional.of(compra));
            mockWorkspaceExists(idEspacioTrabajo, userId, false);

            assertThrows(ForbiddenException.class,
                () -> securityService.validateCompraCreditoOwnership(1L));
        }
    }

    // =========================================================
    // validateCuentaBancariaOwnership
    // =========================================================

    @Test
    void validateCuentaBancariaOwnership_idNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> securityService.validateCuentaBancariaOwnership(null));
    }

    @Test
    void validateCuentaBancariaOwnership_noEncontrado_lanzaEntityNotFoundException() {
        when(cuentaBancariaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> securityService.validateCuentaBancariaOwnership(99L));
    }

    @Test
    void validateCuentaBancariaOwnership_conAcceso_noLanzaExcepcion() {
        CuentaBancaria cuenta = new CuentaBancaria();
        cuenta.setIdEspacioTrabajo(idEspacioTrabajo);

        try (var mocked = mockSecurity(userId)) {
            when(cuentaBancariaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
            mockWorkspaceExists(idEspacioTrabajo, userId, true);

            assertDoesNotThrow(() -> securityService.validateCuentaBancariaOwnership(1L));
        }
    }

    @Test
    void validateCuentaBancariaOwnership_sinAcceso_lanzaForbiddenException() {
        CuentaBancaria cuenta = new CuentaBancaria();
        cuenta.setIdEspacioTrabajo(idEspacioTrabajo);

        try (var mocked = mockSecurity(userId)) {
            when(cuentaBancariaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
            mockWorkspaceExists(idEspacioTrabajo, userId, false);

            assertThrows(ForbiddenException.class,
                () -> securityService.validateCuentaBancariaOwnership(1L));
        }
    }

    // =========================================================
    // validateTarjetaOwnership
    // =========================================================

    @Test
    void validateTarjetaOwnership_idNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> securityService.validateTarjetaOwnership(null));
    }

    @Test
    void validateTarjetaOwnership_noEncontrado_lanzaEntityNotFoundException() {
        when(tarjetaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> securityService.validateTarjetaOwnership(99L));
    }

    @Test
    void validateTarjetaOwnership_conAcceso_noLanzaExcepcion() {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setIdEspacioTrabajo(idEspacioTrabajo);

        try (var mocked = mockSecurity(userId)) {
            when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));
            mockWorkspaceExists(idEspacioTrabajo, userId, true);

            assertDoesNotThrow(() -> securityService.validateTarjetaOwnership(1L));
        }
    }

    @Test
    void validateTarjetaOwnership_sinAcceso_lanzaForbiddenException() {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setIdEspacioTrabajo(idEspacioTrabajo);

        try (var mocked = mockSecurity(userId)) {
            when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));
            mockWorkspaceExists(idEspacioTrabajo, userId, false);

            assertThrows(ForbiddenException.class,
                () -> securityService.validateTarjetaOwnership(1L));
        }
    }

    // =========================================================
    // validateNotificacionOwnership
    // =========================================================

    @Test
    void validateNotificacionOwnership_idNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> securityService.validateNotificacionOwnership(null));
    }

    @Test
    void validateNotificacionOwnership_noEncontrado_lanzaEntityNotFoundException() {
        try (var mocked = mockSecurity(userId)) {
            when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                () -> securityService.validateNotificacionOwnership(99L));
        }
    }

    @Test
    void validateNotificacionOwnership_noEsDelUsuario_lanzaForbiddenException() {
        Notificacion notificacion = new Notificacion();
        notificacion.setIdUsuario(UUID.fromString("00000000-0000-0000-0000-000000000099"));

        try (var mocked = mockSecurity(userId)) {
            when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

            assertThrows(ForbiddenException.class,
                () -> securityService.validateNotificacionOwnership(1L));
        }
    }

    @Test
    void validateNotificacionOwnership_esDelUsuario_noLanzaExcepcion() {
        Notificacion notificacion = new Notificacion();
        notificacion.setIdUsuario(userId);

        try (var mocked = mockSecurity(userId)) {
            when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

            assertDoesNotThrow(() -> securityService.validateNotificacionOwnership(1L));
        }
    }

    // =========================================================
    // validateSolicitudOwnership
    // =========================================================

    @Test
    void validateSolicitudOwnership_idNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> securityService.validateSolicitudOwnership(null));
    }

    @Test
    void validateSolicitudOwnership_noEncontrado_lanzaEntityNotFoundException() {
        try (var mocked = mockSecurity(userId)) {
            when(solicitudPendienteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                () -> securityService.validateSolicitudOwnership(99L));
        }
    }

    @Test
    void validateSolicitudOwnership_noEsDelUsuario_lanzaForbiddenException() {
        Usuario otro = new Usuario();
        otro.setId(UUID.fromString("00000000-0000-0000-0000-000000000099"));

        SolicitudPendienteEspacioTrabajo solicitud = new SolicitudPendienteEspacioTrabajo();
        solicitud.setUsuarioInvitado(otro);

        try (var mocked = mockSecurity(userId)) {
            when(solicitudPendienteRepository.findById(1L)).thenReturn(Optional.of(solicitud));

            assertThrows(ForbiddenException.class,
                () -> securityService.validateSolicitudOwnership(1L));
        }
    }

    @Test
    void validateSolicitudOwnership_esDelUsuario_noLanzaExcepcion() {
        SolicitudPendienteEspacioTrabajo solicitud = new SolicitudPendienteEspacioTrabajo();
        solicitud.setUsuarioInvitado(usuario);

        try (var mocked = mockSecurity(userId)) {
            when(solicitudPendienteRepository.findById(1L)).thenReturn(Optional.of(solicitud));

            assertDoesNotThrow(() -> securityService.validateSolicitudOwnership(1L));
        }
    }

    // =========================================================
    // validateDescuentoOwnership (existing tests preserved)
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

        try (var mocked = mockSecurity(userId)) {
            mockWorkspaceExists(idEspacioTrabajo, userId, true);

            assertDoesNotThrow(() -> securityService.validateDescuentoOwnership(1L));
        }
    }

    @Test
    void validateDescuentoOwnership_sinAcceso_lanzaForbiddenException() {
        Descuento descuento = Descuento.builder()
            .id(1L)
            .idEspacioTrabajo(idEspacioTrabajo)
            .build();

        when(descuentoRepository.findById(1L)).thenReturn(Optional.of(descuento));

        try (var mocked = mockSecurity(userId)) {
            mockWorkspaceExists(idEspacioTrabajo, userId, false);

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

        try (var mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext ctx = mock(SecurityContext.class);
            mocked.when(SecurityContextHolder::getContext).thenReturn(ctx);
            when(ctx.getAuthentication()).thenReturn(null);

            assertThrows(UnauthorizedException.class,
                () -> securityService.validateDescuentoOwnership(1L));
        }
    }

    // =========================================================
    // hasWorkspaceAccess
    // =========================================================

    @Test
    void hasWorkspaceAccess_idNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> securityService.hasWorkspaceAccess(null));
    }

    @Test
    void hasWorkspaceAccess_conAcceso_retornaTrue() {
        try (var mocked = mockSecurity(userId)) {
            mockWorkspaceExists(idEspacioTrabajo, userId, true);

            assertTrue(securityService.hasWorkspaceAccess(idEspacioTrabajo));
        }
    }

    @Test
    void hasWorkspaceAccess_sinAcceso_retornaFalse() {
        try (var mocked = mockSecurity(userId)) {
            mockWorkspaceExists(idEspacioTrabajo, userId, false);

            assertFalse(securityService.hasWorkspaceAccess(idEspacioTrabajo));
        }
    }

    @Test
    void hasWorkspaceAccess_usuarioNoAutenticado_retornaFalse() {
        try (var mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext ctx = mock(SecurityContext.class);
            mocked.when(SecurityContextHolder::getContext).thenReturn(ctx);
            when(ctx.getAuthentication()).thenReturn(null);

            assertFalse(securityService.hasWorkspaceAccess(idEspacioTrabajo));
        }
    }

    // =========================================================
    // isWorkspaceAdmin
    // =========================================================

    @Test
    void isWorkspaceAdmin_idNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> securityService.isWorkspaceAdmin(null));
    }

    @Test
    void isWorkspaceAdmin_espacioNoEncontrado_retornaFalse() {
        try (var mocked = mockSecurity(userId)) {
            when(espacioTrabajoRepository.findById(idEspacioTrabajo)).thenReturn(Optional.empty());

            assertFalse(securityService.isWorkspaceAdmin(idEspacioTrabajo));
        }
    }

    @Test
    void isWorkspaceAdmin_esAdmin_retornaTrue() {
        EspacioTrabajo espacio = new EspacioTrabajo();
        espacio.setUsuarioAdmin(usuario);

        try (var mocked = mockSecurity(userId)) {
            when(espacioTrabajoRepository.findById(idEspacioTrabajo)).thenReturn(Optional.of(espacio));

            assertTrue(securityService.isWorkspaceAdmin(idEspacioTrabajo));
        }
    }

    @Test
    void isWorkspaceAdmin_noEsAdmin_retornaFalse() {
        Usuario admin = new Usuario();
        admin.setId(UUID.fromString("00000000-0000-0000-0000-000000000099"));

        EspacioTrabajo espacio = new EspacioTrabajo();
        espacio.setUsuarioAdmin(admin);

        try (var mocked = mockSecurity(userId)) {
            when(espacioTrabajoRepository.findById(idEspacioTrabajo)).thenReturn(Optional.of(espacio));

            assertFalse(securityService.isWorkspaceAdmin(idEspacioTrabajo));
        }
    }

    @Test
    void isWorkspaceAdmin_usuarioNoAutenticado_retornaFalse() {
        try (var mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext ctx = mock(SecurityContext.class);
            mocked.when(SecurityContextHolder::getContext).thenReturn(ctx);
            when(ctx.getAuthentication()).thenReturn(null);

            assertFalse(securityService.isWorkspaceAdmin(idEspacioTrabajo));
        }
    }
}
