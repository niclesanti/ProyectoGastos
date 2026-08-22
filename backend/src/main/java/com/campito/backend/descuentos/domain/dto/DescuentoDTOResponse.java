package com.campito.backend.descuentos.domain.dto;

import java.util.UUID;

public record DescuentoDTOResponse(
    Long id,
    String dia,
    String localidad,
    String banco,
    Boolean modo,
    String porcentaje,
    String comercio,
    String modoPago,
    String topeReintegro,
    Boolean esSemanal,
    String comentario,
    UUID idEspacioTrabajo
) {}
