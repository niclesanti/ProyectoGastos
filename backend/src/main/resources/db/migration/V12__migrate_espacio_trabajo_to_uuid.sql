-- ====================================================================
-- Migración: V12__migrate_espacio_trabajo_to_uuid.sql
-- Descripción: Migra el campo id de la tabla espacios_trabajo de BIGSERIAL a UUID
--              Actualiza todas las referencias de clave foránea en entidades hijas
-- Fecha: 2026-01-22
-- Criticidad: ALTA - Cambio de seguridad para prevenir IDOR en multi-tenancy
-- ====================================================================

-- IMPORTANTE: Esta migración debe ejecutarse DESPUÉS de V11 (migración de usuarios a UUID)

-- Paso 1: Eliminar ALL constraints de claves foráneas que referencian espacios_trabajo
-- Usamos CASCADE y también los nombres autogenerados que encontró PostgreSQL
ALTER TABLE transacciones DROP CONSTRAINT IF EXISTS fk_transaccion_espacio_trabajo CASCADE;
ALTER TABLE transacciones DROP CONSTRAINT IF EXISTS fk7dmqj6730tpfbojlii0eg8lfv CASCADE;

ALTER TABLE cuentas_bancarias DROP CONSTRAINT IF EXISTS fk_cuenta_bancaria_espacio_trabajo CASCADE;
ALTER TABLE cuentas_bancarias DROP CONSTRAINT IF EXISTS fk_cuentabancaria_espaciotrabajo CASCADE;

ALTER TABLE motivos_transaccion DROP CONSTRAINT IF EXISTS fk_motivo_espacio_trabajo CASCADE;
ALTER TABLE motivos_transaccion DROP CONSTRAINT IF EXISTS fktlofqnrwix83utgw86x10h6fc CASCADE;

ALTER TABLE contactos_transferencia DROP CONSTRAINT IF EXISTS fk_contacto_espacio_trabajo CASCADE;
ALTER TABLE contactos_transferencia DROP CONSTRAINT IF EXISTS fkgij5m31e0w1r80bxbkeeuismu CASCADE;

ALTER TABLE tarjetas DROP CONSTRAINT IF EXISTS fk_tarjeta_espacio_trabajo CASCADE;
ALTER TABLE tarjetas DROP CONSTRAINT IF EXISTS fk_tarjetas_espacio_trabajo CASCADE;

ALTER TABLE compras_credito DROP CONSTRAINT IF EXISTS fk_compra_credito_espacio_trabajo CASCADE;
ALTER TABLE compras_credito DROP CONSTRAINT IF EXISTS fk_compras_credito_espacio_trabajo CASCADE;

ALTER TABLE gastos_ingresos_mensuales DROP CONSTRAINT IF EXISTS fk_gastos_ingresos_espacio_trabajo CASCADE;

ALTER TABLE espacios_trabajo_usuarios DROP CONSTRAINT IF EXISTS fk_espacios_trabajo_usuarios_espacio CASCADE;
ALTER TABLE espacios_trabajo_usuarios DROP CONSTRAINT IF EXISTS fkn06wr27bco5ijhxgvwe7ueej4 CASCADE;

-- Paso 2: Agregar nueva columna UUID en espacios_trabajo
ALTER TABLE espacios_trabajo ADD COLUMN id_uuid UUID DEFAULT gen_random_uuid();

-- Paso 3: Poblar UUIDs para espacios existentes
UPDATE espacios_trabajo SET id_uuid = gen_random_uuid() WHERE id_uuid IS NULL;

-- Paso 4: Hacer la columna UUID NOT NULL
ALTER TABLE espacios_trabajo ALTER COLUMN id_uuid SET NOT NULL;

-- Paso 5: Agregar columnas UUID en todas las tablas que referencian espacios_trabajo
ALTER TABLE transacciones ADD COLUMN espacio_trabajo_id_uuid UUID;
ALTER TABLE cuentas_bancarias ADD COLUMN espacio_trabajo_id_uuid UUID;
ALTER TABLE motivos_transaccion ADD COLUMN espacio_trabajo_id_uuid UUID;
ALTER TABLE contactos_transferencia ADD COLUMN espacio_trabajo_id_uuid UUID;
ALTER TABLE tarjetas ADD COLUMN espacio_trabajo_id_uuid UUID;
ALTER TABLE compras_credito ADD COLUMN espacio_trabajo_id_uuid UUID;
ALTER TABLE gastos_ingresos_mensuales ADD COLUMN espacio_trabajo_id_uuid UUID;
ALTER TABLE espacios_trabajo_usuarios ADD COLUMN espacio_trabajo_id_uuid UUID;

-- Paso 6: Migrar datos de las FKs (mapear Long a UUID)
-- Transacciones
UPDATE transacciones t
SET espacio_trabajo_id_uuid = (
    SELECT et.id_uuid 
    FROM espacios_trabajo et 
    WHERE et.id = t.espacio_trabajo_id
);

