-- ====================================================================
-- Migración: V22__separar_schemas_por_modulo.sql
-- Descripción: Separa cada módulo funcional en su propio schema de BD.
--              Mueve tablas con ALTER TABLE ... SET SCHEMA (atómico, sin
--              copia de datos). Elimina FKs cross-módulo para desacoplar.
-- Fecha: 2026-09-02
-- ====================================================================

-- =====================================================
-- 1. Crear schemas destino
-- =====================================================
CREATE SCHEMA IF NOT EXISTS usuarios;
CREATE SCHEMA IF NOT EXISTS transacciones;
CREATE SCHEMA IF NOT EXISTS dashboard;
CREATE SCHEMA IF NOT EXISTS notificaciones;
CREATE SCHEMA IF NOT EXISTS descuentos;

-- =====================================================
-- 2. Eliminar FKs cross-módulo (todas apuntan a usuarios)
-- =====================================================
-- notificaciones → usuarios
ALTER TABLE notificaciones DROP CONSTRAINT IF EXISTS fk_notificacion_usuario;

-- transacciones → usuarios.espacios_trabajo
ALTER TABLE transacciones DROP CONSTRAINT IF EXISTS fk_transaccion_espacio_trabajo;

-- cuentas_bancarias → usuarios.espacios_trabajo
ALTER TABLE cuentas_bancarias DROP CONSTRAINT IF EXISTS fk_cuenta_bancaria_espacio_trabajo;

-- motivos_transaccion → usuarios.espacios_trabajo
ALTER TABLE motivos_transaccion DROP CONSTRAINT IF EXISTS fk_motivo_espacio_trabajo;

-- contactos_transferencia → usuarios.espacios_trabajo
ALTER TABLE contactos_transferencia DROP CONSTRAINT IF EXISTS fk_contacto_espacio_trabajo;

-- tarjetas → usuarios.espacios_trabajo
ALTER TABLE tarjetas DROP CONSTRAINT IF EXISTS fk_tarjeta_espacio_trabajo;

-- compras_credito → usuarios.espacios_trabajo
ALTER TABLE compras_credito DROP CONSTRAINT IF EXISTS fk_compra_credito_espacio_trabajo;

-- gastos_ingresos_mensuales → usuarios.espacios_trabajo
ALTER TABLE gastos_ingresos_mensuales DROP CONSTRAINT IF EXISTS fk_gastos_ingresos_espacio_trabajo;

-- descuentos → usuarios.espacios_trabajo
ALTER TABLE descuentos DROP CONSTRAINT IF EXISTS fk_descuentos_espacio_trabajo;

-- =====================================================
-- 3. Mover tablas a sus schemas (datos intactos, OIDs preservados)
-- =====================================================
-- NOTA: SET SCHEMA mueve la tabla + secuencias + índices + constraints.
--       Las FKs intra-módulo sobreviven porque PostgreSQL resuelve por OID.

-- --- usuarios (4 tablas) ---
ALTER TABLE public.usuarios SET SCHEMA usuarios;
ALTER TABLE public.espacios_trabajo SET SCHEMA usuarios;
ALTER TABLE public.espacios_trabajo_usuarios SET SCHEMA usuarios;
ALTER TABLE public.solicitudes_pendientes_espacio_trabajo SET SCHEMA usuarios;

-- --- transacciones (8 tablas) ---
ALTER TABLE public.transacciones SET SCHEMA transacciones;
ALTER TABLE public.motivos_transaccion SET SCHEMA transacciones;
ALTER TABLE public.contactos_transferencia SET SCHEMA transacciones;
ALTER TABLE public.cuentas_bancarias SET SCHEMA transacciones;
ALTER TABLE public.tarjetas SET SCHEMA transacciones;
ALTER TABLE public.compras_credito SET SCHEMA transacciones;
ALTER TABLE public.cuotas_credito SET SCHEMA transacciones;
ALTER TABLE public.resumenes SET SCHEMA transacciones;

-- --- dashboard (1 tabla) ---
ALTER TABLE public.gastos_ingresos_mensuales SET SCHEMA dashboard;

-- --- notificaciones (1 tabla) ---
ALTER TABLE public.notificaciones SET SCHEMA notificaciones;

-- --- descuentos (1 tabla) ---
ALTER TABLE public.descuentos SET SCHEMA descuentos;

-- =====================================================
-- Fin: public ahora solo contiene flyway_schema_history
-- =====================================================
