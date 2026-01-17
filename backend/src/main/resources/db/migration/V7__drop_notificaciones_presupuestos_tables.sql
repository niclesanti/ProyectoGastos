-- V7__drop_notificaciones_presupuestos_tables.sql
-- Eliminar tablas no utilizadas: notificaciones y presupuestos

-- Eliminar tabla de notificaciones
DROP TABLE IF EXISTS public.notificaciones CASCADE;

-- Eliminar secuencia asociada a notificaciones
DROP SEQUENCE IF EXISTS public.notificaciones_id_seq;

-- Eliminar tabla de presupuestos
DROP TABLE IF EXISTS public.presupuestos CASCADE;

-- Eliminar secuencia asociada a presupuestos
DROP SEQUENCE IF EXISTS public.presupuestos_id_seq;

-- Fin de V7
