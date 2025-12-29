package com.campito.backend.dto;

import com.campito.backend.validation.ValidNombre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MotivoDTORequest(
    @Size(max = 50, message = "El motivo no puede exceder los 50 caracteres")
    @NotBlank(message = "El motivo no puede estar vacío")
    @ValidNombre
    String motivo,
    @NotNull(message = "El ID del espacio de trabajo no puede ser nulo")
    Long idEspacioTrabajo
) {

}
