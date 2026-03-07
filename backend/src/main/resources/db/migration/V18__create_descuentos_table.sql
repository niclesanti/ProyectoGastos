-- Crear tabla de descuentos
CREATE TABLE descuentos (
    id                 BIGSERIAL PRIMARY KEY,
    dia                VARCHAR(10)  NOT NULL,
    localidad          VARCHAR(100),
    banco              VARCHAR(50)  NOT NULL,
    modo               BOOLEAN      NOT NULL DEFAULT FALSE,
    porcentaje         VARCHAR(4)   NOT NULL,
    comercio           VARCHAR(50)  NOT NULL,
    modo_pago          VARCHAR(20)  NOT NULL,
    tope_reintegro     VARCHAR(13),
    es_semanal         BOOLEAN      NOT NULL DEFAULT TRUE,
    comentario         VARCHAR(100),
    espacio_trabajo_id UUID         NOT NULL,
    CONSTRAINT fk_descuentos_espacio_trabajo
        FOREIGN KEY (espacio_trabajo_id)
        REFERENCES espacios_trabajo(id)
        ON DELETE CASCADE
);

-- Índice para búsquedas frecuentes por espacio de trabajo
CREATE INDEX idx_descuentos_espacio_trabajo ON descuentos(espacio_trabajo_id);
