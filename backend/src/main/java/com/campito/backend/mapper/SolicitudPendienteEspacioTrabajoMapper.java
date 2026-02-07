package com.campito.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.campito.backend.dto.SolicitudPendienteEspacioTrabajoDTOResponse;
import com.campito.backend.mapper.config.MapstructConfig;
import com.campito.backend.model.SolicitudPendienteEspacioTrabajo;

/**
 * Mapper para conversión entre SolicitudPendienteEspacioTrabajo Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface SolicitudPendienteEspacioTrabajoMapper {
    /**
     * Convierte SolicitudPendienteEspacioTrabajo Entity a SolicitudPendienteEspacioTrabajoDTOResponse.
     * 
     * @param solicitud Entidad SolicitudPendienteEspacioTrabajo
     * @return DTO de respuesta con los datos de la solicitud pendiente
     */
    @Mapping(target = "espacioTrabajoNombre", source = "espacioTrabajo.nombre")
    @Mapping(target = "usuarioAdminNombre", source = "espacioTrabajo.usuarioAdmin.nombre")
    @Mapping(target = "fotoPerfilUsuarioAdmin", source = "espacioTrabajo.usuarioAdmin.fotoPerfil")
    SolicitudPendienteEspacioTrabajoDTOResponse toResponse(SolicitudPendienteEspacioTrabajo solicitud);
}
