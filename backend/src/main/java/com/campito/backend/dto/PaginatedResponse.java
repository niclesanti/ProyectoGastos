package com.campito.backend.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO genérico para respuestas paginadas.
 * Proporciona metadatos de paginación junto con el contenido.
 * 
 * @param <T> Tipo de los elementos contenidos en la respuesta paginada
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> {
    
    /**
     * Lista de elementos de la página actual
     */
    private List<T> content;
    
    /**
     * Número total de elementos en todas las páginas
     */
    private long totalElements;
    
    /**
     * Número total de páginas
     */
    private int totalPages;
    
    /**
     * Número de página actual (basado en 0)
     */
    private int currentPage;
    
    /**
     * Tamaño de página solicitado
     */
    private int pageSize;
    
    /**
     * Indica si es la primera página
     */
    private boolean first;
    
    /**
     * Indica si es la última página
     */
    private boolean last;
    
    /**
     * Indica si hay una página anterior
     */
    private boolean hasPrevious;
    
    /**
     * Indica si hay una página siguiente
     */
    private boolean hasNext;
    
    /**
     * Constructor que crea una respuesta paginada a partir de un objeto Page de Spring Data.
     * 
     * @param page Página de Spring Data
     */
    public PaginatedResponse(Page<T> page) {
        this.content = page.getContent();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.hasPrevious = page.hasPrevious();
        this.hasNext = page.hasNext();
    }
}
