package com.campito.backend.service;

import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dao.SolicitudPendienteEspacioTrabajoRepository;
import com.campito.backend.dao.UsuarioRepository;
import com.campito.backend.dto.EspacioTrabajoDTORequest;
import com.campito.backend.dto.SolicitudPendienteEspacioTrabajoDTOResponse;
import com.campito.backend.exception.UsuarioNoEncontradoException;
import com.campito.backend.mapper.EspacioTrabajoMapper;
import com.campito.backend.mapper.SolicitudPendienteEspacioTrabajoMapper;
import com.campito.backend.mapper.UsuarioMapper;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.ProveedorAutenticacion;
import com.campito.backend.model.SolicitudPendienteEspacioTrabajo;
import com.campito.backend.model.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EspacioTrabajoServiceTest {

    @Mock
    private EspacioTrabajoRepository espacioTrabajoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SolicitudPendienteEspacioTrabajoRepository solicitudPendienteRepository;

    @Mock
    private EspacioTrabajoMapper espacioTrabajoMapper;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private SolicitudPendienteEspacioTrabajoMapper solicitudPendienteEspacioTrabajoMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EspacioTrabajoServiceImpl espacioTrabajoService;

    private Usuario usuarioAdmin;
    private EspacioTrabajo espacioTrabajo;

    @BeforeEach
    void setUp() {
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        usuarioAdmin.setNombre("Admin");
        usuarioAdmin.setEmail("admin@test.com");
        usuarioAdmin.setFotoPerfil("foto.jpg");
        usuarioAdmin.setProveedor(ProveedorAutenticacion.MANUAL);
        usuarioAdmin.setIdProveedor("123");
        usuarioAdmin.setRol("ADMIN");
        usuarioAdmin.setActivo(true);
        usuarioAdmin.setFechaRegistro(LocalDateTime.now());
        usuarioAdmin.setFechaUltimoAcceso(LocalDateTime.now());

        espacioTrabajo = new EspacioTrabajo();
        espacioTrabajo.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        espacioTrabajo.setNombre("Espacio de Prueba");
        espacioTrabajo.setSaldo(0f);
        espacioTrabajo.setUsuarioAdmin(usuarioAdmin);
        espacioTrabajo.setUsuariosParticipantes(new java.util.ArrayList<>());
        espacioTrabajo.getUsuariosParticipantes().add(usuarioAdmin);
        
        lenient().when(espacioTrabajoMapper.toEntity(any(EspacioTrabajoDTORequest.class))).thenAnswer(invocation -> {
            EspacioTrabajo espacio = new EspacioTrabajo();
            espacio.setId(null);
            return espacio;
        });
    }

    // Tests para registrarEspacioTrabajo

    @Test
    void registrarEspacioTrabajo_cuandoDTOEsNulo_entoncesLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            espacioTrabajoService.registrarEspacioTrabajo(null);
        });
        assertEquals("El espacio de trabajo no puede ser nulo", exception.getMessage());
        verify(espacioTrabajoRepository, never()).save(any(EspacioTrabajo.class));
    }

    @Test
    void registrarEspacioTrabajo_cuandoUsuarioAdminNoExiste_entoncesLanzaExcepcion() {
        EspacioTrabajoDTORequest dto = new EspacioTrabajoDTORequest("Nuevo Espacio", UUID.fromString("00000000-0000-0000-0000-000000000099"));
        when(usuarioRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000099"))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            espacioTrabajoService.registrarEspacioTrabajo(dto);
        });
        assertTrue(exception.getMessage().contains("Usuario con ID") && exception.getMessage().contains("no encontrado"));
        verify(espacioTrabajoRepository, never()).save(any(EspacioTrabajo.class));
    }

    @Test
    void registrarEspacioTrabajo_cuandoRegistroExitoso_entoncesGuardaEspacioYUsuarioAdmin() {
        EspacioTrabajoDTORequest dto = new EspacioTrabajoDTORequest("Nuevo Espacio", usuarioAdmin.getId());
        when(usuarioRepository.findById(usuarioAdmin.getId())).thenReturn(Optional.of(usuarioAdmin));
        when(espacioTrabajoRepository.save(any(EspacioTrabajo.class))).thenReturn(espacioTrabajo);

        espacioTrabajoService.registrarEspacioTrabajo(dto);

        verify(usuarioRepository, times(1)).findById(usuarioAdmin.getId());
        verify(espacioTrabajoRepository, times(1)).save(any(EspacioTrabajo.class));
    }

    // Tests para compartirEspacioTrabajo

    @Test
    void compartirEspacioTrabajo_cuandoParametrosSonNulos_entoncesLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            espacioTrabajoService.compartirEspacioTrabajo(null, null);
        });
        assertEquals("El email y el ID del espacio de trabajo no pueden ser nulos", exception.getMessage());
        verify(espacioTrabajoRepository, never()).findById(any(java.util.UUID.class));
    }

    @Test
    void compartirEspacioTrabajo_cuandoEspacioTrabajoNoExiste_entoncesLanzaExcepcion() {
        when(espacioTrabajoRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000099"))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            espacioTrabajoService.compartirEspacioTrabajo("test@test.com", UUID.fromString("00000000-0000-0000-0000-000000000099"));
        });
        assertTrue(exception.getMessage().contains("Espacio de trabajo") && exception.getMessage().contains("no encontrado"));
        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    // Test removido: La validación de permisos del administrador ahora se maneja en la capa de controlador

    @Test
    void compartirEspacioTrabajo_cuandoEmailUsuarioNoExiste_entoncesLanzaExcepcion() {
        when(espacioTrabajoRepository.findById(espacioTrabajo.getId())).thenReturn(Optional.of(espacioTrabajo));
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        UsuarioNoEncontradoException exception = assertThrows(UsuarioNoEncontradoException.class, () -> {
            espacioTrabajoService.compartirEspacioTrabajo("noexiste@test.com", espacioTrabajo.getId());
        });
        assertEquals("No existe ningún usuario registrado con el correo electrónico 'noexiste@test.com'. Por favor, verifica que el correo sea correcto o invita a esa persona a registrarse primero.", exception.getMessage());
        verify(espacioTrabajoRepository, never()).save(any(EspacioTrabajo.class));
    }

    @Test
    void compartirEspacioTrabajo_cuandoCompartidoExitosamente_entoncesCreaSolicitudPendiente() {
        Usuario usuarioACompartir = new Usuario();
        usuarioACompartir.setId(UUID.fromString("00000000-0000-0000-0000-000000000005"));
        usuarioACompartir.setNombre("Compartido");
        usuarioACompartir.setEmail("compartido@test.com");
        usuarioACompartir.setFotoPerfil("foto.jpg");
        usuarioACompartir.setProveedor(ProveedorAutenticacion.MANUAL);
        usuarioACompartir.setIdProveedor("789");
        usuarioACompartir.setRol("USER");
        usuarioACompartir.setActivo(true);
        usuarioACompartir.setFechaRegistro(LocalDateTime.now());
        usuarioACompartir.setFechaUltimoAcceso(LocalDateTime.now());

        when(espacioTrabajoRepository.findById(espacioTrabajo.getId())).thenReturn(Optional.of(espacioTrabajo));
        when(usuarioRepository.findByEmail("compartido@test.com")).thenReturn(Optional.of(usuarioACompartir));
        when(solicitudPendienteRepository.save(any(SolicitudPendienteEspacioTrabajo.class))).thenReturn(new SolicitudPendienteEspacioTrabajo());

        espacioTrabajoService.compartirEspacioTrabajo("compartido@test.com", espacioTrabajo.getId());

        verify(espacioTrabajoRepository, times(1)).findById(espacioTrabajo.getId());
        verify(usuarioRepository, times(1)).findByEmail("compartido@test.com");
        verify(solicitudPendienteRepository, times(1)).save(any(SolicitudPendienteEspacioTrabajo.class));
        verify(eventPublisher, times(1)).publishEvent(any());
        assertFalse(espacioTrabajo.getUsuariosParticipantes().contains(usuarioACompartir));
    }

    // Tests para respuestaSolicitudCompartirEspacioTrabajo

    @Test
    void respuestaSolicitudCompartir_cuandoParametrosSonNulos_entoncesLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            espacioTrabajoService.respuestaSolicitudCompartirEspacioTrabajo(null, null);
        });
        assertEquals("El ID de la solicitud y la respuesta no pueden ser nulos", exception.getMessage());
        verify(solicitudPendienteRepository, never()).findById(any());
    }

    @Test
    void respuestaSolicitudCompartir_cuandoSolicitudNoExiste_entoncesLanzaExcepcion() {
        when(solicitudPendienteRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            espacioTrabajoService.respuestaSolicitudCompartirEspacioTrabajo(999L, true);
        });
        assertTrue(exception.getMessage().contains("Solicitud de compartir espacio de trabajo") && exception.getMessage().contains("no encontrada"));
        verify(solicitudPendienteRepository, never()).delete(any());
    }

    @Test
    void respuestaSolicitudCompartir_cuandoRechazada_entoncesEliminaSolicitudSinAgregarUsuario() {
        Usuario usuarioInvitado = new Usuario();
        usuarioInvitado.setId(UUID.fromString("00000000-0000-0000-0000-000000000006"));
        usuarioInvitado.setEmail("invitado@test.com");
        usuarioInvitado.setNombre("Invitado");

        SolicitudPendienteEspacioTrabajo solicitud = SolicitudPendienteEspacioTrabajo.builder()
            .id(1L)
            .espacioTrabajo(espacioTrabajo)
            .usuarioInvitado(usuarioInvitado)
            .fechaCreacion(LocalDateTime.now())
            .build();

        when(solicitudPendienteRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        espacioTrabajoService.respuestaSolicitudCompartirEspacioTrabajo(1L, false);

        verify(solicitudPendienteRepository, times(1)).findById(1L);
        verify(solicitudPendienteRepository, times(1)).delete(solicitud);
        verify(espacioTrabajoRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
        assertFalse(espacioTrabajo.getUsuariosParticipantes().contains(usuarioInvitado));
    }

    @Test
    void respuestaSolicitudCompartir_cuandoAceptada_entoncesAgregaUsuarioYEliminaSolicitud() {
        Usuario usuarioInvitado = new Usuario();
        usuarioInvitado.setId(UUID.fromString("00000000-0000-0000-0000-000000000007"));
        usuarioInvitado.setEmail("invitado@test.com");
        usuarioInvitado.setNombre("Invitado");

        SolicitudPendienteEspacioTrabajo solicitud = SolicitudPendienteEspacioTrabajo.builder()
            .id(2L)
            .espacioTrabajo(espacioTrabajo)
            .usuarioInvitado(usuarioInvitado)
            .fechaCreacion(LocalDateTime.now())
            .build();

        when(solicitudPendienteRepository.findById(2L)).thenReturn(Optional.of(solicitud));
        when(espacioTrabajoRepository.save(any(EspacioTrabajo.class))).thenReturn(espacioTrabajo);

        espacioTrabajoService.respuestaSolicitudCompartirEspacioTrabajo(2L, true);

        verify(solicitudPendienteRepository, times(1)).findById(2L);
        verify(espacioTrabajoRepository, times(1)).save(espacioTrabajo);
        verify(solicitudPendienteRepository, times(1)).delete(solicitud);
        verify(eventPublisher, times(1)).publishEvent(any());
        assertTrue(espacioTrabajo.getUsuariosParticipantes().contains(usuarioInvitado));
    }

    // Tests para listarSolicitudesPendientes

    @Test
    void listarSolicitudesPendientes_cuandoIdUsuarioEsNulo_entoncesLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            espacioTrabajoService.listarSolicitudesPendientes(null);
        });
        assertEquals("El ID del usuario no puede ser nulo", exception.getMessage());
        verify(solicitudPendienteRepository, never()).findByUsuarioInvitado_Id(any());
    }

    @Test
    void listarSolicitudesPendientes_cuandoNoHaySolicitudes_entoncesRetornaListaVacia() {
        UUID idUsuario = UUID.fromString("00000000-0000-0000-0000-000000000008");
        when(solicitudPendienteRepository.findByUsuarioInvitado_Id(idUsuario)).thenReturn(Arrays.asList());

        List<SolicitudPendienteEspacioTrabajoDTOResponse> resultado = espacioTrabajoService.listarSolicitudesPendientes(idUsuario);

        verify(solicitudPendienteRepository, times(1)).findByUsuarioInvitado_Id(idUsuario);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void listarSolicitudesPendientes_cuandoHaySolicitudes_entoncesRetornaListaMapeada() {
        UUID idUsuario = UUID.fromString("00000000-0000-0000-0000-000000000009");
        Usuario usuarioInvitado = new Usuario();
        usuarioInvitado.setId(idUsuario);
        usuarioInvitado.setNombre("Usuario Invitado");

        SolicitudPendienteEspacioTrabajo solicitud1 = SolicitudPendienteEspacioTrabajo.builder()
            .id(1L)
            .espacioTrabajo(espacioTrabajo)
            .usuarioInvitado(usuarioInvitado)
            .fechaCreacion(LocalDateTime.now())
            .build();

        SolicitudPendienteEspacioTrabajo solicitud2 = SolicitudPendienteEspacioTrabajo.builder()
            .id(2L)
            .espacioTrabajo(espacioTrabajo)
            .usuarioInvitado(usuarioInvitado)
            .fechaCreacion(LocalDateTime.now())
            .build();

        List<SolicitudPendienteEspacioTrabajo> solicitudes = Arrays.asList(solicitud1, solicitud2);
        
        SolicitudPendienteEspacioTrabajoDTOResponse dto1 = new SolicitudPendienteEspacioTrabajoDTOResponse(
            1L, "Espacio de Prueba", "Admin", "foto.jpg", LocalDateTime.now()
        );
        SolicitudPendienteEspacioTrabajoDTOResponse dto2 = new SolicitudPendienteEspacioTrabajoDTOResponse(
            2L, "Espacio de Prueba", "Admin", "foto.jpg", LocalDateTime.now()
        );

        when(solicitudPendienteRepository.findByUsuarioInvitado_Id(idUsuario)).thenReturn(solicitudes);
        when(solicitudPendienteEspacioTrabajoMapper.toResponse(solicitud1)).thenReturn(dto1);
        when(solicitudPendienteEspacioTrabajoMapper.toResponse(solicitud2)).thenReturn(dto2);

        List<SolicitudPendienteEspacioTrabajoDTOResponse> resultado = espacioTrabajoService.listarSolicitudesPendientes(idUsuario);

        verify(solicitudPendienteRepository, times(1)).findByUsuarioInvitado_Id(idUsuario);
        verify(solicitudPendienteEspacioTrabajoMapper, times(2)).toResponse(any());
        assertEquals(2, resultado.size());
        assertEquals(dto1, resultado.get(0));
        assertEquals(dto2, resultado.get(1));
    }

    @Test
    void compartirEspacioTrabajo_cuandoYaExisteSolicitudPendiente_entoncesLanzaEntidadDuplicadaException() {
        // Arrange
        String email = "compartido@test.com";
        UUID idEspacioTrabajo = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID idUsuarioInvitado = UUID.fromString("00000000-0000-0000-0000-000000000005");

        Usuario usuarioInvitado = new Usuario();
        usuarioInvitado.setId(idUsuarioInvitado);
        usuarioInvitado.setEmail(email);
        usuarioInvitado.setNombre("Usuario Compartido");

        when(espacioTrabajoRepository.findById(idEspacioTrabajo)).thenReturn(Optional.of(espacioTrabajo));
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioInvitado));
        when(solicitudPendienteRepository.existsByEspacioTrabajo_IdAndUsuarioInvitado_Id(
            idEspacioTrabajo, idUsuarioInvitado
        )).thenReturn(true); // Ya existe una solicitud pendiente

        // Act & Assert
        com.campito.backend.exception.EntidadDuplicadaException exception = 
            assertThrows(com.campito.backend.exception.EntidadDuplicadaException.class, () -> {
                espacioTrabajoService.compartirEspacioTrabajo(email, idEspacioTrabajo);
            });

        // Verificar que el mensaje de error es amigable
        assertTrue(exception.getMessage().contains("Ya existe una invitación pendiente"));
        assertTrue(exception.getMessage().contains(email));
        assertTrue(exception.getMessage().contains(espacioTrabajo.getNombre()));

        // Verificar que NO se intentó guardar la solicitud
        verify(solicitudPendienteRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}