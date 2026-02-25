package com.campito.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class BackendApplication {

	public static void main(String[] args) {
		// Habilita la propagación automática del SecurityContext (ThreadLocal) de Spring Security
		// hacia los threads del scheduler de Reactor (onPool-worker-N).
		// Esto es necesario porque las tools de Spring AI se ejecutan en threads del pool de Reactor
		// y no en el thread HTTP donde JwtAuthenticationFilter seteó el SecurityContext.
		// Spring Security 6.3+ registra SecurityContextHolderThreadLocalAccessor automáticamente
		// cuando context-propagation (Micrometer) está en el classpath.
		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(BackendApplication.class, args);
	}

}
