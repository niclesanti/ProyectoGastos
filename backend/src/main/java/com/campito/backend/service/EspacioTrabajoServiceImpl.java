package com.campito.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dao.UsuarioRepository;
import com.campito.backend.dto.EspacioTrabajoDTORequest;
import com.campito.backend.dto.EspacioTrabajoDTOResponse;
import com.campito.backend.mapper.EspacioTrabajoMapper;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.Usuario;

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

    /**
     * Registra un nuevo espacio de trabajo.
     * 
     * @param espacioTrabajoDTO Datos del espacio de trabajo a registrar.
     * @throws IllegalArgumentException si el espacio de trabajo es nulo.
     * @throws EntityNotFoundException si el usuario administrador no se encuentra en la base de datos.
     * @throws Exception para cualquier otro error inesperado.
     */
    @Override
    @Transactional
    public void registrarEspacioTrabajo(EspacioTrabajoDTORequest espacioTrabajoDTO) {

        if(espacioTrabajoDTO == null) {
            logger.warn("Intento de registrar un EspacioTrabajoDTO nulo.");
            throw new IllegalArgumentException("El espacio de trabajo no puede ser nulo");
        }

        logger.info("Intentando registrar un nuevo espacio de trabajo con nombre: '{}'", espacioTrabajoDTO.nombre());
        try {
            Usuario usuario = usuarioRepository.findById(espacioTrabajoDTO.idUsuarioAdmin()).orElseThrow(() -> {
                String mensaje = "Usuario con ID " + espacioTrabajoDTO.idUsuarioAdmin() + " no encontrado";
                logger.warn(mensaje);
                return new EntityNotFoundException(mensaje);
            });

            EspacioTrabajo espacioTrabajo = espacioTrabajoMapper.toEntity(espacioTrabajoDTO);
            espacioTrabajo.setSaldo(0f);
            espacioTrabajo.setUsuarioAdmin(usuario);
            espacioTrabajo.setUsuariosParticipantes(new java.util.ArrayList<>());
            espacioTrabajo.getUsuariosParticipantes().add(usuario);
            espacioRepository.save(espacioTrabajo);
            logger.info("Espacio de trabajo '{}' registrado exitosamente.", espacioTrabajo.getNombre());
        } catch (Exception e) {
            logger.error("Error inesperado al registrar espacio de trabajo con nombre: '{}'", espacioTrabajoDTO.nombre(), e);
            throw e;
        }
    }

    /**
     * Comparte un espacio de trabajo con otro usuario mediante su email.
     * 
     * @param email Email del usuario con quien se compartirá el espacio.
     * @param idEspacioTrabajo ID del espacio de trabajo a compartir.
     * @param idUsuarioAdmin ID del usuario administrador que realiza la acción.
     * @throws IllegalArgumentException si alguno de los parámetros es nulo o si el usuario administrador no tiene permiso.
     * @throws EntityNotFoundException si el espacio de trabajo o el usuario no se encuentran en la base de datos.
     * @throws Exception para cualquier otro error inesperado.
     */
    @Override
    @Transactional
    public void compartirEspacioTrabajo(String email, Long idEspacioTrabajo, Long idUsuarioAdmin) {
        logger.info("Intentando compartir espacio de trabajo ID: {} con email: {}", idEspacioTrabajo, email);
        try {
            if(email == null || idEspacioTrabajo == null || idUsuarioAdmin == null) {
                logger.warn("Se recibieron parametros nulos para compartir. Email: {}, EspacioID: {}, AdminID: {}", email, idEspacioTrabajo, idUsuarioAdmin);
                throw new IllegalArgumentException("El email, el ID del espacio de trabajo y el ID del usuario administrador del espacio no pueden ser nulos");
            }

            EspacioTrabajo espacioTrabajo = espacioRepository.findById(idEspacioTrabajo).orElseThrow(() -> {
                String mensaje = "Espacio de trabajo con ID " + idEspacioTrabajo + " no encontrado";
                logger.warn(mensaje);
                return new EntityNotFoundException(mensaje);
            });

            if(!espacioTrabajo.getUsuarioAdmin().getId().equals(idUsuarioAdmin)) {
                logger.warn("Intento no autorizado del usuario ID: {} para compartir el espacio ID: {}.", idUsuarioAdmin, idEspacioTrabajo);
                throw new IllegalArgumentException("El usuario administrador no tiene permiso para compartir este espacio de trabajo");
            }

            Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> {
                String mensaje = "Usuario con email " + email + " no encontrado";
                logger.warn(mensaje);
                return new EntityNotFoundException(mensaje);
            });

            espacioTrabajo.getUsuariosParticipantes().add(usuario);

            espacioRepository.save(espacioTrabajo);
            logger.info("Espacio de trabajo ID: {} compartido exitosamente con {}.", idEspacioTrabajo, email);
        } catch (Exception e) {
            logger.error("Error al compartir espacio de trabajo ID: {} con email: {}. Causa: {}", idEspacioTrabajo, email, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lista los espacios de trabajo asociados a un usuario.
     * 
     * @param idUsuario ID del usuario cuyos espacios se desean listar.
     * @return Lista de espacios de trabajo en formato DTO.
     * @throws Exception para cualquier error inesperado.
     */
    @Override
    public List<EspacioTrabajoDTOResponse> listarEspaciosTrabajoPorUsuario(Long idUsuario) {
        logger.info("Intentando listar espacios de trabajo para el usuario ID: {}", idUsuario);
        try {
            List<EspacioTrabajo> espacios = espacioRepository.findByUsuariosParticipantes_Id(idUsuario);
            logger.info("Encontrados {} espacios de trabajo para el usuario ID: {}.", espacios.size(), idUsuario);
            return espacios.stream()
                .map(espacioTrabajoMapper::toResponse)
                .toList();
        } catch (Exception e) {
            logger.error("Error al listar espacios de trabajo para el usuario ID: {}. Causa: {}", idUsuario, e.getMessage(), e);
            throw e;
        }
    }

}
