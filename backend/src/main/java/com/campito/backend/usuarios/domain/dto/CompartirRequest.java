package com.campito.backend.usuarios.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompartirRequest(
    @NotBlank(message = "El email no puede estar vacío")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    @Email(message = "Debe proporcionar un email válido")
    @Pattern(
        regexp = "^[a-zA-Z0-9@.\\-_]+$",
        message = "El email solo puede contener letras, números, @, punto, guiones y barra baja"
    )
    String email
) {}
