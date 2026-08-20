package com.campito.backend.dto;

import java.util.UUID;
import com.campito.backend.validation.ValidNombre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EspacioTrabajoDTORequest(
    @NotBlank(message = "El nombre del espacio de trabajo no puede estar vacío")
    @Size(max = 50, message = "El nombre del espacio de trabajo no puede exceder los 50 caracteres")
    @ValidNombre(message = "El nombre solo puede contener letras (incluidas acentuadas), números, coma, paréntesis, guiones, barra y espacios")
    String nombre,
    @NotNull(message = "El ID del usuario administrador no puede ser nulo")
    UUID idUsuarioAdmin
) {

}
