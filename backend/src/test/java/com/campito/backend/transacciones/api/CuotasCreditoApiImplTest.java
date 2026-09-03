package com.campito.backend.transacciones.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campito.backend.common.test.TestIds;
import com.campito.backend.transacciones.repository.CuotaCreditoRepository;

@ExtendWith(MockitoExtension.class)
class CuotasCreditoApiImplTest {

    @Mock
    private CuotaCreditoRepository cuotaCreditoRepository;

    @Mock
    private TarjetaApi tarjetaApi;

    @InjectMocks
    private CuotasCreditoApiImpl cuotasCreditoApi;

    private UUID idEspacio;

    @BeforeEach
    void setUp() {
        idEspacio = TestIds.ESPACIO_TRABAJO_ID;
    }

    @Test
    void calcularDeudaTotalPendiente_retornaMonto() {
        when(cuotaCreditoRepository.calcularDeudaTotalPendiente(idEspacio))
            .thenReturn(new BigDecimal("1500.00"));

        BigDecimal result = cuotasCreditoApi.calcularDeudaTotalPendiente(idEspacio);

        assertEquals(new BigDecimal("1500.00"), result);
    }

    @Test
    void resumenMensual_sinTarjetas_retornaCero() {
        when(tarjetaApi.listarParaCierre(idEspacio)).thenReturn(List.of());

        BigDecimal result = cuotasCreditoApi.resumenMensual(idEspacio, LocalDate.now());

        assertEquals(BigDecimal.ZERO, result);
    }
}
