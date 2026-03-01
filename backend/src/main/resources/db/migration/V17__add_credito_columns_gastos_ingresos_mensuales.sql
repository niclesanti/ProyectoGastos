-- Agregar columnas para trackear compras con crédito y pagos de resúmenes mensuales
ALTER TABLE gastos_ingresos_mensuales
    ADD COLUMN compras_credito NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN pago_resumen    NUMERIC(15,2) NOT NULL DEFAULT 0.00;

-- Asegurar que los registros existentes queden con valor 0.00
UPDATE gastos_ingresos_mensuales
SET compras_credito = 0.00,
    pago_resumen    = 0.00
WHERE compras_credito IS NULL
   OR pago_resumen IS NULL;
