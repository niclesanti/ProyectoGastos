-- V5__Optimizacion_Indices_Rendimiento.sql
-- Optimización del rendimiento mediante índices estratégicos
-- No modifica la estructura de las tablas, solo agrega índices

-- 1. Índice Compuesto para consultas del Dashboard
-- Optimiza las queries: WHERE espacio_trabajo_id = :id AND fecha >= :limite
-- Este índice es crucial para acelerar las consultas más frecuentes del dashboard
CREATE INDEX idx_transacciones_dashboard 
ON transacciones (espacio_trabajo_id, fecha, tipo);

-- 2. Índice Funcional para optimizar EXTRACT(YEAR/MONTH)
-- Optimiza las queries que usan EXTRACT(MONTH FROM fecha) y EXTRACT(YEAR FROM fecha)
-- PostgreSQL puede usar este índice cuando se hacen agregaciones por mes/año
CREATE INDEX idx_transacciones_fecha_extract 
ON transacciones (EXTRACT(YEAR FROM fecha), EXTRACT(MONTH FROM fecha));

-- 3. Índice Parcial para Cuotas Pendientes
-- Optimiza las queries que buscan cuotas no pagadas
-- El índice parcial (WHERE pagada = false) ocupa menos espacio y es más eficiente
CREATE INDEX idx_cuotas_pendientes 
ON cuotas_credito (pagada) WHERE pagada = false;
