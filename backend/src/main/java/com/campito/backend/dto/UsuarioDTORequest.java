package com.campito.backend.dto;

import com.campito.backend.validation.ValidNombre;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioDTORequest(
    @NotBlank(message = "El nombre del usuario no puede estar vacío")
    @Size(max = 100, message = "El nombre del usuario no puede exceder los 100 caracteres")
    @ValidNombre
    String nombre,

    @NotBlank(message = "El email del usuario no puede estar vacío")
    @Size(max = 100, message = "El email del usuario no puede exceder los 100 caracteres")
    @Email(message = "El email debe tener un formato válido")
    String email,

    @Size(max = 256, message = "La URL de foto de perfil no puede exceder los 256 caracteres")
    String fotoPerfil
) {

}
