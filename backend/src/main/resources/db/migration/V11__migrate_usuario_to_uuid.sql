-- ====================================================================
-- Migración: V11__migrate_usuario_to_uuid.sql
-- Descripción: Migra el campo id de la tabla usuarios de BIGSERIAL a UUID
--              Actualiza todas las referencias de clave foránea
-- Fecha: 2026-01-22
-- Criticidad: ALTA - Cambio de seguridad para prevenir IDOR
-- ====================================================================

-- IMPORTANTE: Esta migración requiere que NO existan datos en las tablas
-- Si ya existen datos, se debe hacer una migración más compleja con mapeo de IDs

-- Paso 1: Eliminar ALL constraints de claves foráneas que referencian usuarios
-- Usamos CASCADE para eliminar automáticamente todas las dependencias
ALTER TABLE espacios_trabajo DROP CONSTRAINT IF EXISTS fk_usuario_admin CASCADE;
ALTER TABLE espacios_trabajo DROP CONSTRAINT IF EXISTS fkhete5fw8leajay2mcr3gw18rm CASCADE;
ALTER TABLE espacios_trabajo_usuarios DROP CONSTRAINT IF EXISTS fk_espacios_trabajo_usuarios_usuario CASCADE;
ALTER TABLE espacios_trabajo_usuarios DROP CONSTRAINT IF EXISTS fkgy1o8u3vlkkky6pv8quaq8sx5 CASCADE;

-- Paso 2: Agregar nueva columna UUID en la tabla usuarios
ALTER TABLE usuarios ADD COLUMN id_uuid UUID DEFAULT gen_random_uuid();

-- Paso 3: Poblar UUIDs para usuarios existentes (si los hay)
UPDATE usuarios SET id_uuid = gen_random_uuid() WHERE id_uuid IS NULL;

-- Paso 4: Hacer la columna UUID NOT NULL
ALTER TABLE usuarios ALTER COLUMN id_uuid SET NOT NULL;

-- Paso 5: Agregar columnas UUID en tablas que referencian usuarios
ALTER TABLE espacios_trabajo ADD COLUMN usuario_admin_id_uuid UUID;
ALTER TABLE espacios_trabajo_usuarios ADD COLUMN usuario_id_uuid UUID;

-- Paso 6: Migrar datos de las FKs (mapear Long a UUID)
-- Actualizar usuario_admin_id en espacios_trabajo
UPDATE espacios_trabajo et
SET usuario_admin_id_uuid = (
    SELECT u.id_uuid 
    FROM usuarios u 
    WHERE u.id = et.usuario_admin_id
);

-- Actualizar usuario_id en espacios_trabajo_usuarios
UPDATE espacios_trabajo_usuarios etu
SET usuario_id_uuid = (
    SELECT u.id_uuid 
    FROM usuarios u 
    WHERE u.id = etu.usuario_id
);

-- Paso 7: Eliminar la constraint de clave primaria antigua
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_pkey;

-- Paso 8: Eliminar columna id antigua de usuarios
ALTER TABLE usuarios DROP COLUMN id;

-- Paso 9: Renombrar columna id_uuid a id
ALTER TABLE usuarios RENAME COLUMN id_uuid TO id;

-- Paso 10: Establecer nueva clave primaria con UUID
ALTER TABLE usuarios ADD PRIMARY KEY (id);

-- Paso 11: Eliminar columnas FK antiguas en tablas relacionadas
ALTER TABLE espacios_trabajo DROP COLUMN usuario_admin_id;
ALTER TABLE espacios_trabajo_usuarios DROP COLUMN usuario_id;

-- Paso 12: Renombrar columnas UUID en tablas relacionadas
ALTER TABLE espacios_trabajo RENAME COLUMN usuario_admin_id_uuid TO usuario_admin_id;
ALTER TABLE espacios_trabajo_usuarios RENAME COLUMN usuario_id_uuid TO usuario_id;

-- Paso 13: Hacer NOT NULL las nuevas columnas FK
ALTER TABLE espacios_trabajo ALTER COLUMN usuario_admin_id SET NOT NULL;
ALTER TABLE espacios_trabajo_usuarios ALTER COLUMN usuario_id SET NOT NULL;

-- Paso 14: Recrear constraints de claves foráneas
ALTER TABLE espacios_trabajo 
    ADD CONSTRAINT fk_usuario_admin 
    FOREIGN KEY (usuario_admin_id) 
    REFERENCES usuarios(id) 
    ON DELETE RESTRICT;

ALTER TABLE espacios_trabajo_usuarios 
    ADD CONSTRAINT fk_espacios_trabajo_usuarios_usuario 
    FOREIGN KEY (usuario_id) 
    REFERENCES usuarios(id) 
    ON DELETE CASCADE;

-- Paso 15: Recrear índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_espacios_trabajo_usuario_admin 
    ON espacios_trabajo(usuario_admin_id);

CREATE INDEX IF NOT EXISTS idx_espacios_trabajo_usuarios_usuario 
    ON espacios_trabajo_usuarios(usuario_id);

-- Verificación: Comentarios informativos
COMMENT ON COLUMN usuarios.id IS 'Identificador único UUID del usuario. Migrado desde BIGSERIAL por seguridad (IDOR prevention).';
COMMENT ON TABLE usuarios IS 'Tabla de usuarios del sistema. ID migrado a UUID en V11 para prevenir vulnerabilidades IDOR.';
