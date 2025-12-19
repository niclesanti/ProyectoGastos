package com.campito.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.campito.backend.dto.CompraCreditoDTORequest;
import com.campito.backend.dto.CompraCreditoDTOResponse;
import com.campito.backend.mapper.config.MapstructConfig;
import com.campito.backend.model.CompraCredito;

/**
 * Mapper para conversión entre CompraCredito Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface CompraCreditoMapper {

    /**
     * Convierte CompraCreditoDTORequest a CompraCredito Entity.
     * El ID se ignora automáticamente para nuevas entidades.
     * Los campos fechaCreacion y cuotasPagadas se manejan por lógica de negocio.
     * 
     * NOTA: Las relaciones (@ManyToOne) deben ser establecidas DESPUÉS de llamar a este mapper
     * usando los métodos setters correspondientes de la entidad.
     * 
     * @param request DTO con datos del request
     * @return Entidad CompraCredito sin ID (para INSERT)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cuotasPagadas", constant = "0")
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "espacioTrabajo", ignore = true)
    @Mapping(target = "motivo", ignore = true)
    @Mapping(target = "comercio", ignore = true)
    @Mapping(target = "tarjeta", ignore = true)
    CompraCredito toEntity(CompraCreditoDTORequest request);

    /**
     * Convierte CompraCredito Entity a CompraCreditoDTOResponse.
     * 
     * Las relaciones @ManyToOne se mapean a sus IDs correspondientes.
     * 
     * @param compraCredito Entidad CompraCredito
     * @return DTO de respuesta con todos los datos de la compra a crédito
     */
    @Mapping(target = "espacioTrabajoId", source = "espacioTrabajo.id")
    @Mapping(target = "motivoId", source = "motivo.id")
    @Mapping(target = "comercioId", source = "comercio.id")
    @Mapping(target = "tarjetaId", source = "tarjeta.id")
    CompraCreditoDTOResponse toResponse(CompraCredito compraCredito);
}
