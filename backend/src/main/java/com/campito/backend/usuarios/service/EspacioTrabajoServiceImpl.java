package com.campito.backend.usuarios.service;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Dependencias a revisar para reducir acoplamiento y mejorar la modularidad
import com.campito.backend.notificaciones.event.NotificacionEvent;
import com.campito.backend.notificaciones.domain.entity.TipoNotificacion;

import com.campito.backend.usuarios.repository.EspacioTrabajoRepository;
import com.campito.backend.usuarios.repository.SolicitudPendienteEspacioTrabajoRepository;
import com.campito.backend.usuarios.repository.UsuarioRepository;
import com.campito.backend.usuarios.domain.dto.EspacioTrabajoDTORequest;
import com.campito.backend.usuarios.domain.dto.EspacioTrabajoDTOResponse;
import com.campito.backend.usuarios.domain.dto.UsuarioDTOResponse;
import com.campito.backend.usuarios.domain.dto.SolicitudPendienteEspacioTrabajoDTOResponse;
import com.campito.backend.usuarios.mapper.EspacioTrabajoMapper;
import com.campito.backend.usuarios.mapper.SolicitudPendienteEspacioTrabajoMapper;
import com.campito.backend.usuarios.mapper.UsuarioMapper;
import com.campito.backend.usuarios.domain.entity.EspacioTrabajo;
import com.campito.backend.usuarios.domain.entity.Usuario;
import com.campito.backend.usuarios.domain.entity.SolicitudPendienteEspacioTrabajo;
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
@Slf4j
public class EspacioTrabajoServiceImpl implements EspacioTrabajoService {

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
     * @throws EntityNotFoundException si el usuario administrador no se encuentra en la base de datos.
     */
    @Override
    @Transactional
    public void registrarEspacioTrabajo(EspacioTrabajoDTORequest espacioTrabajoDTO) {

        log.info("Intentando registrar un nuevo espacio de trabajo con nombre: '{}'", espacioTrabajoDTO.nombre());
        // Validar que no exista un espacio con el mismo nombre para el mismo usuario administrador
        Optional<EspacioTrabajo> espacioExistente = espacioRepository
                .findFirstByNombreAndUsuarioAdmin_Id(espacioTrabajoDTO.nombre(), espacioTrabajoDTO.idUsuarioAdmin());
        
        if (espacioExistente.isPresent()) {
            String msg = String.format("Ya existe un espacio de trabajo con el nombre '%s' creado por ti. Por favor, utiliza un nombre diferente.", 
                    espacioTrabajoDTO.nombre());
            log.warn(msg);
            throw new EntidadDuplicadaException(msg);
        }
        Usuario usuario = buscarUsuarioPorId(espacioTrabajoDTO.idUsuarioAdmin());

        EspacioTrabajo espacioTrabajo = espacioTrabajoMapper.toEntity(espacioTrabajoDTO);
        espacioTrabajo.setSaldo(BigDecimal.ZERO);
        espacioTrabajo.setUsuarioAdmin(usuario);
        espacioTrabajo.setUsuariosParticipantes(new ArrayList<>());
        espacioTrabajo.getUsuariosParticipantes().add(usuario);
        espacioRepository.save(espacioTrabajo);
        log.info("Espacio de trabajo '{}' registrado exitosamente.", espacioTrabajo.getNombre());
    }

