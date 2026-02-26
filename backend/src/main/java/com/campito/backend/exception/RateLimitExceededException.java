package com.campito.backend.exception;

/**
 * Excepción lanzada cuando un usuario excede el límite de requests al agente IA.
 * Se mapea a HTTP 429 (Too Many Requests).
 */
public class RateLimitExceededException extends RuntimeException {
    
    public RateLimitExceededException(String message) {
        super(message);
    }
    
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
