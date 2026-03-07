package com.campito.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.campito.backend.dto.DescuentoDTORequest;
import com.campito.backend.dto.DescuentoDTOResponse;
import com.campito.backend.mapper.config.MapstructConfig;
import com.campito.backend.model.Descuento;

/**
 * Mapper para conversión entre Descuento Entity y DTOs.
 *
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface DescuentoMapper {

    /**
     * Convierte DescuentoDTORequest a Descuento Entity.
     * El ID y la relación espacioTrabajo se establecen por la lógica de negocio.
     *
     * @param request DTO con datos del request
     * @return Entidad Descuento sin ID (para INSERT)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "espacioTrabajo", ignore = true)
    Descuento toEntity(DescuentoDTORequest request);

    /**
     * Convierte Descuento Entity a DescuentoDTOResponse.
     *
     * @param descuento Entidad Descuento
     * @return DTO de respuesta con todos los datos del descuento
     */
    @Mapping(target = "idEspacioTrabajo", source = "espacioTrabajo.id")
    DescuentoDTOResponse toResponse(Descuento descuento);
}
