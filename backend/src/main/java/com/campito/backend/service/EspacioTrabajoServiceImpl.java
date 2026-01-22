package com.campito.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dao.UsuarioRepository;
import com.campito.backend.dto.EspacioTrabajoDTORequest;
import com.campito.backend.dto.EspacioTrabajoDTOResponse;
import com.campito.backend.dto.UsuarioDTOResponse;
import com.campito.backend.mapper.EspacioTrabajoMapper;
import com.campito.backend.mapper.UsuarioMapper;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.Usuario;
import com.campito.backend.exception.UsuarioNoEncontradoException;
import com.campito.backend.exception.EntidadDuplicadaException;
import com.campito.backend.exception.PermisosDenegadosException;

import java.util.Optional;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio para gestión de espacios de trabajo.
 * 
 * Proporciona métodos para registrar espacios de trabajo, compartirlos,
 * y listar espacios de trabajo por usuario.
 */
@Service
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
public class EspacioTrabajoServiceImpl implements EspacioTrabajoService {

    private static final Logger logger = LoggerFactory.getLogger(EspacioTrabajoServiceImpl.class);

    private final EspacioTrabajoRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspacioTrabajoMapper espacioTrabajoMapper;
    private final UsuarioMapper usuarioMapper;

    /**
     * Registra un nuevo espacio de trabajo.
     * 
     * @param espacioTrabajoDTO Datos del espacio de trabajo a registrar.
     * @throws IllegalArgumentException si el espacio de trabajo es nulo.
     * @throws EntityNotFoundException si el usuario administrador no se encuentra en la base de datos.
     */
    @Override
    @Transactional
    public void registrarEspacioTrabajo(EspacioTrabajoDTORequest espacioTrabajoDTO) {

        if(espacioTrabajoDTO == null) {
            logger.warn("Intento de registrar un EspacioTrabajoDTO nulo.");
            throw new IllegalArgumentException("El espacio de trabajo no puede ser nulo");
        }

        logger.info("Intentando registrar un nuevo espacio de trabajo con nombre: '{}'", espacioTrabajoDTO.nombre());
        // Validar que no exista un espacio con el mismo nombre para el mismo usuario administrador
        Optional<EspacioTrabajo> espacioExistente = espacioRepository
                .findFirstByNombreAndUsuarioAdmin_Id(espacioTrabajoDTO.nombre(), espacioTrabajoDTO.idUsuarioAdmin());
        
        if (espacioExistente.isPresent()) {
            String msg = String.format("Ya existe un espacio de trabajo con el nombre '%s' creado por ti. Por favor, utiliza un nombre diferente.", 
                    espacioTrabajoDTO.nombre());
            logger.warn(msg);
            throw new EntidadDuplicadaException(msg);
        }
        Usuario usuario = usuarioRepository.findById(espacioTrabajoDTO.idUsuarioAdmin()).orElseThrow(() -> {
            String mensaje = "Usuario con ID " + espacioTrabajoDTO.idUsuarioAdmin() + " no encontrado";
            logger.warn(mensaje);
            return new EntityNotFoundException(mensaje);
        });

        EspacioTrabajo espacioTrabajo = espacioTrabajoMapper.toEntity(espacioTrabajoDTO);
        espacioTrabajo.setSaldo(0f);
        espacioTrabajo.setUsuarioAdmin(usuario);
        espacioTrabajo.setUsuariosParticipantes(new ArrayList<>());
        espacioTrabajo.getUsuariosParticipantes().add(usuario);
        espacioRepository.save(espacioTrabajo);
        logger.info("Espacio de trabajo '{}' registrado exitosamente.", espacioTrabajo.getNombre());
    }

