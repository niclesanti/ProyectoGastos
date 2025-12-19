CREATE TABLE tarjetas (
    id BIGSERIAL PRIMARY KEY,
    numero_tarjeta VARCHAR(4) NOT NULL,
    entidad_financiera VARCHAR(50) NOT NULL,
    red_de_pago VARCHAR(50) NOT NULL,
    dia_cierre INT NOT NULL,
    dia_vencimiento_pago INT NOT NULL,
    espacio_trabajo_id BIGINT NOT NULL,
    CONSTRAINT fk_tarjetas_espacio_trabajo FOREIGN KEY (espacio_trabajo_id) REFERENCES espacios_trabajo(id)
);

CREATE TABLE compras_credito (
    id BIGSERIAL PRIMARY KEY,
    fecha_compra DATE NOT NULL,
    monto_total FLOAT NOT NULL,
    cantidad_cuotas INT NOT NULL,
    cuotas_pagadas INT NOT NULL,
    descripcion VARCHAR(100),
    nombre_completo_auditoria VARCHAR(100) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    espacio_trabajo_id BIGINT NOT NULL,
    motivo_transaccion_id BIGINT NOT NULL,
    comercio_id BIGINT,
    tarjeta_id BIGINT NOT NULL,
    CONSTRAINT fk_compras_credito_espacio_trabajo FOREIGN KEY (espacio_trabajo_id) REFERENCES espacios_trabajo(id),
    CONSTRAINT fk_compras_credito_motivo FOREIGN KEY (motivo_transaccion_id) REFERENCES motivos_transaccion(id),
    CONSTRAINT fk_compras_credito_comercio FOREIGN KEY (comercio_id) REFERENCES contactos_transferencia(id),
    CONSTRAINT fk_compras_credito_tarjeta FOREIGN KEY (tarjeta_id) REFERENCES tarjetas(id)
);

CREATE TABLE cuotas_credito (
    id BIGSERIAL PRIMARY KEY,
    numero_cuota INT NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    monto_cuota FLOAT NOT NULL,
    pagada BOOLEAN NOT NULL,
    compra_credito_id BIGINT NOT NULL,
    transaccion_id BIGINT,
    CONSTRAINT fk_cuotas_credito_compra FOREIGN KEY (compra_credito_id) REFERENCES compras_credito(id),
    CONSTRAINT fk_cuotas_credito_transaccion FOREIGN KEY (transaccion_id) REFERENCES transacciones(id)
);
