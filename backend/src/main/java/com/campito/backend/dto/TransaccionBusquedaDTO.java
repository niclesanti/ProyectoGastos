package com.campito.backend.dto;

import java.util.UUID;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransaccionBusquedaDTO(
    @Min(value = 1, message = "El minimo valor es 1, es decir, enero")
    @Max(value = 12, message = "El maximo valor es 12, es decir, diciembre")
    Integer mes,
    @Min(value = 2000, message = "El año minimo es 2000")
    @Max(value = 2100, message = "El año maximo es 2100")
    Integer anio,
    @Size(max = 50, message = "El motivo no puede exceder los 50 caracteres")
    String motivo,
    @Size(max = 50, message = "El nombre de contacto no puede exceder los 50 caracteres")
    String contacto,
    @NotNull(message = "El ID del espacio de trabajo no puede ser nulo")
    UUID idEspacioTrabajo,
    @Min(value = 0, message = "El número de página debe ser mayor o igual a 0")
    Integer page,
    @Min(value = 1, message = "El tamaño de página debe ser al menos 1")
    @Max(value = 100, message = "El tamaño de página no puede exceder 100")
    Integer size
) {

}
