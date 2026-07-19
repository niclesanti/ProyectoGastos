package com.campito.backend.dto;

import jakarta.validation.constraints.NotNull;

public record ResponderSolicitudRequest(
    @NotNull(message = "La respuesta (aceptada o rechazada) es obligatoria")
    Boolean aceptada
) {}
