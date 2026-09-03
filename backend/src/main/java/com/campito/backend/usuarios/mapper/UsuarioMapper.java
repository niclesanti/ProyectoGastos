package com.campito.backend.usuarios.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.campito.backend.usuarios.domain.dto.UsuarioDTORequest;
import com.campito.backend.usuarios.domain.dto.UsuarioDTOResponse;
import com.campito.backend.config.MapstructConfig;
import com.campito.backend.usuarios.domain.entity.Usuario;

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

    /**
     * Convierte UsuarioDTORequest a Usuario Entity.
     * Solo mapea campos editables por el usuario (nombre, email, fotoPerfil).
     * Los campos id, proveedor, idProveedor, rol, activo, fechaRegistro
     * y fechaUltimoAcceso son ignorados porque son gestionados por el servidor.
     * 
     * @param dtoRequest DTO de solicitud con datos del usuario
     * @return Entidad Usuario con los campos del DTO mapeados
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "idProveedor", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaUltimoAcceso", ignore = true)
    Usuario toEntity(UsuarioDTORequest dtoRequest);
}
