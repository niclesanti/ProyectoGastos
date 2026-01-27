-- Migración V9: Añadir constraints UNIQUE para evitar duplicados en:
-- 1. Espacios de trabajo (mismo nombre + usuario)
-- 2. Cuentas bancarias (mismo nombre + espacio)
-- 3. Tarjetas (mismo número + entidad + red + espacio)

-- ===========================================
-- 1. ESPACIOS_TRABAJO
-- ===========================================
-- Eliminar duplicados manteniendo el más reciente (por id más alto)
DELETE FROM espacios_trabajo
WHERE id NOT IN (
    SELECT MAX(id)
    FROM espacios_trabajo
    GROUP BY nombre, usuario_admin_id
);

-- Añadir constraint UNIQUE para nombre + usuario_admin_id
ALTER TABLE espacios_trabajo
ADD CONSTRAINT uk_espacios_trabajo_nombre_usuario
UNIQUE (nombre, usuario_admin_id);

-- ===========================================
-- 2. CUENTAS_BANCARIAS
-- ===========================================
-- Eliminar duplicados manteniendo el más reciente (por id más alto)
DELETE FROM cuentas_bancarias
WHERE id NOT IN (
    SELECT MAX(id)
    FROM cuentas_bancarias
    GROUP BY nombre, id_espacio_trabajo
);

-- Añadir constraint UNIQUE para nombre + id_espacio_trabajo
ALTER TABLE cuentas_bancarias
ADD CONSTRAINT uk_cuentas_bancarias_nombre_espacio
UNIQUE (nombre, id_espacio_trabajo);

-- ===========================================
-- 3. TARJETAS
-- ===========================================
-- Eliminar duplicados manteniendo la más reciente (por id más alto)
DELETE FROM tarjetas
WHERE id NOT IN (
    SELECT MAX(id)
    FROM tarjetas
    GROUP BY numero_tarjeta, entidad_financiera, red_de_pago, espacio_trabajo_id
);

-- Añadir constraint UNIQUE para numero_tarjeta + entidad_financiera + red_de_pago + espacio_trabajo_id
ALTER TABLE tarjetas
ADD CONSTRAINT uk_tarjetas_numero_entidad_red_espacio
UNIQUE (numero_tarjeta, entidad_financiera, red_de_pago, espacio_trabajo_id);
