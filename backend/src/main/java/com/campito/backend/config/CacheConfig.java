package com.campito.backend.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de caché.
 *
 * Habilita el procesamiento de anotaciones {@code @Cacheable} para cachear
 * las agregaciones temporales del dashboard (resumen mensual y distribuciones)
 * a través del servicio {@code DashboardReportesService}.
 *
 * La implementación concreta (Caffeine) y el tamaño/TTL se configuran en los
 * archivos de propiedades de cada perfil (ver {@code spring.cache.*}).
 */
@Configuration
@EnableCaching
public class CacheConfig {

}