    /**
     * Comparte un espacio de trabajo con otro usuario mediante su email.
     * 
     * @param email Email del usuario con quien se compartirá el espacio.
     * @param idEspacioTrabajo ID del espacio de trabajo a compartir.
     * @throws EntityNotFoundException si el espacio de trabajo o el usuario no se encuentran en la base de datos.
     * @throws EntidadDuplicadaException si ya existe una solicitud pendiente para el mismo usuario y espacio de trabajo.
     * @throws UsuarioNoEncontradoException si no se encuentra un usuario registrado con el email proporcionado.
     */
    @Override
    @Transactional
    public void compartirEspacioTrabajo(String email, UUID idEspacioTrabajo) {
        log.info("Intentando compartir espacio de trabajo ID: {} con email: {}", idEspacioTrabajo, email);

        EspacioTrabajo espacioTrabajo = buscarEspacioTrabajoPorId(idEspacioTrabajo);

        Usuario usuario = buscarUsuarioPorEmail(email);

        // Validar si el usuario ya es colaborador del espacio de trabajo
        if (espacioTrabajo.getUsuariosParticipantes().contains(usuario)) {
            String mensaje = String.format(
                "El usuario '%s' ya es colaborador del espacio de trabajo '%s'. " +
                "No es necesario volver a invitarlo.",
                email, espacioTrabajo.getNombre()
            );
            log.warn("Intento de invitar a usuario que ya es miembro. Espacio: {}, Usuario: {}", 
                       idEspacioTrabajo, usuario.getId());
            throw new EntidadDuplicadaException(mensaje);
        }

        // Validar si ya existe una solicitud pendiente para este usuario y espacio
        boolean existeSolicitudPendiente = solicitudPendienteRepository
                .existsByEspacioTrabajo_IdAndUsuarioInvitado_Id(idEspacioTrabajo, usuario.getId());
        
        if (existeSolicitudPendiente) {
            String mensaje = String.format(
                "Ya existe una invitación pendiente para '%s' en el espacio de trabajo '%s'. " +
                "Por favor, espera a que el usuario responda la invitación anterior.",
                email, espacioTrabajo.getNombre()
            );
            log.warn("Intento de crear solicitud duplicada. Espacio: {}, Usuario: {}", 
                       idEspacioTrabajo, usuario.getId());
            throw new EntidadDuplicadaException(mensaje);
        }

        SolicitudPendienteEspacioTrabajo solicitud = SolicitudPendienteEspacioTrabajo.builder()
            .espacioTrabajo(espacioTrabajo)
            .usuarioInvitado(usuario)
            .build();
        
        solicitudPendienteRepository.save(solicitud);
        log.info("Solicitud de compartir espacio de trabajo ID: {} creada para el usuario {} (email: {}).", 
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
            log.info("Evento de notificación enviado al usuario {} por invitación al espacio {}", 
                       usuario.getId(), idEspacioTrabajo);
        } catch (Exception e) {
            log.error("Error al enviar notificación de invitación al usuario {} para espacio ID: {}", 
                        usuario.getId(), idEspacioTrabajo, e);
            // No propagamos la excepción para no afectar el compartir del espacio que ya fue guardado exitosamente
        }
    }

    /**
     * Responde a una solicitud pendiente de compartir un espacio de trabajo.
     * 
     * @param idSolicitud ID de la solicitud pendiente.
     * @param aceptada Indica si la solicitud fue aceptada o rechazada.
     * @throws EntityNotFoundException si la solicitud pendiente no se encuentra en la base de datos.
     */
    @Override
    @Transactional
    public void respuestaSolicitudCompartirEspacioTrabajo(Long idSolicitud, Boolean aceptada) {
        // Implementación pendiente
        log.info("Intentando responder solicitud de compartir espacio ID: {} con respuesta: {}", idSolicitud, aceptada);

        SolicitudPendienteEspacioTrabajo solicitud = buscarSolicitudPendientePorId(idSolicitud);
        if (aceptada) {
            EspacioTrabajo espacioTrabajo = solicitud.getEspacioTrabajo();
            Usuario usuario = solicitud.getUsuarioInvitado();

            espacioTrabajo.getUsuariosParticipantes().add(usuario);
            espacioRepository.save(espacioTrabajo);
            log.info("Solicitud de compartir espacio ID: {} aceptada. Usuario {} agregado al espacio ID: {}.", 
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
                log.info("Evento de notificación enviado al administrador {} por aceptación de invitación al espacio {}", 
                           espacioTrabajo.getUsuarioAdmin().getId(), espacioTrabajo.getId());
            } catch (Exception e) {
                log.error("Error al enviar notificación de aceptación al administrador {} para espacio ID: {}", 
                            espacioTrabajo.getUsuarioAdmin().getId(), espacioTrabajo.getId(), e);
                // No propagamos la excepción para no afectar la respuesta a la solicitud que ya fue procesada exitosamente
            }
        }

        solicitudPendienteRepository.delete(solicitud);
        log.info("Solicitud de compartir espacio ID: {} eliminada de solicitudes pendientes.", idSolicitud);
    }

