package com.campito.backend.usuarios.domain.dto;

import jakarta.validation.constraints.NotNull;

public record ResponderSolicitudRequest(
    @NotNull(message = "La respuesta (aceptada o rechazada) es obligatoria")
    Boolean aceptada
) {}
