package com.campito.backend.common.test;

import java.util.UUID;

public final class TestIds {

    public static final UUID USUARIO_ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID USUARIO_PARTICIPANTE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID USUARIO_NO_MIEMBRO_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID ESPACIO_TRABAJO_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID ESPACIO_TRABAJO_2_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    public static final UUID NOT_FOUND_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    public static final Long TRANSACCION_ID = 1L;
    public static final Long COMPRA_CREDITO_ID = 1L;
    public static final Long CUENTA_BANCARIA_ID = 1L;
    public static final Long TARJETA_ID = 1L;
    public static final Long MOTIVO_ID = 1L;
    public static final Long CONTACTO_ID = 1L;
    public static final Long RESUMEN_ID = 1L;
    public static final Long NOTIFICACION_ID = 1L;
    public static final Long DESCUENTO_ID = 1L;
    public static final Long SOLICITUD_ID = 1L;

    private TestIds() {
        throw new UnsupportedOperationException("Utility class");
    }
}
