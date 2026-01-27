package com.campito.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.campito.backend.dto.CuotaCreditoDTORequest;
import com.campito.backend.dto.CuotaCreditoDTOResponse;
import com.campito.backend.mapper.config.MapstructConfig;
import com.campito.backend.model.CuotaCredito;

/**
 * Mapper para conversión entre CuotaCredito Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface CuotaCreditoMapper {

    /**
     * Convierte CuotaCreditoDTORequest a CuotaCredito Entity.
     * El ID se ignora automáticamente para nuevas entidades.
     * El campo pagada se inicializa en false por defecto.
     * 
     * NOTA: Las relaciones (@ManyToOne) deben ser establecidas DESPUÉS de llamar a este mapper
     * usando los métodos setters correspondientes de la entidad.
     * 
     * @param request DTO con datos del request
     * @return Entidad CuotaCredito sin ID (para INSERT)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pagada", constant = "false")
    @Mapping(target = "compraCredito", ignore = true)
    @Mapping(target = "resumenAsociado", ignore = true)
    CuotaCredito toEntity(CuotaCreditoDTORequest request);

    /**
     * Convierte CuotaCredito Entity a CuotaCreditoDTOResponse.
     * 
     * Las relaciones @ManyToOne se mapean a sus IDs correspondientes.
     * 
     * @param cuotaCredito Entidad CuotaCredito
     * @return DTO de respuesta con todos los datos de la cuota de crédito
     */
    @Mapping(target = "idCompraCredito", source = "compraCredito.id")
    @Mapping(target = "idResumenAsociado", source = "resumenAsociado.id")
    CuotaCreditoDTOResponse toResponse(CuotaCredito cuotaCredito);
}
