package com.campito.backend.service;

import java.util.UUID;

/**
 * Servicio de seguridad para gestionar la autorización y validación de acceso a recursos.
 * 
 * Proporciona métodos para obtener el contexto del usuario autenticado y validar
 * permisos de acceso a espacios de trabajo, transacciones y otros recursos.
 */
public interface SecurityService {
    
    /**
     * Obtiene el ID del usuario actualmente autenticado desde el contexto de seguridad.
     * 
     * @return UUID del usuario autenticado.
     * @throws com.campito.backend.exception.UnauthorizedException si no hay usuario autenticado.
     */
    UUID getAuthenticatedUserId();
    
    /**
     * Valida que el usuario autenticado tenga acceso a un espacio de trabajo específico.
     * 
     * @param workspaceId ID del espacio de trabajo a validar.
     * @throws com.campito.backend.exception.UnauthorizedException si no hay usuario autenticado.
     * @throws com.campito.backend.exception.ForbiddenException si el usuario no tiene acceso al espacio.
     * @throws IllegalArgumentException si el workspaceId es nulo.
     */
    void validateWorkspaceAccess(UUID workspaceId);
    
    /**
     * Valida que el usuario autenticado sea el administrador de un espacio de trabajo.
     * 
     * @param workspaceId ID del espacio de trabajo a validar.
     * @throws com.campito.backend.exception.UnauthorizedException si no hay usuario autenticado.
     * @throws com.campito.backend.exception.ForbiddenException si el usuario no es administrador.
     * @throws IllegalArgumentException si el workspaceId es nulo.
     */
    void validateWorkspaceAdmin(UUID workspaceId);
    
    /**
     * Valida que una transacción pertenezca a un espacio de trabajo al que el usuario tiene acceso.
     * 
     * @param transactionId ID de la transacción a validar.
     * @throws com.campito.backend.exception.UnauthorizedException si no hay usuario autenticado.
     * @throws com.campito.backend.exception.ForbiddenException si el usuario no tiene acceso.
     * @throws jakarta.persistence.EntityNotFoundException si la transacción no existe.
     * @throws IllegalArgumentException si el transactionId es nulo.
     */
    void validateTransactionOwnership(Long transactionId);
    
    /**
     * Valida que una compra a crédito pertenezca a un espacio de trabajo al que el usuario tiene acceso.
     * 
     * @param compraCreditoId ID de la compra a crédito a validar.
     * @throws com.campito.backend.exception.UnauthorizedException si no hay usuario autenticado.
     * @throws com.campito.backend.exception.ForbiddenException si el usuario no tiene acceso.
     * @throws jakarta.persistence.EntityNotFoundException si la compra no existe.
     * @throws IllegalArgumentException si el compraCreditoId es nulo.
     */
    void validateCompraCreditoOwnership(Long compraCreditoId);
    
    /**
     * Valida que una cuenta bancaria pertenezca a un espacio de trabajo al que el usuario tiene acceso.
     * 
     * @param cuentaBancariaId ID de la cuenta bancaria a validar.
     * @throws com.campito.backend.exception.UnauthorizedException si no hay usuario autenticado.
     * @throws com.campito.backend.exception.ForbiddenException si el usuario no tiene acceso.
     * @throws jakarta.persistence.EntityNotFoundException si la cuenta no existe.
     * @throws IllegalArgumentException si el cuentaBancariaId es nulo.
     */
    void validateCuentaBancariaOwnership(Long cuentaBancariaId);
    
    /**
     * Valida que una tarjeta pertenezca a un espacio de trabajo al que el usuario tiene acceso.
     * 
     * @param tarjetaId ID de la tarjeta a validar.
     * @throws com.campito.backend.exception.UnauthorizedException si no hay usuario autenticado.
     * @throws com.campito.backend.exception.ForbiddenException si el usuario no tiene acceso.
     * @throws jakarta.persistence.EntityNotFoundException si la tarjeta no existe.
     * @throws IllegalArgumentException si el tarjetaId es nulo.
     */
    void validateTarjetaOwnership(Long tarjetaId);
    
    /**
     * Verifica si el usuario autenticado tiene acceso a un espacio de trabajo específico.
     * 
     * @param workspaceId ID del espacio de trabajo a verificar.
     * @return true si el usuario tiene acceso, false en caso contrario.
     * @throws IllegalArgumentException si el workspaceId es nulo.
     */
    boolean hasWorkspaceAccess(UUID workspaceId);
    
    /**
     * Verifica si el usuario autenticado es administrador de un espacio de trabajo.
     * 
     * @param workspaceId ID del espacio de trabajo a verificar.
     * @return true si el usuario es administrador, false en caso contrario.
     * @throws IllegalArgumentException si el workspaceId es nulo.
     */
    boolean isWorkspaceAdmin(UUID workspaceId);
}
