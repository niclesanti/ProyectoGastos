-- Migración V10: Agregar campos de auditoría a las entidades principales
-- Fecha: 2026-01-20
-- Descripción: Agrega fecha_creacion y fecha_modificacion a ContactoTransferencia, 
--              CuentaBancaria, EspacioTrabajo, MotivoTransaccion y Tarjeta

-- Agregar columnas de auditoría a contactos_transferencia
ALTER TABLE contactos_transferencia
ADD COLUMN fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Agregar columnas de auditoría a cuentas_bancarias
ALTER TABLE cuentas_bancarias
ADD COLUMN fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Agregar columnas de auditoría a espacios_trabajo
ALTER TABLE espacios_trabajo
ADD COLUMN fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Agregar columnas de auditoría a motivos_transaccion
ALTER TABLE motivos_transaccion
ADD COLUMN fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Agregar columnas de auditoría a tarjetas
ALTER TABLE tarjetas
ADD COLUMN fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Crear índices para optimizar las consultas ordenadas por fecha_modificacion
-- Estos índices mejorarán el rendimiento al obtener entidades ordenadas por última modificación

CREATE INDEX idx_contactos_transferencia_fecha_modificacion 
ON contactos_transferencia(fecha_modificacion DESC);

CREATE INDEX idx_cuentas_bancarias_fecha_modificacion 
ON cuentas_bancarias(fecha_modificacion DESC);

CREATE INDEX idx_espacios_trabajo_fecha_modificacion 
ON espacios_trabajo(fecha_modificacion DESC);

CREATE INDEX idx_motivos_transaccion_fecha_modificacion 
ON motivos_transaccion(fecha_modificacion DESC);

CREATE INDEX idx_tarjetas_fecha_modificacion 
ON tarjetas(fecha_modificacion DESC);

-- Comentarios sobre los índices:
-- Los índices DESC optimizan las consultas ORDER BY fecha_modificacion DESC
-- que son las más comunes para mostrar los elementos más recientes primero
