package com.campito.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.campito.backend.dto.MotivoDTORequest;
import com.campito.backend.dto.MotivoDTOResponse;
import com.campito.backend.mapper.config.MapstructConfig;
import com.campito.backend.model.MotivoTransaccion;

/**
 * Mapper para conversión entre MotivoTransaccion Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface MotivoTransaccionMapper {

    /**
     * Convierte MotivoDTORequest a MotivoTransaccion Entity.
     * El ID se ignora automáticamente para nuevas entidades.
     * 
     * NOTA: La relación espacioTrabajo debe ser establecida DESPUÉS de llamar a este mapper
     * usando el método setter correspondiente de la entidad.
     * 
     * @param request DTO con datos del request
     * @return Entidad MotivoTransaccion sin ID (para INSERT)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "espacioTrabajo", ignore = true)
    MotivoTransaccion toEntity(MotivoDTORequest request);

    /**
     * Convierte MotivoTransaccion Entity a MotivoDTOResponse.
     * 
     * @param motivoTransaccion Entidad MotivoTransaccion
     * @return DTO de respuesta con todos los datos del motivo
     */
    MotivoDTOResponse toResponse(MotivoTransaccion motivoTransaccion);
}
