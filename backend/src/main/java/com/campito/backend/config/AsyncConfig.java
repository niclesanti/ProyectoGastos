package com.campito.backend.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuración del thread pool para la ejecución paralela de queries 
 * independientes usando CompletableFuture.
 * 
 * El pool está dimensionado para no exceder el maximum-pool-size de HikariCP (5),
 * evitando thread starvation donde threads esperan conexiones de BD indisponibles.
 */
@Configuration
@Slf4j
public class AsyncConfig {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 5;
    private static final int QUEUE_CAPACITY = 50;
    private static final String THREAD_NAME_PREFIX = "async-exec-";

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setRejectedExecutionHandler((runnable, executor1) -> {
            log.warn("Async task rejected - thread pool saturated, executing on caller thread");
            if (!executor1.isShutdown()) {
                runnable.run();
            }
        });
        executor.initialize();
        return executor;
    }
}