    /**
     * Comparte un espacio de trabajo con otro usuario mediante su email.
     * 
     * @param email Email del usuario con quien se compartirá el espacio.
     * @param idEspacioTrabajo ID del espacio de trabajo a compartir.
     * @param idUsuarioAdmin ID del usuario administrador que realiza la acción.
     * @throws IllegalArgumentException si alguno de los parámetros es nulo.
     * @throws EntityNotFoundException si el espacio de trabajo o el usuario no se encuentran en la base de datos.
     * @throws PermisosDenegadosException si el usuario no es el administrador del espacio de trabajo.
     */
    @Override
    @Transactional
    public void compartirEspacioTrabajo(String email, Long idEspacioTrabajo, Long idUsuarioAdmin) {
        
        if(email == null || idEspacioTrabajo == null || idUsuarioAdmin == null) {
            logger.warn("Se recibieron parametros nulos para compartir. Email: {}, EspacioID: {}, AdminID: {}", email, idEspacioTrabajo, idUsuarioAdmin);
            throw new IllegalArgumentException("El email, el ID del espacio de trabajo y el ID del usuario administrador del espacio no pueden ser nulos");
        }
        logger.info("Intentando compartir espacio de trabajo ID: {} con email: {}", idEspacioTrabajo, email);

        EspacioTrabajo espacioTrabajo = espacioRepository.findById(idEspacioTrabajo).orElseThrow(() -> {
            String mensaje = "Espacio de trabajo con ID " + idEspacioTrabajo + " no encontrado";
            logger.warn(mensaje);
            return new EntityNotFoundException(mensaje);
        });

        if(!espacioTrabajo.getUsuarioAdmin().getId().equals(idUsuarioAdmin)) {
            logger.warn("Intento no autorizado del usuario ID: {} para compartir el espacio ID: {}.", idUsuarioAdmin, idEspacioTrabajo);
            throw new PermisosDenegadosException("No tienes permiso para compartir este espacio de trabajo. Solo el administrador puede realizar esta acción.");
        }

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> {
            String mensajeLog = "Usuario con email " + email + " no encontrado";
            logger.warn(mensajeLog);
            String mensajeUsuario = "No existe ningún usuario registrado con el correo electrónico '" + email + "'. Por favor, verifica que el correo sea correcto o invita a esa persona a registrarse primero.";
            return new UsuarioNoEncontradoException(mensajeUsuario);
        });

        espacioTrabajo.getUsuariosParticipantes().add(usuario);

        espacioRepository.save(espacioTrabajo);
        logger.info("Espacio de trabajo ID: {} compartido exitosamente con {}.", idEspacioTrabajo, email);
    }

    /**
     * Lista los espacios de trabajo asociados a un usuario.
     * 
     * @param idUsuario ID del usuario cuyos espacios se desean listar.
     * @return Lista de espacios de trabajo en formato DTO.
     * @throws IllegalArgumentException si el ID del usuario es nulo.
     */
    @Override
    public List<EspacioTrabajoDTOResponse> listarEspaciosTrabajoPorUsuario(Long idUsuario) {
        
        if(idUsuario == null) {
            logger.warn("Se recibieron parametros nulos para listar espacios de trabajo por usuario.");
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        logger.info("Intentando listar espacios de trabajo para el usuario ID: {}", idUsuario);

        List<EspacioTrabajo> espacios = espacioRepository.findByUsuariosParticipantes_IdOrderByFechaModificacionDesc(idUsuario);
        logger.info("Encontrados {} espacios de trabajo para el usuario ID: {} (ordenados por última modificación).", espacios.size(), idUsuario);
        return espacios.stream()
            .map(espacioTrabajoMapper::toResponse)
            .toList();
    }

    /**
     * Obtiene la lista de miembros (usuarios participantes) de un espacio de trabajo.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo.
     * @return Lista de usuarios en formato DTO.
     * @throws EntityNotFoundException si el espacio de trabajo no se encuentra.
     * @throws IllegalArgumentException si el ID del espacio de trabajo es nulo.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTOResponse> obtenerMiembrosEspacioTrabajo(Long idEspacioTrabajo) {
        
        if(idEspacioTrabajo == null) {
            logger.warn("Se recibieron parametros nulos para obtener miembros del espacio de trabajo.");
            throw new IllegalArgumentException("El ID del espacio de trabajo no puede ser nulo");
        }
        logger.info("Intentando obtener miembros del espacio de trabajo ID: {}", idEspacioTrabajo);

        EspacioTrabajo espacioTrabajo = espacioRepository.findById(idEspacioTrabajo)
            .orElseThrow(() -> {
                String mensaje = "Espacio de trabajo con ID " + idEspacioTrabajo + " no encontrado";
                logger.warn(mensaje);
                return new EntityNotFoundException(mensaje);
            });
        
        List<Usuario> miembros = espacioTrabajo.getUsuariosParticipantes();
        logger.info("Encontrados {} miembros en el espacio de trabajo ID: {}.", miembros.size(), idEspacioTrabajo);
        
        return miembros.stream()
            .map(usuarioMapper::toResponse)
            .toList();
    }

}
