package com.campito.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.campito.backend.dto.TransaccionDTORequest;
import com.campito.backend.dto.TransaccionDTOResponse;
import com.campito.backend.mapper.config.MapstructConfig;
import com.campito.backend.model.Transaccion;

/**
 * Mapper para conversión entre Transaccion Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface TransaccionMapper {

    /**
     * Convierte TransaccionDTORequest a Transaccion Entity.
     * El ID se ignora automáticamente para nuevas entidades.
     * El campo fechaCreacion se maneja por lógica de negocio.
     * 
     * NOTA: Las relaciones (@ManyToOne) deben ser establecidas DESPUÉS de llamar a este mapper
     * usando los métodos setters correspondientes de la entidad.
     * 
     * @param request DTO con datos del request
     * @return Entidad Transaccion sin ID (para INSERT)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "espacioTrabajo", ignore = true)
    @Mapping(target = "motivo", ignore = true)
    @Mapping(target = "contacto", ignore = true)
    @Mapping(target = "cuentaBancaria", ignore = true)
    Transaccion toEntity(TransaccionDTORequest request);

    /**
     * Convierte Transaccion Entity a TransaccionDTOResponse.
     * 
     * Las relaciones @ManyToOne se mapean a sus IDs y nombres correspondientes.
     * 
     * @param transaccion Entidad Transaccion
     * @return DTO de respuesta con todos los datos de la transacción
     */
    @Mapping(target = "idEspacioTrabajo", source = "espacioTrabajo.id")
    @Mapping(target = "nombreEspacioTrabajo", source = "espacioTrabajo.nombre")
    @Mapping(target = "idMotivo", source = "motivo.id")
    @Mapping(target = "nombreMotivo", source = "motivo.motivo")
    @Mapping(target = "idContacto", source = "contacto.id")
    @Mapping(target = "nombreContacto", source = "contacto.nombre")
    @Mapping(target = "nombreCuentaBancaria", source = "cuentaBancaria.nombre")
    TransaccionDTOResponse toResponse(Transaccion transaccion);
}