-- Cuentas bancarias
UPDATE cuentas_bancarias cb
SET espacio_trabajo_id_uuid = (
    SELECT et.id_uuid 
    FROM espacios_trabajo et 
    WHERE et.id = cb.id_espacio_trabajo
);

-- Motivos de transacción
UPDATE motivos_transaccion mt
SET espacio_trabajo_id_uuid = (
    SELECT et.id_uuid 
    FROM espacios_trabajo et 
    WHERE et.id = mt.id_espacio_trabajo
);

-- Contactos de transferencia
UPDATE contactos_transferencia ct
SET espacio_trabajo_id_uuid = (
    SELECT et.id_uuid 
    FROM espacios_trabajo et 
    WHERE et.id = ct.id_espacio_trabajo
);

-- Tarjetas
UPDATE tarjetas tar
SET espacio_trabajo_id_uuid = (
    SELECT et.id_uuid 
    FROM espacios_trabajo et 
    WHERE et.id = tar.espacio_trabajo_id
);

-- Compras a crédito
UPDATE compras_credito cc
SET espacio_trabajo_id_uuid = (
    SELECT et.id_uuid 
    FROM espacios_trabajo et 
    WHERE et.id = cc.espacio_trabajo_id
);

-- Gastos e ingresos mensuales
UPDATE gastos_ingresos_mensuales gim
SET espacio_trabajo_id_uuid = (
    SELECT et.id_uuid 
    FROM espacios_trabajo et 
    WHERE et.id = gim.espacio_trabajo_id
);

-- Espacios trabajo usuarios (relación many-to-many)
UPDATE espacios_trabajo_usuarios etu
SET espacio_trabajo_id_uuid = (
    SELECT et.id_uuid 
    FROM espacios_trabajo et 
    WHERE et.id = etu.espacio_trabajo_id
);

-- Paso 7: Eliminar la constraint de clave primaria antigua
ALTER TABLE espacios_trabajo DROP CONSTRAINT IF EXISTS espacios_trabajo_pkey;

-- Paso 8: Eliminar columna id antigua
ALTER TABLE espacios_trabajo DROP COLUMN id;

-- Paso 9: Renombrar columna id_uuid a id
ALTER TABLE espacios_trabajo RENAME COLUMN id_uuid TO id;

-- Paso 10: Establecer nueva clave primaria con UUID
ALTER TABLE espacios_trabajo ADD PRIMARY KEY (id);

-- Paso 11: Eliminar columnas FK antiguas y renombrar UUID a nombre original
-- Transacciones
ALTER TABLE transacciones DROP COLUMN espacio_trabajo_id;
ALTER TABLE transacciones RENAME COLUMN espacio_trabajo_id_uuid TO espacio_trabajo_id;
ALTER TABLE transacciones ALTER COLUMN espacio_trabajo_id SET NOT NULL;

-- Cuentas bancarias (nota: columna se llama id_espacio_trabajo)
ALTER TABLE cuentas_bancarias DROP COLUMN id_espacio_trabajo;
ALTER TABLE cuentas_bancarias RENAME COLUMN espacio_trabajo_id_uuid TO id_espacio_trabajo;
ALTER TABLE cuentas_bancarias ALTER COLUMN id_espacio_trabajo SET NOT NULL;

-- Motivos transacción (nota: columna se llama id_espacio_trabajo)
ALTER TABLE motivos_transaccion DROP COLUMN id_espacio_trabajo;
ALTER TABLE motivos_transaccion RENAME COLUMN espacio_trabajo_id_uuid TO id_espacio_trabajo;
ALTER TABLE motivos_transaccion ALTER COLUMN id_espacio_trabajo SET NOT NULL;

-- Contactos transferencia (nota: columna se llama id_espacio_trabajo)
ALTER TABLE contactos_transferencia DROP COLUMN id_espacio_trabajo;
ALTER TABLE contactos_transferencia RENAME COLUMN espacio_trabajo_id_uuid TO id_espacio_trabajo;
ALTER TABLE contactos_transferencia ALTER COLUMN id_espacio_trabajo SET NOT NULL;

-- Tarjetas
ALTER TABLE tarjetas DROP COLUMN espacio_trabajo_id;
ALTER TABLE tarjetas RENAME COLUMN espacio_trabajo_id_uuid TO espacio_trabajo_id;
ALTER TABLE tarjetas ALTER COLUMN espacio_trabajo_id SET NOT NULL;

-- Compras crédito
ALTER TABLE compras_credito DROP COLUMN espacio_trabajo_id;
ALTER TABLE compras_credito RENAME COLUMN espacio_trabajo_id_uuid TO espacio_trabajo_id;
ALTER TABLE compras_credito ALTER COLUMN espacio_trabajo_id SET NOT NULL;

