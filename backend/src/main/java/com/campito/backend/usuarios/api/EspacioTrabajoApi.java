package com.campito.backend.usuarios.api;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Facade del módulo de usuarios que expone operaciones sobre EspacioTrabajo
 * a otros módulos sin acoplarlos a la entidad ni al repositorio.
 * 
 * Las mutaciones de saldo se realizan mediante deltas signados
 * (+ ingreso, - gasto) calculados por el módulo productor.
 */
public interface EspacioTrabajoApi {

    boolean existe(UUID idEspacio);

    BigDecimal obtenerSaldo(UUID idEspacio);

    String obtenerNombre(UUID idEspacio);

    UUID obtenerIdUsuarioAdmin(UUID idEspacio);

    void aplicarMovimientoSaldo(UUID idEspacio, BigDecimal delta);
}
