package com.campito.backend.dto;

import java.util.UUID;

import com.campito.backend.validation.ValidDescripcion;
import com.campito.backend.validation.ValidNombre;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DescuentoDTORequest(
    @NotBlank(message = "El día no puede estar vacío")
    @Size(max = 10, message = "El día no puede exceder los 10 caracteres")
    String dia,

    @Size(max = 100, message = "La localidad no puede exceder los 100 caracteres")
    String localidad,

    @NotBlank(message = "El banco no puede estar vacío")
    @Size(max = 50, message = "El banco no puede exceder los 50 caracteres")
    String banco,

    @NotNull(message = "El campo modo no puede ser nulo")
    Boolean modo,

    @NotBlank(message = "El porcentaje no puede estar vacío")
    @Size(max = 4, message = "El porcentaje no puede exceder los 4 caracteres")
    String porcentaje,

    @NotBlank(message = "El comercio no puede estar vacío")
    @Size(max = 50, message = "El comercio no puede exceder los 50 caracteres")
    @ValidNombre
    String comercio,

    @NotBlank(message = "El modo de pago no puede estar vacío")
    @Size(max = 20, message = "El modo de pago no puede exceder los 20 caracteres")
    String modoPago,

    @Size(max = 13, message = "El tope de reintegro no puede exceder los 13 caracteres")
    String topeReintegro,

    @NotNull(message = "El campo esSemanal no puede ser nulo")
    Boolean esSemanal,

    @Size(max = 100, message = "El comentario no puede exceder los 100 caracteres")
    @ValidDescripcion
    String comentario,

    @NotNull(message = "El ID del espacio de trabajo no puede ser nulo")
    UUID idEspacioTrabajo
) {}
