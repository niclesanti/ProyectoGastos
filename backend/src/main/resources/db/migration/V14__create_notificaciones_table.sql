-- =====================================================
-- Migración V14: Sistema de Notificaciones
-- =====================================================
-- Descripción: Crea la tabla de notificaciones para el sistema
-- Autor: Sistema
-- Fecha: 2026-02-04
-- =====================================================

-- Crear tabla de notificaciones
CREATE TABLE notificaciones (
    id BIGSERIAL PRIMARY KEY,
    id_usuario UUID NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    mensaje VARCHAR(200) NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT false,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_leida TIMESTAMP,
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Índices para optimización de consultas
CREATE INDEX idx_notificaciones_usuario_leida ON notificaciones(id_usuario, leida);
CREATE INDEX idx_notificaciones_fecha ON notificaciones(fecha_creacion DESC);

-- Comentarios para documentación
COMMENT ON TABLE notificaciones IS 'Notificaciones del sistema para los usuarios';
COMMENT ON COLUMN notificaciones.id IS 'Identificador único de la notificación (autoincremental)';
COMMENT ON COLUMN notificaciones.id_usuario IS 'Usuario destinatario de la notificación';
COMMENT ON COLUMN notificaciones.tipo IS 'Tipo de notificación: CIERRE_TARJETA, INVITACION_ESPACIO, VENCIMIENTO_RESUMEN, MIEMBRO_AGREGADO, SISTEMA';
COMMENT ON COLUMN notificaciones.mensaje IS 'Mensaje descriptivo de la notificación (máx 200 caracteres)';
COMMENT ON COLUMN notificaciones.leida IS 'Indica si el usuario ya leyó la notificación';
COMMENT ON COLUMN notificaciones.fecha_creacion IS 'Fecha y hora de creación de la notificación';
COMMENT ON COLUMN notificaciones.fecha_leida IS 'Fecha y hora en que el usuario marcó la notificación como leída';
