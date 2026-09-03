package com.campito.backend.common.test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.campito.backend.common.domain.TipoTransaccion;
import com.campito.backend.transacciones.domain.entity.*;
import com.campito.backend.transacciones.domain.dto.*;

public final class TransaccionesTestDataFactory {

    private TransaccionesTestDataFactory() {}

    public static CuentaBancaria crearCuentaBancaria(Long id) {
        return CuentaBancaria.builder()
            .id(id)
            .nombre("Cuenta Test")
            .entidadFinanciera("Banco Test")
            .saldoActual(new BigDecimal("1000.00"))
            .idEspacioTrabajo(TestIds.ESPACIO_TRABAJO_ID)
            .fechaCreacion(LocalDateTime.now())
            .fechaModificacion(LocalDateTime.now())
            .build();
    }

    public static Transaccion crearTransaccion(Long id) {
        return Transaccion.builder()
            .id(id)
            .tipo(TipoTransaccion.INGRESO)
            .monto(new BigDecimal("100.00"))
            .fecha(LocalDate.now())
            .descripcion("Transaccion Test")
            .nombreCompletoAuditoria("Test User")
            .fechaCreacion(LocalDateTime.now())
            .idEspacioTrabajo(TestIds.ESPACIO_TRABAJO_ID)
            .nombreEspacioTrabajo("Espacio Test")
            .motivo(crearMotivoTransaccion(TestIds.MOTIVO_ID))
            .build();
    }

    public static MotivoTransaccion crearMotivoTransaccion(Long id) {
        return MotivoTransaccion.builder()
            .id(id)
            .motivo("Motivo Test")
            .idEspacioTrabajo(TestIds.ESPACIO_TRABAJO_ID)
            .fechaCreacion(LocalDateTime.now())
            .fechaModificacion(LocalDateTime.now())
            .build();
    }

    public static ContactoTransferencia crearContacto(Long id) {
        return ContactoTransferencia.builder()
            .id(id)
            .nombre("Contacto Test")
            .idEspacioTrabajo(TestIds.ESPACIO_TRABAJO_ID)
            .fechaCreacion(LocalDateTime.now())
            .fechaModificacion(LocalDateTime.now())
            .build();
    }

    public static Tarjeta crearTarjeta(Long id) {
        return Tarjeta.builder()
            .id(id)
            .numeroTarjeta("1234")
            .entidadFinanciera("Banco Test")
            .redDePago("Visa")
            .diaCierre(15)
            .diaVencimientoPago(5)
            .idEspacioTrabajo(TestIds.ESPACIO_TRABAJO_ID)
            .fechaCreacion(LocalDateTime.now())
            .fechaModificacion(LocalDateTime.now())
            .build();
    }

    public static CompraCredito crearCompraCredito(Long id) {
        return CompraCredito.builder()
            .id(id)
            .fechaCompra(LocalDate.now().minusDays(10))
            .montoTotal(new BigDecimal("1000.00"))
            .cantidadCuotas(3)
            .cuotasPagadas(0)
            .descripcion("Compra Test")
            .nombreCompletoAuditoria("Test User")
            .fechaCreacion(LocalDateTime.now())
            .idEspacioTrabajo(TestIds.ESPACIO_TRABAJO_ID)
            .nombreEspacioTrabajo("Espacio Test")
            .motivo(crearMotivoTransaccion(TestIds.MOTIVO_ID))
            .tarjeta(crearTarjeta(TestIds.TARJETA_ID))
            .build();
    }

    public static CuotaCredito crearCuotaCredito(Long id, int numeroCuota, boolean pagada) {
        return CuotaCredito.builder()
            .id(id)
            .numeroCuota(numeroCuota)
            .fechaVencimiento(LocalDate.now().plusMonths(1))
            .montoCuota(new BigDecimal("333.34"))
            .pagada(pagada)
            .compraCredito(crearCompraCredito(TestIds.COMPRA_CREDITO_ID))
            .build();
    }

    public static Resumen crearResumen(Long id) {
        return Resumen.builder()
            .id(id)
            .anio(2026)
            .mes(9)
            .fechaVencimiento(LocalDate.now().plusMonths(1))
            .estado(EstadoResumen.CERRADO)
            .montoTotal(new BigDecimal("333.34"))
            .tarjeta(crearTarjeta(TestIds.TARJETA_ID))
            .build();
    }

