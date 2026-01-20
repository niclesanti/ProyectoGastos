package com.campito.backend.service;

import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dao.UsuarioRepository;
import com.campito.backend.dto.EspacioTrabajoDTORequest;
import com.campito.backend.exception.UsuarioNoEncontradoException;
import com.campito.backend.mapper.EspacioTrabajoMapper;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.ProveedorAutenticacion;
import com.campito.backend.model.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

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
    private EspacioTrabajoMapper espacioTrabajoMapper;

    @InjectMocks
    private EspacioTrabajoServiceImpl espacioTrabajoService;

    private Usuario usuarioAdmin;
    private EspacioTrabajo espacioTrabajo;

    @BeforeEach
    void setUp() {
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
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
        espacioTrabajo.setId(1L);
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
        EspacioTrabajoDTORequest dto = new EspacioTrabajoDTORequest("Nuevo Espacio", 99L);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            espacioTrabajoService.registrarEspacioTrabajo(dto);
        });
        assertEquals("Usuario con ID 99 no encontrado", exception.getMessage());
        verify(espacioTrabajoRepository, never()).save(any(EspacioTrabajo.class));
    }

    @Test
    void registrarEspacioTrabajo_cuandoRegistroExitoso_entoncesGuardaEspacioYUsuarioAdmin() {
        EspacioTrabajoDTORequest dto = new EspacioTrabajoDTORequest("Nuevo Espacio", 1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAdmin));
        when(espacioTrabajoRepository.save(any(EspacioTrabajo.class))).thenReturn(espacioTrabajo);

        espacioTrabajoService.registrarEspacioTrabajo(dto);

        verify(usuarioRepository, times(1)).findById(1L);
        verify(espacioTrabajoRepository, times(1)).save(any(EspacioTrabajo.class));
    }

    // Tests para compartirEspacioTrabajo

    @Test
    void compartirEspacioTrabajo_cuandoParametrosSonNulos_entoncesLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            espacioTrabajoService.compartirEspacioTrabajo(null, 1L, 1L);
        });
        assertEquals("El email, el ID del espacio de trabajo y el ID del usuario administrador del espacio no pueden ser nulos", exception.getMessage());
        verify(espacioTrabajoRepository, never()).findById(anyLong());
    }

    @Test
    void compartirEspacioTrabajo_cuandoEspacioTrabajoNoExiste_entoncesLanzaExcepcion() {
        when(espacioTrabajoRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            espacioTrabajoService.compartirEspacioTrabajo("test@test.com", 99L, 1L);
        });
        assertEquals("Espacio de trabajo con ID 99 no encontrado", exception.getMessage());
        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    @Test
    void compartirEspacioTrabajo_cuandoUsuarioAdminNoEsAdminDelEspacio_entoncesLanzaExcepcion() {
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(2L);
        otroUsuario.setNombre("Otro");
        otroUsuario.setEmail("otro@test.com");
        otroUsuario.setFotoPerfil("foto.jpg");
        otroUsuario.setProveedor(ProveedorAutenticacion.MANUAL);
        otroUsuario.setIdProveedor("456");
        otroUsuario.setRol("USER");
        otroUsuario.setActivo(true);
        otroUsuario.setFechaRegistro(LocalDateTime.now());
        otroUsuario.setFechaUltimoAcceso(LocalDateTime.now());
        
        EspacioTrabajo otroEspacio = new EspacioTrabajo();
        otroEspacio.setId(2L);
        otroEspacio.setNombre("Otro Espacio");
        otroEspacio.setSaldo(0f);
        otroEspacio.setUsuarioAdmin(otroUsuario);

        when(espacioTrabajoRepository.findById(1L)).thenReturn(Optional.of(otroEspacio));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            espacioTrabajoService.compartirEspacioTrabajo("test@test.com", 1L, 1L);
        });
        assertEquals("El usuario administrador no tiene permiso para compartir este espacio de trabajo", exception.getMessage());
        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    @Test
    void compartirEspacioTrabajo_cuandoEmailUsuarioNoExiste_entoncesLanzaExcepcion() {
        when(espacioTrabajoRepository.findById(1L)).thenReturn(Optional.of(espacioTrabajo));
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        UsuarioNoEncontradoException exception = assertThrows(UsuarioNoEncontradoException.class, () -> {
            espacioTrabajoService.compartirEspacioTrabajo("noexiste@test.com", 1L, 1L);
        });
        assertEquals("No existe ningún usuario registrado con el correo electrónico 'noexiste@test.com'. Por favor, verifica que el correo sea correcto o invita a esa persona a registrarse primero.", exception.getMessage());
        verify(espacioTrabajoRepository, never()).save(any(EspacioTrabajo.class));
    }

    @Test
    void compartirEspacioTrabajo_cuandoCompartidoExitosamente_entoncesGuardaEspacio() {
        Usuario usuarioACompartir = new Usuario();
        usuarioACompartir.setId(3L);
        usuarioACompartir.setNombre("Compartido");
        usuarioACompartir.setEmail("compartido@test.com");
        usuarioACompartir.setFotoPerfil("foto.jpg");
        usuarioACompartir.setProveedor(ProveedorAutenticacion.MANUAL);
        usuarioACompartir.setIdProveedor("789");
        usuarioACompartir.setRol("USER");
        usuarioACompartir.setActivo(true);
        usuarioACompartir.setFechaRegistro(LocalDateTime.now());
        usuarioACompartir.setFechaUltimoAcceso(LocalDateTime.now());

        when(espacioTrabajoRepository.findById(1L)).thenReturn(Optional.of(espacioTrabajo));
        when(usuarioRepository.findByEmail("compartido@test.com")).thenReturn(Optional.of(usuarioACompartir));
        when(espacioTrabajoRepository.save(any(EspacioTrabajo.class))).thenReturn(espacioTrabajo);

        espacioTrabajoService.compartirEspacioTrabajo("compartido@test.com", 1L, 1L);

        verify(espacioTrabajoRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).findByEmail("compartido@test.com");
        verify(espacioTrabajoRepository, times(1)).save(any(EspacioTrabajo.class));
        assertTrue(espacioTrabajo.getUsuariosParticipantes().contains(usuarioACompartir));
    }
}