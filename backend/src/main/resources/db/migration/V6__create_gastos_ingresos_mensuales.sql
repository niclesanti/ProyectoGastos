-- V6__create_gastos_ingresos_mensuales.sql
-- Crear tabla para almacenar totales mensuales de gastos e ingresos por espacio de trabajo

CREATE TABLE public.gastos_ingresos_mensuales (
    id BIGSERIAL PRIMARY KEY,
    anio INT NOT NULL,
    mes INT NOT NULL,
    gastos REAL NOT NULL DEFAULT 0,
    ingresos REAL NOT NULL DEFAULT 0,
    espacio_trabajo_id BIGINT NOT NULL,
    CONSTRAINT fk_gastos_ingresos_espacio_trabajo FOREIGN KEY (espacio_trabajo_id) REFERENCES public.espacios_trabajo(id),
    CONSTRAINT uk_gastos_ingresos_espacio_periodo UNIQUE (espacio_trabajo_id, anio, mes),
    CONSTRAINT ck_gastos_ingresos_mes CHECK (mes >= 1 AND mes <= 12)
);

-- Índices para consultas por espacio y periodo
CREATE INDEX idx_gastos_ingresos_espacio_periodo ON public.gastos_ingresos_mensuales(espacio_trabajo_id, anio, mes);
CREATE INDEX idx_gastos_ingresos_anio_mes ON public.gastos_ingresos_mensuales(anio, mes);

-- Fin de V6