    /**
     * Lista los espacios de trabajo asociados a un usuario.
     * 
     * @param idUsuario ID del usuario cuyos espacios se desean listar.
     * @return Lista de espacios de trabajo en formato DTO.
     */
    @Override
    @Transactional(readOnly = true)
    public List<EspacioTrabajoDTOResponse> listarEspaciosTrabajoPorUsuario(UUID idUsuario) {
        log.info("Intentando listar espacios de trabajo para el usuario ID: {}", idUsuario);

        List<EspacioTrabajo> espacios = espacioRepository.findByUsuariosParticipantes_IdOrderByFechaModificacionDesc(idUsuario);
        log.info("Encontrados {} espacios de trabajo para el usuario ID: {} (ordenados por última modificación).", espacios.size(), idUsuario);
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
     */
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTOResponse> obtenerMiembrosEspacioTrabajo(UUID idEspacioTrabajo) {
        log.info("Intentando obtener miembros del espacio de trabajo ID: {}", idEspacioTrabajo);

        EspacioTrabajo espacioTrabajo = buscarEspacioTrabajoPorId(idEspacioTrabajo);
        
        List<Usuario> miembros = espacioTrabajo.getUsuariosParticipantes();
        log.info("Encontrados {} miembros en el espacio de trabajo ID: {}.", miembros.size(), idEspacioTrabajo);
        
        return miembros.stream()
            .map(usuarioMapper::toResponse)
            .toList();
    }

    /**
     * Lista las solicitudes pendientes para integrar espacios de trabajo de un usuario.
     * 
     * @param idUsuario ID del usuario para listar sus solicitudes pendientes.
     * @return Lista de solicitudes pendientes en formato DTO.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudPendienteEspacioTrabajoDTOResponse> listarSolicitudesPendientes(UUID idUsuario) {
        log.info("Intentando listar solicitudes pendientes de espacios de trabajo para el usuario ID: {}", idUsuario);

        List<SolicitudPendienteEspacioTrabajo> solicitudes = solicitudPendienteRepository.findByUsuarioInvitado_Id(idUsuario);
        log.info("Encontradas {} solicitudes pendientes para el usuario ID: {}.", solicitudes.size(), idUsuario);
        
        return solicitudes.stream()
            .map(solicitudPendienteEspacioTrabajoMapper::toResponse)
            .toList();
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

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow(() -> {
            String mensaje = "Usuario con email " + email + " no encontrado";
            log.warn(mensaje);
            String mensajeUsuario = "No existe ningún usuario registrado con el correo electrónico '" + email + "'. Por favor, verifica que el correo sea correcto o invita a esa persona a registrarse primero.";
            return new UsuarioNoEncontradoException(mensajeUsuario);
        });
    }

    private EspacioTrabajo buscarEspacioTrabajoPorId(UUID idEspacioTrabajo) {
        return espacioRepository.findById(idEspacioTrabajo).orElseThrow(() -> {
            String mensaje = "Espacio de trabajo con ID " + idEspacioTrabajo + " no encontrado";
            log.warn(mensaje);
            return new EntityNotFoundException(mensaje);
        });
    }

    private SolicitudPendienteEspacioTrabajo buscarSolicitudPendientePorId(Long idSolicitud) {
        return solicitudPendienteRepository.findById(idSolicitud).orElseThrow(() -> {
            String mensaje = "Solicitud de compartir espacio de trabajo con ID " + idSolicitud + " no encontrada";
            log.warn(mensaje);
            return new EntityNotFoundException(mensaje);
        });
    }
}
