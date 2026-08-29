package com.campito.backend.transacciones.domain.dto;

import java.util.UUID;
import com.campito.backend.validation.ValidNombre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContactoDTORequest(
    @Size(max = 50, message = "El nombre de contacto no puede exceder los 50 caracteres")
    @NotBlank(message = "El nombre de contacto no puede estar vacío")
    @ValidNombre
    String nombre,
    @NotNull(message = "El ID del espacio de trabajo no puede ser nulo")
    UUID idEspacioTrabajo
) {

}
