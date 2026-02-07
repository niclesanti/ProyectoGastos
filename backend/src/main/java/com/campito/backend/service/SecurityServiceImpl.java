package com.campito.backend.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.campito.backend.dao.CompraCreditoRepository;
import com.campito.backend.dao.CuentaBancariaRepository;
import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dao.NotificacionRepository;
import com.campito.backend.dao.SolicitudPendienteEspacioTrabajoRepository;
import com.campito.backend.dao.TarjetaRepository;
import com.campito.backend.dao.TransaccionRepository;
import com.campito.backend.exception.ForbiddenException;
import com.campito.backend.exception.UnauthorizedException;
import com.campito.backend.model.CompraCredito;
import com.campito.backend.model.CuentaBancaria;
import com.campito.backend.model.CustomOAuth2User;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.Notificacion;
import com.campito.backend.model.SolicitudPendienteEspacioTrabajo;
import com.campito.backend.model.Tarjeta;
import com.campito.backend.model.Transaccion;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de seguridad para validación de autorización.
 * 
 * Gestiona la obtención del contexto de seguridad del usuario autenticado
 * y valida permisos de acceso a recursos basándose en la relación con espacios de trabajo.
 */
@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);
    
    private final EspacioTrabajoRepository espacioTrabajoRepository;
    private final TransaccionRepository transaccionRepository;
    private final CompraCreditoRepository compraCreditoRepository;
    private final CuentaBancariaRepository cuentaBancariaRepository;
    private final TarjetaRepository tarjetaRepository;
    private final NotificacionRepository notificacionRepository;
    private final SolicitudPendienteEspacioTrabajoRepository solicitudPendienteRepository;

    /**
     * Obtiene el ID del usuario actualmente autenticado desde el contexto de seguridad de Spring.
     * 
     * @return UUID del usuario autenticado.
     * @throws UnauthorizedException si no hay usuario autenticado o el principal no es del tipo esperado.
     */
    @Override
    public UUID getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            logger.warn("Intento de acceso sin autenticación válida");
            throw new UnauthorizedException("Usuario no autenticado. Por favor, inicia sesión.");
        }
        
        Object principal = auth.getPrincipal();
        
        if (!(principal instanceof CustomOAuth2User)) {
            logger.error("Principal no es del tipo esperado. Tipo recibido: {}", 
                principal != null ? principal.getClass().getName() : "null");
            throw new UnauthorizedException("Sesión de usuario inválida. Por favor, inicia sesión nuevamente.");
        }
        
        CustomOAuth2User oauthUser = (CustomOAuth2User) principal;
        UUID userId = oauthUser.getUsuario().getId();
        
        logger.debug("Usuario autenticado obtenido: {}", userId);
        return userId;
    }

    /**
     * Valida que el usuario autenticado tenga acceso a un espacio de trabajo.
     * Un usuario tiene acceso si es participante del espacio.
     * 
     * @param workspaceId ID del espacio de trabajo.
     * @throws IllegalArgumentException si workspaceId es nulo.
     * @throws UnauthorizedException si no hay usuario autenticado.
     * @throws ForbiddenException si el usuario no tiene acceso al espacio.
     */
    @Override
    public void validateWorkspaceAccess(UUID workspaceId) {
        if (workspaceId == null) {
            logger.warn("Intento de validar acceso con workspaceId nulo");
            throw new IllegalArgumentException("El ID del espacio de trabajo no puede ser nulo");
        }
        
        UUID userId = getAuthenticatedUserId();
        
        boolean hasAccess = espacioTrabajoRepository
            .existsByIdAndUsuariosParticipantes_Id(workspaceId, userId);
        
        if (!hasAccess) {
            logger.warn("Usuario {} intenta acceder al espacio de trabajo {} sin permisos", userId, workspaceId);
            throw new ForbiddenException("No tienes acceso a este espacio de trabajo");
        }
        
        logger.debug("Acceso validado: Usuario {} tiene acceso al espacio {}", userId, workspaceId);
    }

    /**
     * Valida que el usuario autenticado sea el administrador del espacio de trabajo.
     * 
     * @param workspaceId ID del espacio de trabajo.
     * @throws IllegalArgumentException si workspaceId es nulo.
     * @throws UnauthorizedException si no hay usuario autenticado.
     * @throws ForbiddenException si el usuario no es administrador del espacio.
     */
    @Override
    public void validateWorkspaceAdmin(UUID workspaceId) {
        if (workspaceId == null) {
            logger.warn("Intento de validar admin con workspaceId nulo");
            throw new IllegalArgumentException("El ID del espacio de trabajo no puede ser nulo");
        }
        
        UUID userId = getAuthenticatedUserId();
        
        EspacioTrabajo workspace = espacioTrabajoRepository.findById(workspaceId)
            .orElseThrow(() -> {
                logger.warn("Espacio de trabajo {} no encontrado", workspaceId);
                return new EntityNotFoundException("Espacio de trabajo no encontrado");
            });
        
        if (!workspace.getUsuarioAdmin().getId().equals(userId)) {
            logger.warn("Usuario {} intenta realizar acción de admin en espacio {} sin ser administrador", 
                userId, workspaceId);
            throw new ForbiddenException("Solo el administrador del espacio de trabajo puede realizar esta acción");
        }
        
        logger.debug("Permisos de admin validados: Usuario {} es admin del espacio {}", userId, workspaceId);
    }

    /**
     * Valida que una transacción pertenezca a un espacio de trabajo accesible por el usuario.
     * 
     * @param transactionId ID de la transacción.
     * @throws IllegalArgumentException si transactionId es nulo.
     * @throws EntityNotFoundException si la transacción no existe.
     * @throws UnauthorizedException si no hay usuario autenticado.
     * @throws ForbiddenException si el usuario no tiene acceso a la transacción.
     */
    @Override
    public void validateTransactionOwnership(Long transactionId) {
        if (transactionId == null) {
            logger.warn("Intento de validar transacción con ID nulo");
            throw new IllegalArgumentException("El ID de la transacción no puede ser nulo");
        }
        
        Transaccion transaccion = transaccionRepository.findById(transactionId)
            .orElseThrow(() -> {
                logger.warn("Transacción {} no encontrada", transactionId);
                return new EntityNotFoundException("Transacción no encontrada");
            });
        
        UUID workspaceId = transaccion.getEspacioTrabajo().getId();
        validateWorkspaceAccess(workspaceId);
        
        logger.debug("Ownership validado: Usuario tiene acceso a transacción {}", transactionId);
    }

    /**
     * Valida que una compra a crédito pertenezca a un espacio de trabajo accesible por el usuario.
     * 
     * @param compraCreditoId ID de la compra a crédito.
     * @throws IllegalArgumentException si compraCreditoId es nulo.
     * @throws EntityNotFoundException si la compra no existe.
     * @throws UnauthorizedException si no hay usuario autenticado.
     * @throws ForbiddenException si el usuario no tiene acceso a la compra.
     */
    @Override
    public void validateCompraCreditoOwnership(Long compraCreditoId) {
        if (compraCreditoId == null) {
            logger.warn("Intento de validar compra a crédito con ID nulo");
            throw new IllegalArgumentException("El ID de la compra a crédito no puede ser nulo");
        }
        
        CompraCredito compra = compraCreditoRepository.findById(compraCreditoId)
            .orElseThrow(() -> {
                logger.warn("Compra a crédito {} no encontrada", compraCreditoId);
                return new EntityNotFoundException("Compra a crédito no encontrada");
            });
        
        UUID workspaceId = compra.getEspacioTrabajo().getId();
        validateWorkspaceAccess(workspaceId);
        
        logger.debug("Ownership validado: Usuario tiene acceso a compra a crédito {}", compraCreditoId);
    }

    /**
     * Valida que una cuenta bancaria pertenezca a un espacio de trabajo accesible por el usuario.
     * 
     * @param cuentaBancariaId ID de la cuenta bancaria.
     * @throws IllegalArgumentException si cuentaBancariaId es nulo.
     * @throws EntityNotFoundException si la cuenta no existe.
     * @throws UnauthorizedException si no hay usuario autenticado.
     * @throws ForbiddenException si el usuario no tiene acceso a la cuenta.
     */
    @Override
    public void validateCuentaBancariaOwnership(Long cuentaBancariaId) {
        if (cuentaBancariaId == null) {
            logger.warn("Intento de validar cuenta bancaria con ID nulo");
            throw new IllegalArgumentException("El ID de la cuenta bancaria no puede ser nulo");
        }
        
        CuentaBancaria cuenta = cuentaBancariaRepository.findById(cuentaBancariaId)
            .orElseThrow(() -> {
                logger.warn("Cuenta bancaria {} no encontrada", cuentaBancariaId);
                return new EntityNotFoundException("Cuenta bancaria no encontrada");
            });
        
        UUID workspaceId = cuenta.getEspacioTrabajo().getId();
        validateWorkspaceAccess(workspaceId);
        
        logger.debug("Ownership validado: Usuario tiene acceso a cuenta bancaria {}", cuentaBancariaId);
    }

    /**
     * Valida que una tarjeta pertenezca a un espacio de trabajo accesible por el usuario.
     * 
     * @param tarjetaId ID de la tarjeta.
     * @throws IllegalArgumentException si tarjetaId es nulo.
     * @throws EntityNotFoundException si la tarjeta no existe.
     * @throws UnauthorizedException si no hay usuario autenticado.
     * @throws ForbiddenException si el usuario no tiene acceso a la tarjeta.
     */
    @Override
    public void validateTarjetaOwnership(Long tarjetaId) {
        if (tarjetaId == null) {
            logger.warn("Intento de validar tarjeta con ID nulo");
            throw new IllegalArgumentException("El ID de la tarjeta no puede ser nulo");
        }
        
        Tarjeta tarjeta = tarjetaRepository.findById(tarjetaId)
            .orElseThrow(() -> {
                logger.warn("Tarjeta {} no encontrada", tarjetaId);
                return new EntityNotFoundException("Tarjeta no encontrada");
            });
        
        UUID workspaceId = tarjeta.getEspacioTrabajo().getId();
        validateWorkspaceAccess(workspaceId);
        
        logger.debug("Ownership validado: Usuario tiene acceso a tarjeta {}", tarjetaId);
    }

    /**
     * Valida que una notificación pertenezca al usuario autenticado.
     * 
     * @param notificacionId ID de la notificación.
     * @throws IllegalArgumentException si notificacionId es nulo.
     * @throws EntityNotFoundException si la notificación no existe.
     * @throws UnauthorizedException si no hay usuario autenticado.
     * @throws ForbiddenException si la notificación no pertenece al usuario.
     */
    @Override
    public void validateNotificacionOwnership(Long notificacionId) {
        if (notificacionId == null) {
            logger.warn("Intento de validar notificación con ID nulo");
            throw new IllegalArgumentException("El ID de la notificación no puede ser nulo");
        }
        
        UUID userId = getAuthenticatedUserId();
        
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
            .orElseThrow(() -> {
                logger.warn("Notificación {} no encontrada", notificacionId);
                return new EntityNotFoundException("Notificación no encontrada");
            });
        
        if (!notificacion.getUsuario().getId().equals(userId)) {
            logger.warn("Usuario {} intenta acceder a notificación {} que no le pertenece", 
                userId, notificacionId);
            throw new ForbiddenException("No tienes acceso a esta notificación");
        }
        
        logger.debug("Ownership validado: Usuario {} tiene acceso a notificación {}", userId, notificacionId);
    }

    @Override
    public void validateSolicitudOwnership(Long idSolicitud) {
        if (idSolicitud == null) {
            logger.warn("Intento de validar solicitud con ID nulo");
            throw new IllegalArgumentException("El ID de la solicitud no puede ser nulo");
        }

        UUID userId = getAuthenticatedUserId();

        SolicitudPendienteEspacioTrabajo solicitud = solicitudPendienteRepository.findById(idSolicitud)
            .orElseThrow(() -> {
                logger.warn("Solicitud pendiente {} no encontrada", idSolicitud);
                return new EntityNotFoundException("Solicitud pendiente no encontrada");
            });
        
        if (!solicitud.getUsuarioInvitado().getId().equals(userId)) {
            logger.warn("Usuario {} intenta acceder a solicitud pendiente {} que no le pertenece", 
                userId, idSolicitud);
            throw new ForbiddenException("No tienes acceso a esta solicitud pendiente");
        }
        
        logger.debug("Ownership validado: Usuario {} tiene acceso a solicitud pendiente {}", userId, idSolicitud);
    }

    /**
     * Verifica si el usuario autenticado tiene acceso a un espacio de trabajo.
     * 
     * @param workspaceId ID del espacio de trabajo.
     * @return true si tiene acceso, false en caso contrario.
     * @throws IllegalArgumentException si workspaceId es nulo.
     */
    @Override
    public boolean hasWorkspaceAccess(UUID workspaceId) {
        if (workspaceId == null) {
            logger.warn("Intento de verificar acceso con workspaceId nulo");
            throw new IllegalArgumentException("El ID del espacio de trabajo no puede ser nulo");
        }
        
        try {
            UUID userId = getAuthenticatedUserId();
            boolean hasAccess = espacioTrabajoRepository
                .existsByIdAndUsuariosParticipantes_Id(workspaceId, userId);
            
            logger.debug("Verificación de acceso: Usuario {} {} acceso al espacio {}", 
                userId, hasAccess ? "tiene" : "no tiene", workspaceId);
            
            return hasAccess;
        } catch (UnauthorizedException e) {
            logger.debug("Usuario no autenticado al verificar acceso al espacio {}", workspaceId);
            return false;
        }
    }

    /**
     * Verifica si el usuario autenticado es administrador de un espacio de trabajo.
     * 
     * @param workspaceId ID del espacio de trabajo.
     * @return true si es administrador, false en caso contrario.
     * @throws IllegalArgumentException si workspaceId es nulo.
     */
    @Override
    public boolean isWorkspaceAdmin(UUID workspaceId) {
        if (workspaceId == null) {
            logger.warn("Intento de verificar admin con workspaceId nulo");
            throw new IllegalArgumentException("El ID del espacio de trabajo no puede ser nulo");
        }
        
        try {
            UUID userId = getAuthenticatedUserId();
            
            EspacioTrabajo workspace = espacioTrabajoRepository.findById(workspaceId)
                .orElse(null);
            
            if (workspace == null) {
                logger.debug("Espacio de trabajo {} no encontrado al verificar admin", workspaceId);
                return false;
            }
            
            boolean isAdmin = workspace.getUsuarioAdmin().getId().equals(userId);
            
            logger.debug("Verificación de admin: Usuario {} {} admin del espacio {}", 
                userId, isAdmin ? "es" : "no es", workspaceId);
            
            return isAdmin;
        } catch (UnauthorizedException e) {
            logger.debug("Usuario no autenticado al verificar admin del espacio {}", workspaceId);
            return false;
        }
    }
}
