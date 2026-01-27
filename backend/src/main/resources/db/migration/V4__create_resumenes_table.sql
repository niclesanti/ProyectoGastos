-- Crear tabla de resúmenes de tarjeta
CREATE TABLE resumenes (
    id BIGSERIAL PRIMARY KEY,
    anio INT NOT NULL,
    mes INT NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    monto_total FLOAT NOT NULL,
    tarjeta_id BIGINT NOT NULL,
    transaccion_id BIGINT,
    CONSTRAINT fk_resumenes_tarjeta FOREIGN KEY (tarjeta_id) REFERENCES tarjetas(id),
    CONSTRAINT fk_resumenes_transaccion FOREIGN KEY (transaccion_id) REFERENCES transacciones(id),
    CONSTRAINT uk_resumen_tarjeta_periodo UNIQUE (tarjeta_id, anio, mes)
);

-- Agregar índices para mejorar el rendimiento
CREATE INDEX idx_resumenes_tarjeta ON resumenes(tarjeta_id);
CREATE INDEX idx_resumenes_estado ON resumenes(estado);
CREATE INDEX idx_resumenes_fecha_vencimiento ON resumenes(fecha_vencimiento);

-- Agregar columna resumen_id a la tabla cuotas_credito
ALTER TABLE cuotas_credito 
ADD COLUMN resumen_id BIGINT,
ADD CONSTRAINT fk_cuotas_credito_resumen FOREIGN KEY (resumen_id) REFERENCES resumenes(id);

-- Agregar índice para la nueva relación
CREATE INDEX idx_cuotas_credito_resumen ON cuotas_credito(resumen_id);

-- Eliminar la columna transaccion_id de cuotas_credito ya que ahora el pago se hace por resumen
ALTER TABLE cuotas_credito DROP COLUMN IF EXISTS transaccion_id;
