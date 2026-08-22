package com.campito.backend.descuentos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.campito.backend.descuentos.domain.dto.DescuentoDTORequest;
import com.campito.backend.descuentos.domain.dto.DescuentoDTOResponse;
import com.campito.backend.descuentos.mapper.config.MapstructConfig;
import com.campito.backend.descuentos.domain.entity.Descuento;

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
    Descuento toEntity(DescuentoDTORequest request);

    /**
     * Convierte Descuento Entity a DescuentoDTOResponse.
     *
     * @param descuento Entidad Descuento
     * @return DTO de respuesta con todos los datos del descuento
     */
    DescuentoDTOResponse toResponse(Descuento descuento);
}