    public static TransaccionDTORequest crearTransaccionRequest(UUID idEspacio) {
        return new TransaccionDTORequest(
            LocalDate.now(),
            new BigDecimal("100.00"),
            TipoTransaccion.INGRESO,
            "Test",
            "Test User",
            idEspacio,
            TestIds.MOTIVO_ID,
            null,
            null
        );
    }

    public static CuentaBancariaDTORequest crearCuentaBancariaRequest(UUID idEspacio) {
        return new CuentaBancariaDTORequest(
            "Cuenta Test",
            "Banco Test",
            idEspacio,
            new BigDecimal("1000.00")
        );
    }

    public static CompraCreditoDTORequest crearCompraCreditoRequest(UUID idEspacio) {
        return new CompraCreditoDTORequest(
            LocalDate.now().minusDays(10),
            new BigDecimal("1000.00"),
            3,
            "Compra Test",
            "Test User",
            idEspacio,
            TestIds.MOTIVO_ID,
            null,
            TestIds.TARJETA_ID
        );
    }

    public static TarjetaDTORequest crearTarjetaRequest(UUID idEspacio) {
        return new TarjetaDTORequest(
            "1234",
            "Banco Test",
            "Visa",
            15,
            5,
            idEspacio
        );
    }

    public static ContactoDTORequest crearContactoRequest(UUID idEspacio) {
        return new ContactoDTORequest("Contacto Test", idEspacio);
    }

    public static MotivoDTORequest crearMotivoRequest(UUID idEspacio) {
        return new MotivoDTORequest("Motivo Test", idEspacio);
    }

    public static TransaccionDTOResponse crearTransaccionResponse(Long id) {
        return new TransaccionDTOResponse(
            id,
            LocalDate.now(),
            new BigDecimal("100.00"),
            TipoTransaccion.INGRESO,
            "Test",
            "Test User",
            LocalDateTime.now(),
            TestIds.ESPACIO_TRABAJO_ID,
            "Espacio Test",
            TestIds.MOTIVO_ID,
            "Motivo Test",
            null,
            null,
            null
        );
    }

    public static CuentaBancariaDTOResponse crearCuentaBancariaResponse(Long id) {
        return new CuentaBancariaDTOResponse(id, "Cuenta Test", "Banco Test", new BigDecimal("1000.00"));
    }

    public static CompraCreditoDTOResponse crearCompraCreditoResponse(Long id) {
        return new CompraCreditoDTOResponse(
            id, LocalDate.now().minusDays(10), new BigDecimal("1000.00"), 3, 0,
            "Compra Test", "Test User", LocalDateTime.now(),
            TestIds.ESPACIO_TRABAJO_ID, "Espacio Test",
            TestIds.MOTIVO_ID, "Motivo Test",
            null, null, TestIds.TARJETA_ID, "1234", "Banco Test", "Visa"
        );
    }

    public static TarjetaDTOResponse crearTarjetaResponse(Long id) {
        return new TarjetaDTOResponse(id, "1234", "Banco Test", "Visa", 15, 5, TestIds.ESPACIO_TRABAJO_ID);
    }

    public static ContactoDTOResponse crearContactoResponse(Long id) {
        return new ContactoDTOResponse(id, "Contacto Test");
    }

    public static MotivoDTOResponse crearMotivoResponse(Long id) {
        return new MotivoDTOResponse(id, "Motivo Test");
    }

    public static ContactoTransferencia crearContactoTransferencia(Long id) {
        return ContactoTransferencia.builder()
            .id(id)
            .nombre("Contacto Test")
            .idEspacioTrabajo(TestIds.ESPACIO_TRABAJO_ID)
            .fechaCreacion(LocalDateTime.now())
            .fechaModificacion(LocalDateTime.now())
            .build();
    }

    public static ContactoTransferencia crearComercio(Long id) {
        ContactoTransferencia c = crearContactoTransferencia(id);
        c.setNombre("Comercio Test");
        return c;
    }
}
