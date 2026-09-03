package com.campito.backend.transacciones.api;

import java.util.List;
import java.util.UUID;

/**
 * Facade de lectura del módulo de transacciones para las tarjetas
 * de un espacio de trabajo, expuesto a otros módulos (dashboard, schedulers).
 */
public interface TarjetaApi {

    /**
     * Resumen mínimo de una tarjeta para los cálculos de cierre/resumen.
     */
    record TarjetaResumen(Long id, Integer diaCierre, Integer diaVencimientoPago) {
    }

    List<TarjetaResumen> listarParaCierre(UUID idEspacio);
}
