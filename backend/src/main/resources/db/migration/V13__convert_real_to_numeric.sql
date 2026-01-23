-- ================================================================
-- Migración: Conversión de columnas REAL a NUMERIC(15,2)
-- Descripción: Mejora la precisión de montos y saldos financieros
-- Author: Sistema de Migración
-- Date: 2026-01-23
-- ================================================================

-- Tabla: transacciones
ALTER TABLE transacciones 
    ALTER COLUMN monto TYPE NUMERIC(15,2);

-- Tabla: compras_credito
ALTER TABLE compras_credito 
    ALTER COLUMN monto_total TYPE NUMERIC(15,2);

-- Tabla: cuotas_credito
ALTER TABLE cuotas_credito 
    ALTER COLUMN monto_cuota TYPE NUMERIC(15,2);

-- Tabla: cuentas_bancarias
ALTER TABLE cuentas_bancarias 
    ALTER COLUMN saldo_actual TYPE NUMERIC(15,2);

-- Tabla: espacios_trabajo
ALTER TABLE espacios_trabajo 
    ALTER COLUMN saldo TYPE NUMERIC(15,2);

-- Tabla: gastos_ingresos_mensuales
ALTER TABLE gastos_ingresos_mensuales 
    ALTER COLUMN gastos TYPE NUMERIC(15,2),
    ALTER COLUMN ingresos TYPE NUMERIC(15,2);

-- Tabla: resumenes
ALTER TABLE resumenes 
    ALTER COLUMN monto_total TYPE NUMERIC(15,2);

-- Nota: PostgreSQL maneja automáticamente la conversión de REAL a NUMERIC
-- Los índices existentes se recrean automáticamente si es necesario
