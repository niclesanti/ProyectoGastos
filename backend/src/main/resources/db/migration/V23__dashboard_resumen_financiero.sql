-- ====================================================================
-- Migración: V23__dashboard_resumen_financiero.sql
-- Descripción: Crea el read-model desnormalizado del dashboard que
--              consolida el saldo y la deuda total de tarjetas por espacio
--              de trabajo, con backfill de los datos existentes.
--              Mantenido de forma SÍNCRONA vía eventos de módulos productores.
-- Fecha: 2026-09-02
-- ====================================================================

-- =====================================================
-- 1. Crear la tabla del read-model
-- =====================================================
CREATE SCHEMA IF NOT EXISTS dashboard;

CREATE TABLE dashboard.resumen_financiero (
    espacio_trabajo_id UUID PRIMARY KEY,
    saldo NUMERIC(15,2) NOT NULL DEFAULT 0,
    deuda_total NUMERIC(15,2) NOT NULL DEFAULT 0,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT now()
);

-- =====================================================
-- 2. Backfill de datos existentes
--    - saldo: copia exacta de usuarios.espacios_trabajo.saldo
--    - deuda: SUM(monto_cuota) de cuotas NO pagadas (pagada = false),
--      usando monto_cuota (NO monto_total) por el redondeo HALF_UP de las cuotas.
--    - LEFT JOIN + COALESCE para garantizar fila con deuda 0 en espacios
--      que no tienen deuda pendiente.
-- =====================================================
INSERT INTO dashboard.resumen_financiero (espacio_trabajo_id, saldo, deuda_total, fecha_actualizacion)
SELECT
    e.id AS espacio_trabajo_id,
    e.saldo,
    COALESCE(c.deuda_total, 0) AS deuda_total,
    now() AS fecha_actualizacion
FROM usuarios.espacios_trabajo e
LEFT JOIN (
    SELECT
        cc.espacio_trabajo_id,
        SUM(cq.monto_cuota) AS deuda_total
    FROM transacciones.cuotas_credito cq
    JOIN transacciones.compras_credito cc ON cq.compra_credito_id = cc.id
    WHERE cq.pagada = false
    GROUP BY cc.espacio_trabajo_id
) c ON c.espacio_trabajo_id = e.id
ORDER BY e.id;

-- Fin de V23
