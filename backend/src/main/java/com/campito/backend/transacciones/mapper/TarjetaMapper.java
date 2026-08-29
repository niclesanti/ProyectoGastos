package com.campito.backend.transacciones.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.campito.backend.transacciones.domain.dto.TarjetaDTORequest;
import com.campito.backend.transacciones.domain.dto.TarjetaDTOResponse;
import com.campito.backend.transacciones.domain.dto.TarjetaDTOUpdate;
import com.campito.backend.mapper.config.MapstructConfig;
import com.campito.backend.transacciones.domain.entity.Tarjeta;

/**
 * Mapper para conversión entre Tarjeta Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface TarjetaMapper {

    /**
     * Convierte TarjetaDTORequest a Tarjeta Entity.
     * El ID se ignora automáticamente para nuevas entidades.
     * 
     * NOTA: La relación espacioTrabajo (@ManyToOne) debe ser establecida DESPUÉS de llamar a este mapper
     * usando el método setter correspondiente de la entidad.
     * 
     * @param request DTO con datos del request
     * @return Entidad Tarjeta sin ID (para INSERT)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "espacioTrabajo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    Tarjeta toEntity(TarjetaDTORequest request);

    /**
     * Convierte Tarjeta Entity a TarjetaDTOResponse.
     * 
     * La relación @ManyToOne espacioTrabajo se mapea a su ID correspondiente.
     * 
     * @param tarjeta Entidad Tarjeta
     * @return DTO de respuesta con todos los datos de la tarjeta
     */
    @Mapping(target = "espacioTrabajoId", source = "espacioTrabajo.id")
    TarjetaDTOResponse toResponse(Tarjeta tarjeta);

    /**
     * Actualiza los campos editables de una Tarjeta existente desde TarjetaDTOUpdate.
     * Solo actualiza diaCierre y diaVencimientoPago; el resto se ignora.
     *
     * @param dto DTO con los campos a actualizar
     * @param tarjeta Entidad a actualizar (MappingTarget)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "espacioTrabajo", ignore = true)
    @Mapping(target = "numeroTarjeta", ignore = true)
    @Mapping(target = "entidadFinanciera", ignore = true)
    @Mapping(target = "redDePago", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    void updateEntity(TarjetaDTOUpdate dto, @MappingTarget Tarjeta tarjeta);
}
