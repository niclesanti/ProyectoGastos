package com.campito.backend.transacciones.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ResumenBusinessTest {

    @Test
    void asociarTransaccion_cambiaEstadoAPagado() {
        Resumen resumen = Resumen.builder()
            .estado(EstadoResumen.CERRADO)
            .build();

        Transaccion transaccion = Transaccion.builder().build();

        resumen.asociarTransaccion(transaccion);

        assertEquals(EstadoResumen.PAGADO, resumen.getEstado());
        assertEquals(transaccion, resumen.getTransaccionAsociada());
    }
}
