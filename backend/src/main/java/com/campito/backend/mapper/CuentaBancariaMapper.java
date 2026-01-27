package com.campito.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.campito.backend.dto.CuentaBancariaDTORequest;
import com.campito.backend.dto.CuentaBancariaDTOResponse;
import com.campito.backend.mapper.config.MapstructConfig;
import com.campito.backend.model.CuentaBancaria;

/**
 * Mapper para conversión entre CuentaBancaria Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface CuentaBancariaMapper {

    /**
     * Convierte CuentaBancariaDTORequest a CuentaBancaria Entity.
     * El ID se ignora automáticamente para nuevas entidades.
     * El saldo inicial se establece en 0.0 por defecto en la entidad.
     * 
     * NOTA: La relación espacioTrabajo debe ser establecida DESPUÉS de llamar a este mapper
     * usando el método setter correspondiente de la entidad.
     * 
     * @param request DTO con datos del request
     * @return Entidad CuentaBancaria sin ID (para INSERT)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "espacioTrabajo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    CuentaBancaria toEntity(CuentaBancariaDTORequest request);

    /**
     * Convierte CuentaBancaria Entity a CuentaBancariaDTOResponse.
     * 
     * @param cuentaBancaria Entidad CuentaBancaria
     * @return DTO de respuesta con todos los datos de la cuenta bancaria
     */
    CuentaBancariaDTOResponse toResponse(CuentaBancaria cuentaBancaria);
}
