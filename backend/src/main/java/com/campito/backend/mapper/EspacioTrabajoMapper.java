package com.campito.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.campito.backend.dto.EspacioTrabajoDTORequest;
import com.campito.backend.dto.EspacioTrabajoDTOResponse;
import com.campito.backend.mapper.config.MapstructConfig;
import com.campito.backend.model.EspacioTrabajo;

/**
 * Mapper para conversión entre EspacioTrabajo Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface EspacioTrabajoMapper {

    /**
     * Convierte EspacioTrabajoDTORequest a EspacioTrabajo Entity.
     * El ID se ignora automáticamente para nuevas entidades.
     * El saldo inicial se establece en 0.0.
     * 
     * NOTA: Las relaciones usuarioAdmin y usuariosParticipantes deben ser establecidas DESPUÉS de llamar a este mapper
     * usando los métodos setters correspondientes de la entidad.
     * 
     * @param request DTO con datos del request
     * @return Entidad EspacioTrabajo sin ID (para INSERT)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "saldo", ignore = true)
    @Mapping(target = "usuarioAdmin", ignore = true)
    @Mapping(target = "usuariosParticipantes", ignore = true)
    EspacioTrabajo toEntity(EspacioTrabajoDTORequest request);

    /**
     * Convierte EspacioTrabajo Entity a EspacioTrabajoDTOResponse.
     * 
     * La relación usuarioAdmin se mapea a su ID correspondiente.
     * 
     * @param espacioTrabajo Entidad EspacioTrabajo
     * @return DTO de respuesta con todos los datos del espacio de trabajo
     */
    @Mapping(target = "usuarioAdminId", source = "usuarioAdmin.id")
    EspacioTrabajoDTOResponse toResponse(EspacioTrabajo espacioTrabajo);
}
