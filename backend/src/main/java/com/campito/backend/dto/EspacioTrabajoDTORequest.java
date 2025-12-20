package com.campito.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EspacioTrabajoDTORequest(
    @NotBlank(message = "El nombre del espacio de trabajo no puede estar vacío")
    @Size(max = 50, message = "El nombre del espacio de trabajo no puede exceder los 50 caracteres")
    String nombre,
    @NotNull(message = "El ID del usuario administrador no puede ser nulo")
    Long idUsuarioAdmin
) {

}
