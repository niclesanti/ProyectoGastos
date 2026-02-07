package com.campito.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dao.SolicitudPendienteEspacioTrabajoRepository;
import com.campito.backend.dao.UsuarioRepository;
import com.campito.backend.dto.EspacioTrabajoDTORequest;
import com.campito.backend.dto.EspacioTrabajoDTOResponse;
import com.campito.backend.dto.UsuarioDTOResponse;
import com.campito.backend.dto.SolicitudPendienteEspacioTrabajoDTOResponse;
import com.campito.backend.event.NotificacionEvent;
import com.campito.backend.mapper.EspacioTrabajoMapper;
import com.campito.backend.mapper.SolicitudPendienteEspacioTrabajoMapper;
import com.campito.backend.mapper.UsuarioMapper;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.TipoNotificacion;
import com.campito.backend.model.Usuario;
import com.campito.backend.model.SolicitudPendienteEspacioTrabajo;
import com.campito.backend.exception.UsuarioNoEncontradoException;
import com.campito.backend.exception.EntidadDuplicadaException;

import java.util.Optional;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

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
    private final SolicitudPendienteEspacioTrabajoRepository solicitudPendienteRepository;
    private final EspacioTrabajoMapper espacioTrabajoMapper;
    private final UsuarioMapper usuarioMapper;
    private final SolicitudPendienteEspacioTrabajoMapper solicitudPendienteEspacioTrabajoMapper;
    private final ApplicationEventPublisher eventPublisher;

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
     * @throws IllegalArgumentException si alguno de los parámetros es nulo.
     * @throws EntityNotFoundException si el espacio de trabajo o el usuario no se encuentran en la base de datos.
     */
    @Override
    @Transactional
    public void compartirEspacioTrabajo(String email, UUID idEspacioTrabajo) {
        
        if(email == null || idEspacioTrabajo == null) {
            logger.warn("Se recibieron parametros nulos para compartir. Email: {}, EspacioID: {}", email, idEspacioTrabajo);
            throw new IllegalArgumentException("El email y el ID del espacio de trabajo no pueden ser nulos");
        }
        logger.info("Intentando compartir espacio de trabajo ID: {} con email: {}", idEspacioTrabajo, email);

        EspacioTrabajo espacioTrabajo = espacioRepository.findById(idEspacioTrabajo).orElseThrow(() -> {
            String mensaje = "Espacio de trabajo con ID " + idEspacioTrabajo + " no encontrado";
            logger.warn(mensaje);
            return new EntityNotFoundException(mensaje);
        });

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> {
            String mensajeLog = "Usuario con email " + email + " no encontrado";
            logger.warn(mensajeLog);
            String mensajeUsuario = "No existe ningún usuario registrado con el correo electrónico '" + email + "'. Por favor, verifica que el correo sea correcto o invita a esa persona a registrarse primero.";
            return new UsuarioNoEncontradoException(mensajeUsuario);
        });

        SolicitudPendienteEspacioTrabajo solicitud = SolicitudPendienteEspacioTrabajo.builder()
            .espacioTrabajo(espacioTrabajo)
            .usuarioInvitado(usuario)
            .build();
        
        solicitudPendienteRepository.save(solicitud);
        logger.info("Solicitud de compartir espacio de trabajo ID: {} creada para el usuario {} (email: {}).", 
                    idEspacioTrabajo, usuario.getId(), email);
        
        // Emitir evento de notificación al usuario invitado
        try {
            String nombreAdmin = espacioTrabajo.getUsuarioAdmin().getNombre();
            String mensaje = String.format("%s te invitó a unirte al espacio de trabajo: '%s'", 
                                            nombreAdmin, espacioTrabajo.getNombre());
            eventPublisher.publishEvent(new NotificacionEvent(
                this,
                usuario.getId(),
                TipoNotificacion.INVITACION_ESPACIO,
                mensaje
            ));
            logger.info("Evento de notificación enviado al usuario {} por invitación al espacio {}", 
                       usuario.getId(), idEspacioTrabajo);
        } catch (Exception e) {
            logger.error("Error al enviar notificación de invitación al usuario {} para espacio ID: {}", 
                        usuario.getId(), idEspacioTrabajo, e);
            // No propagamos la excepción para no afectar el compartir del espacio que ya fue guardado exitosamente
        }
    }

    /**
     * Responde a una solicitud pendiente de compartir un espacio de trabajo.
     * 
     * @param idSolicitud ID de la solicitud pendiente.
     * @param aceptada Indica si la solicitud fue aceptada o rechazada.
     * @throws IllegalArgumentException si alguno de los parámetros es nulo.
     * @throws EntityNotFoundException si la solicitud pendiente no se encuentra en la base de datos.
     */
    @Override
    public void respuestaSolicitudCompartirEspacioTrabajo(Long idSolicitud, Boolean aceptada) {
        // Implementación pendiente
        if (idSolicitud == null || aceptada == null) {
            logger.warn("Se recibieron parametros nulos para responder solicitud de compartir espacio.");
            throw new IllegalArgumentException("El ID de la solicitud y la respuesta no pueden ser nulos");
        }
        logger.info("Intentando responder solicitud de compartir espacio ID: {} con respuesta: {}", idSolicitud, aceptada);

        SolicitudPendienteEspacioTrabajo solicitud = solicitudPendienteRepository.findById(idSolicitud)
            .orElseThrow(() -> {
                String mensaje = "Solicitud de compartir espacio de trabajo con ID " + idSolicitud + " no encontrada";
                logger.warn(mensaje);
                return new EntityNotFoundException(mensaje);
            });
        if (aceptada) {
            EspacioTrabajo espacioTrabajo = solicitud.getEspacioTrabajo();
            Usuario usuario = solicitud.getUsuarioInvitado();

            espacioTrabajo.getUsuariosParticipantes().add(usuario);
            espacioRepository.save(espacioTrabajo);
            logger.info("Solicitud de compartir espacio ID: {} aceptada. Usuario {} agregado al espacio ID: {}.", 
                        idSolicitud, usuario.getEmail(), espacioTrabajo.getId());
            
            // Emitir evento de notificación al usuario administrador del espacio
            try {
                String mensaje = String.format("%s ha aceptado tu invitación para unirse al espacio de trabajo: '%s'", 
                                                usuario.getNombre(), espacioTrabajo.getNombre());
                eventPublisher.publishEvent(new NotificacionEvent(
                    this,
                    espacioTrabajo.getUsuarioAdmin().getId(),
                    TipoNotificacion.MIEMBRO_AGREGADO,
                    mensaje
                ));
                logger.info("Evento de notificación enviado al administrador {} por aceptación de invitación al espacio {}", 
                           espacioTrabajo.getUsuarioAdmin().getId(), espacioTrabajo.getId());
            } catch (Exception e) {
                logger.error("Error al enviar notificación de aceptación al administrador {} para espacio ID: {}", 
                            espacioTrabajo.getUsuarioAdmin().getId(), espacioTrabajo.getId(), e);
                // No propagamos la excepción para no afectar la respuesta a la solicitud que ya fue procesada exitosamente
            }
        }

        solicitudPendienteRepository.delete(solicitud);
        logger.info("Solicitud de compartir espacio ID: {} eliminada de solicitudes pendientes.", idSolicitud);
    }

    /**
     * Lista los espacios de trabajo asociados a un usuario.
     * 
     * @param idUsuario ID del usuario cuyos espacios se desean listar.
     * @return Lista de espacios de trabajo en formato DTO.
     * @throws IllegalArgumentException si el ID del usuario es nulo.
     */
    @Override
    public List<EspacioTrabajoDTOResponse> listarEspaciosTrabajoPorUsuario(UUID idUsuario) {
        
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
    public List<UsuarioDTOResponse> obtenerMiembrosEspacioTrabajo(UUID idEspacioTrabajo) {
        
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

    /**
     * Lista las solicitudes pendientes para integrar espacios de trabajo de un usuario.
     * 
     * @param idUsuario ID del usuario para listar sus solicitudes pendientes.
     * @return Lista de solicitudes pendientes en formato DTO.
     * @throws IllegalArgumentException si el ID del usuario es nulo.
     */
    @Override
    public List<SolicitudPendienteEspacioTrabajoDTOResponse> listarSolicitudesPendientes(UUID idUsuario) {
        if(idUsuario == null) {
            logger.warn("Se recibieron parametros nulos para listar solicitudes pendientes de espacios de trabajo.");
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        logger.info("Intentando listar solicitudes pendientes de espacios de trabajo para el usuario ID: {}", idUsuario);

        List<SolicitudPendienteEspacioTrabajo> solicitudes = solicitudPendienteRepository.findByUsuarioInvitado_Id(idUsuario);
        logger.info("Encontradas {} solicitudes pendientes para el usuario ID: {}.", solicitudes.size(), idUsuario);
        
        return solicitudes.stream()
            .map(solicitudPendienteEspacioTrabajoMapper::toResponse)
            .toList();
    }
}
