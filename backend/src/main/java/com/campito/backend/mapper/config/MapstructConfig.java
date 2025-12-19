package com.campito.backend.mapper.config;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Configuración global para todos los Mappers de MapStruct.
 * 
 * Esta interfaz centraliza la configuración común para evitar repetir
 * las mismas anotaciones en cada mapper individual.
 * 
 * Configuraciones aplicadas:
 * - componentModel = SPRING: Los mappers serán beans de Spring (@Component)
 * - unmappedTargetPolicy = IGNORE: No genera warnings para campos sin mapeo
 * - injectionStrategy = CONSTRUCTOR: Inyecta dependencias por constructor
 * - nullValuePropertyMappingStrategy = IGNORE: Ignora propiedades null del source
 * - nullValueCheckStrategy = ALWAYS: Siempre verifica nulls antes de mapear
 */
@MapperConfig(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface MapstructConfig {}