-- Gastos ingresos mensuales
ALTER TABLE gastos_ingresos_mensuales DROP COLUMN espacio_trabajo_id;
ALTER TABLE gastos_ingresos_mensuales RENAME COLUMN espacio_trabajo_id_uuid TO espacio_trabajo_id;
ALTER TABLE gastos_ingresos_mensuales ALTER COLUMN espacio_trabajo_id SET NOT NULL;

-- Espacios trabajo usuarios
ALTER TABLE espacios_trabajo_usuarios DROP COLUMN espacio_trabajo_id;
ALTER TABLE espacios_trabajo_usuarios RENAME COLUMN espacio_trabajo_id_uuid TO espacio_trabajo_id;
ALTER TABLE espacios_trabajo_usuarios ALTER COLUMN espacio_trabajo_id SET NOT NULL;

-- Paso 12: Recrear constraints de claves foráneas con ON DELETE apropiado
ALTER TABLE transacciones 
    ADD CONSTRAINT fk_transaccion_espacio_trabajo 
    FOREIGN KEY (espacio_trabajo_id) 
    REFERENCES espacios_trabajo(id) 
    ON DELETE CASCADE;

ALTER TABLE cuentas_bancarias 
    ADD CONSTRAINT fk_cuenta_bancaria_espacio_trabajo 
    FOREIGN KEY (id_espacio_trabajo) 
    REFERENCES espacios_trabajo(id) 
    ON DELETE CASCADE;

ALTER TABLE motivos_transaccion 
    ADD CONSTRAINT fk_motivo_espacio_trabajo 
    FOREIGN KEY (id_espacio_trabajo) 
    REFERENCES espacios_trabajo(id) 
    ON DELETE CASCADE;

ALTER TABLE contactos_transferencia 
    ADD CONSTRAINT fk_contacto_espacio_trabajo 
    FOREIGN KEY (id_espacio_trabajo) 
    REFERENCES espacios_trabajo(id) 
    ON DELETE CASCADE;

ALTER TABLE tarjetas 
    ADD CONSTRAINT fk_tarjeta_espacio_trabajo 
    FOREIGN KEY (espacio_trabajo_id) 
    REFERENCES espacios_trabajo(id) 
    ON DELETE CASCADE;

ALTER TABLE compras_credito 
    ADD CONSTRAINT fk_compra_credito_espacio_trabajo 
    FOREIGN KEY (espacio_trabajo_id) 
    REFERENCES espacios_trabajo(id) 
    ON DELETE CASCADE;

ALTER TABLE gastos_ingresos_mensuales 
    ADD CONSTRAINT fk_gastos_ingresos_espacio_trabajo 
    FOREIGN KEY (espacio_trabajo_id) 
    REFERENCES espacios_trabajo(id) 
    ON DELETE CASCADE;

ALTER TABLE espacios_trabajo_usuarios 
    ADD CONSTRAINT fk_espacios_trabajo_usuarios_espacio 
    FOREIGN KEY (espacio_trabajo_id) 
    REFERENCES espacios_trabajo(id) 
    ON DELETE CASCADE;

-- Paso 13: Recrear índices para optimizar rendimiento
CREATE INDEX IF NOT EXISTS idx_transacciones_espacio_trabajo 
    ON transacciones(espacio_trabajo_id);

CREATE INDEX IF NOT EXISTS idx_cuentas_bancarias_espacio_trabajo 
    ON cuentas_bancarias(id_espacio_trabajo);

CREATE INDEX IF NOT EXISTS idx_motivos_espacio_trabajo 
    ON motivos_transaccion(id_espacio_trabajo);

CREATE INDEX IF NOT EXISTS idx_contactos_espacio_trabajo 
    ON contactos_transferencia(id_espacio_trabajo);

CREATE INDEX IF NOT EXISTS idx_tarjetas_espacio_trabajo 
    ON tarjetas(espacio_trabajo_id);

CREATE INDEX IF NOT EXISTS idx_compras_credito_espacio_trabajo 
    ON compras_credito(espacio_trabajo_id);

CREATE INDEX IF NOT EXISTS idx_gastos_ingresos_espacio_trabajo 
    ON gastos_ingresos_mensuales(espacio_trabajo_id);

CREATE INDEX IF NOT EXISTS idx_espacios_trabajo_usuarios_espacio 
    ON espacios_trabajo_usuarios(espacio_trabajo_id);

-- Verificación: Comentarios informativos
COMMENT ON COLUMN espacios_trabajo.id IS 'Identificador único UUID del espacio de trabajo. Migrado desde BIGSERIAL por seguridad multi-tenant (IDOR prevention).';
COMMENT ON TABLE espacios_trabajo IS 'Tabla de espacios de trabajo. ID migrado a UUID en V12 para prevenir vulnerabilidades IDOR y mejorar seguridad multi-tenant.';
