-- Eliminar duplicados de motivos de transacción manteniendo solo el registro más antiguo (menor ID)
DELETE FROM public.motivos_transaccion
WHERE id NOT IN (
    SELECT MIN(id)
    FROM public.motivos_transaccion
    GROUP BY motivo, id_espacio_trabajo
);

-- Eliminar duplicados de contactos de transferencia manteniendo solo el registro más antiguo (menor ID)
DELETE FROM public.contactos_transferencia
WHERE id NOT IN (
    SELECT MIN(id)
    FROM public.contactos_transferencia
    GROUP BY nombre, id_espacio_trabajo
);

-- Agregar constraint UNIQUE para evitar duplicados de motivos de transacción en el mismo espacio de trabajo
ALTER TABLE public.motivos_transaccion
    ADD CONSTRAINT uk_motivo_espacio_trabajo UNIQUE (motivo, id_espacio_trabajo);

-- Agregar constraint UNIQUE para evitar duplicados de contactos de transferencia en el mismo espacio de trabajo
ALTER TABLE public.contactos_transferencia
    ADD CONSTRAINT uk_contacto_espacio_trabajo UNIQUE (nombre, id_espacio_trabajo);
