package com.campito.backend.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EspacioTrabajoDTORequest(
    @NotBlank(message = "El nombre del espacio de trabajo no puede estar vacío")
    @Size(max = 50, message = "El nombre del espacio de trabajo no puede exceder los 50 caracteres")
    @Pattern(
        regexp = "^[a-zA-Z0-9,()\\-_/\\s]+$",
        message = "El nombre solo puede contener letras, números, coma, paréntesis, guiones, barra y espacios"
    )
    String nombre,
    @NotNull(message = "El ID del usuario administrador no puede ser nulo")
    UUID idUsuarioAdmin
) {

}
