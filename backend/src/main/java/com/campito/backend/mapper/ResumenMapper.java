package com.campito.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.campito.backend.dto.ResumenDTOResponse;
import com.campito.backend.mapper.config.MapstructConfig;
import com.campito.backend.model.Resumen;

/**
 * Mapper para conversión entre Resumen Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface ResumenMapper {

    /**
     * Convierte Resumen Entity a ResumenDTOResponse.
     * 
     * Las relaciones @ManyToOne se mapean a sus IDs correspondientes.
     * Los campos cuotas y cantidadCuotas deben ser establecidos manualmente en el servicio.
     * 
     * @param resumen Entidad Resumen
     * @return DTO de respuesta con datos del resumen (sin cuotas)
     */
    @Mapping(target = "idTarjeta", source = "tarjeta.id")
    @Mapping(target = "numeroTarjeta", source = "tarjeta.numeroTarjeta")
    @Mapping(target = "entidadFinanciera", source = "tarjeta.entidadFinanciera")
    @Mapping(target = "redDePago", source = "tarjeta.redDePago")
    @Mapping(target = "idTransaccionAsociada", source = "transaccionAsociada.id")
    @Mapping(target = "cuotas", ignore = true)
    @Mapping(target = "cantidadCuotas", ignore = true)
    ResumenDTOResponse toResponse(Resumen resumen);
}
