-- ====================================================================
-- Migración: V21__nombre_espacio_trabajo_denormalizado.sql
-- Descripción: Agrega la columna nombre_espacio_trabajo desnormalizada
--              a transacciones y compras_credito para eliminar la
--              referencia a la entidad EspacioTrabajo (modulith).
--              Incluye backfill de datos existentes.
-- ====================================================================

ALTER TABLE transacciones ADD COLUMN nombre_espacio_trabajo VARCHAR(50);
ALTER TABLE compras_credito ADD COLUMN nombre_espacio_trabajo VARCHAR(50);

UPDATE transacciones t
SET nombre_espacio_trabajo = et.nombre
FROM espacios_trabajo et
WHERE et.id = t.espacio_trabajo_id;

UPDATE compras_credito c
SET nombre_espacio_trabajo = et.nombre
FROM espacios_trabajo et
WHERE et.id = c.espacio_trabajo_id;

ALTER TABLE transacciones ALTER COLUMN nombre_espacio_trabajo SET NOT NULL;
ALTER TABLE compras_credito ALTER COLUMN nombre_espacio_trabajo SET NOT NULL;
