package com.campito.backend.mapper;

import org.mapstruct.Mapper;

import com.campito.backend.dto.UsuarioDTOResponse;
import com.campito.backend.mapper.config.MapstructConfig;
import com.campito.backend.model.Usuario;

/**
 * Mapper para conversión entre Usuario Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface UsuarioMapper {
    
    /**
     * Convierte Usuario Entity a UsuarioDTOResponse.
     * 
     * @param usuario Entidad Usuario
     * @return DTO de respuesta con los datos del usuario
     */
    UsuarioDTOResponse toResponse(Usuario usuario);
}
