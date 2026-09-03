package com.campito.backend.transacciones.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CompraCreditoBusinessTest {

    @Test
    void pagarCuota_incrementaCuotasPagadas() {
        CompraCredito compra = CompraCredito.builder()
            .cantidadCuotas(3)
            .cuotasPagadas(0)
            .build();

        compra.pagarCuota();

        assertEquals(1, compra.getCuotasPagadas());
    }

    @Test
    void pagarCuota_todasPagadas_lanzaIllegalStateException() {
        CompraCredito compra = CompraCredito.builder()
            .cantidadCuotas(2)
            .cuotasPagadas(2)
            .build();

        assertThrows(IllegalStateException.class, compra::pagarCuota);
    }

    @Test
    void pagarCuota_parcialmentePagada_incrementa() {
        CompraCredito compra = CompraCredito.builder()
            .cantidadCuotas(5)
            .cuotasPagadas(3)
            .build();

        compra.pagarCuota();

        assertEquals(4, compra.getCuotasPagadas());
    }
}
