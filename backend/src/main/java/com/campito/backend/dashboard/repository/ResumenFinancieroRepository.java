package com.campito.backend.dashboard.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campito.backend.dashboard.domain.entity.ResumenFinanciero;

/**
 * Repositorio del read-model {@link ResumenFinanciero} del módulo dashboard.
 *
 * Las mutaciones (upsert de fila e incremento/decremento de deuda) se realizan
 * mediante operaciones JPA estándar ({@code findById} + mutación + {@code save})
 * en el {@code DashboardEventListener}. Esto las mantiene testables con mocks y
 * portables entre PostgreSQL (prod) y H2 (tests).
 */
public interface ResumenFinancieroRepository extends JpaRepository<ResumenFinanciero, UUID> {

}
