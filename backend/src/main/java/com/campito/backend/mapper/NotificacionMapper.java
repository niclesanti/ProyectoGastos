package com.campito.backend.mapper;

import com.campito.backend.dto.NotificacionDTOResponse;
import com.campito.backend.mapper.config.MapstructConfig;
import com.campito.backend.model.Notificacion;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper para la conversión entre entidad Notificacion y su DTO.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface NotificacionMapper {
    
    /**
     * Convierte una entidad Notificacion a su DTO de respuesta.
     * 
     * @param notificacion Entidad a convertir
     * @return DTO de respuesta
     */
    NotificacionDTOResponse toResponse(Notificacion notificacion);
    
    /**
     * Convierte una lista de entidades Notificacion a una lista de DTOs.
     * 
     * @param notificaciones Lista de entidades a convertir
     * @return Lista de DTOs de respuesta
     */
    List<NotificacionDTOResponse> toResponseList(List<Notificacion> notificaciones);
}
