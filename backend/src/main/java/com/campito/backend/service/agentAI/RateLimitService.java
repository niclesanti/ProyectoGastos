package com.campito.backend.service.agentAI;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de rate limiting para el agente IA.
 * Implementa token bucket algorithm usando Bucket4j.
 * Limita requests por usuario para prevenir abuso y controlar costos de API.
 */
@Service
@Slf4j
public class RateLimitService {
    
    private final Map<UUID, Bucket> buckets = new ConcurrentHashMap<>();
    
    @Value("${agente.rate-limit.requests-per-minute:60}")
    private long requestsPerMinute;
    
    @Value("${agente.rate-limit.burst-capacity:10}")
    private long burstCapacity;
    
    /**
     * Valida si el usuario puede hacer otra request al agente.
     * Usa token bucket: permite ráfagas cortas pero limita el promedio.
     * 
     * @param userId ID del usuario
     * @return true si tiene tokens disponibles, false si excedió el límite
     */
    public boolean allowRequest(UUID userId) {
        Bucket bucket = buckets.computeIfAbsent(userId, this::createBucket);
        
        boolean allowed = bucket.tryConsume(1);
        
        if (!allowed) {
            log.warn("Rate limit excedido para usuario: {} (límite: {} req/min)", 
                userId, requestsPerMinute);
        }
        
        return allowed;
    }
    
    /**
     * Crea un nuevo bucket con la configuración de rate limiting.
     * 
     * Ejemplo con valores por defecto:
     * - Burst capacity: 10 tokens (permite 10 requests rápidas)
     * - Refill: 60 tokens/minuto (1 token/segundo en promedio)
     */
    private Bucket createBucket(UUID userId) {
        log.debug("Creando bucket de rate limit para usuario: {}", userId);
        
        Bandwidth limit = Bandwidth.builder()
            .capacity(burstCapacity)
            .refillIntervally(requestsPerMinute, Duration.ofMinutes(1))
            .build();
        
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
    
    /**
     * Obtiene tokens restantes para un usuario.
     * Útil para mostrar en el frontend cuántas requests le quedan.
     * 
     * @param userId ID del usuario
     * @return Cantidad de tokens disponibles
     */
    public long getAvailableTokens(UUID userId) {
        Bucket bucket = buckets.get(userId);
        if (bucket == null) {
            return burstCapacity; // Usuario nuevo, tiene todos los tokens
        }
        return bucket.getAvailableTokens();
    }
    
    /**
     * Resetea el bucket de un usuario (útil para testing o admin).
     * 
     * @param userId ID del usuario
     */
    public void resetUserLimit(UUID userId) {
        log.info("Reseteando rate limit para usuario: {}", userId);
        buckets.remove(userId);
    }
}
