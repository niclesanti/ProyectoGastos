package com.campito.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuración de auditoría JPA.
 * 
 * Habilita la auditoría automática de entidades con @EntityListeners(AuditingEntityListener.class).
 * Esto permite que Spring Data JPA gestione automáticamente:
 * - @CreatedDate: Asigna la fecha de creación cuando se inserta un registro
 * - @LastModifiedDate: Actualiza la fecha de modificación cuando se actualiza un registro
 * 
 * Las entidades que utilizan esta funcionalidad deben tener:
 * 1. @EntityListeners(AuditingEntityListener.class) a nivel de clase
 * 2. Campos anotados con @CreatedDate y/o @LastModifiedDate
 * 
 * @see org.springframework.data.annotation.CreatedDate
 * @see org.springframework.data.annotation.LastModifiedDate
 * @see org.springframework.data.jpa.domain.support.AuditingEntityListener
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // No requiere implementación adicional
    // La anotación @EnableJpaAuditing activa la funcionalidad automáticamente
}
