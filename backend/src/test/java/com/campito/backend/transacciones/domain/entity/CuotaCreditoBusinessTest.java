package com.campito.backend.transacciones.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CuotaCreditoBusinessTest {

    @Test
    void pagarCuota_marcaComoPagada() {
        CuotaCredito cuota = CuotaCredito.builder()
            .pagada(false)
            .build();

        cuota.pagarCuota();

        assertTrue(cuota.isPagada());
    }
}
