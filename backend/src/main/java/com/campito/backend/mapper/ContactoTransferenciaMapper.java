package com.campito.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.campito.backend.dto.ContactoDTORequest;
import com.campito.backend.dto.ContactoDTOResponse;
import com.campito.backend.mapper.config.MapstructConfig;
import com.campito.backend.model.ContactoTransferencia;

/**
 * Mapper para conversión entre Contacto Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface ContactoTransferenciaMapper {

    /**
     * Convierte ContactoDTORequest a ContactoTransferencia Entity.
     * El ID se ignora automáticamente para nuevas entidades.
     * 
     * NOTA: La relación espacioTrabajo debe ser establecida DESPUÉS de llamar a este mapper
     * usando el método setter correspondiente de la entidad.
     * 
     * @param request DTO con datos del request
     * @return Entidad ContactoTransferencia sin ID (para INSERT)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "espacioTrabajo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    ContactoTransferencia toEntity(ContactoDTORequest request);

    /**
     * Convierte ContactoTransferencia Entity a ContactoDTOResponse.
     * 
     * @param contacto Entidad ContactoTransferencia
     * @return DTO de respuesta con todos los datos del contacto
     */
    ContactoDTOResponse toResponse(ContactoTransferencia contacto);
}
