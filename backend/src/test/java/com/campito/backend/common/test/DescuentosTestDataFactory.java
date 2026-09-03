package com.campito.backend.common.test;

import java.util.UUID;

import com.campito.backend.descuentos.domain.entity.Descuento;
import com.campito.backend.descuentos.domain.dto.*;

public final class DescuentosTestDataFactory {

    private DescuentosTestDataFactory() {}

    public static Descuento crearDescuento(Long id) {
        return Descuento.builder()
            .id(id)
            .dia("Lunes")
            .localidad("CABA")
            .banco("Galicia")
            .modo(true)
            .porcentaje("15")
            .comercio("Carrefour")
            .modoPago("Débito")
            .topeReintegro("500")
            .esSemanal(true)
            .comentario("Test")
            .idEspacioTrabajo(TestIds.ESPACIO_TRABAJO_ID)
            .build();
    }

    public static DescuentoDTOResponse crearDescuentoResponse(Long id) {
        return new DescuentoDTOResponse(
            id, "Lunes", "CABA", "Galicia", true, "15",
            "Carrefour", "Débito", "500", true, "Test",
            TestIds.ESPACIO_TRABAJO_ID
        );
    }

    public static DescuentoDTORequest crearDescuentoRequest(UUID idEspacio) {
        return new DescuentoDTORequest(
            "Lunes", "CABA", "Galicia", true, "15",
            "Carrefour", "Débito", "500", true, "Test", idEspacio
        );
    }
}
