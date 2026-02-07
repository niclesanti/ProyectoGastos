-- =====================================================
-- Migración V15: Solicitudes Pendientes de Espacios de Trabajo
-- =====================================================
-- Descripción: Crea la tabla para gestionar invitaciones pendientes a espacios de trabajo
-- Autor: Sistema
-- Fecha: 2026-02-07
-- =====================================================

-- Crear tabla de solicitudes pendientes
CREATE TABLE solicitudes_pendientes_espacio_trabajo (
    id BIGSERIAL PRIMARY KEY,
    espacio_trabajo_id UUID NOT NULL,
    usuario_invitado_id UUID NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_solicitud_espacio_trabajo FOREIGN KEY (espacio_trabajo_id) 
        REFERENCES espacios_trabajo(id) ON DELETE CASCADE,
    CONSTRAINT fk_solicitud_usuario_invitado FOREIGN KEY (usuario_invitado_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Restricción única: evitar solicitudes duplicadas para el mismo usuario y espacio
ALTER TABLE solicitudes_pendientes_espacio_trabajo
    ADD CONSTRAINT uq_solicitud_usuario_espacio UNIQUE (espacio_trabajo_id, usuario_invitado_id);

-- Índices para optimización de consultas
CREATE INDEX idx_solicitudes_usuario_invitado ON solicitudes_pendientes_espacio_trabajo(usuario_invitado_id);
CREATE INDEX idx_solicitudes_fecha ON solicitudes_pendientes_espacio_trabajo(fecha_creacion DESC);

-- Comentarios para documentación
COMMENT ON TABLE solicitudes_pendientes_espacio_trabajo IS 'Solicitudes pendientes de invitación a espacios de trabajo';
COMMENT ON COLUMN solicitudes_pendientes_espacio_trabajo.id IS 'Identificador único de la solicitud (autoincremental)';
COMMENT ON COLUMN solicitudes_pendientes_espacio_trabajo.espacio_trabajo_id IS 'ID del espacio de trabajo al que se invita';
COMMENT ON COLUMN solicitudes_pendientes_espacio_trabajo.usuario_invitado_id IS 'ID del usuario que recibe la invitación';
COMMENT ON COLUMN solicitudes_pendientes_espacio_trabajo.fecha_creacion IS 'Fecha y hora en que se creó la solicitud';
