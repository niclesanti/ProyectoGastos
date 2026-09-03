package com.campito.backend.transacciones.scheduler;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.campito.backend.common.test.TransaccionesTestDataFactory;
import com.campito.backend.transacciones.domain.entity.Tarjeta;
import com.campito.backend.transacciones.repository.CuotaCreditoRepository;
import com.campito.backend.transacciones.repository.ResumenRepository;
import com.campito.backend.transacciones.repository.TarjetaRepository;
import com.campito.backend.usuarios.api.EspacioTrabajoApi;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ResumenSchedulerTest {

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private CuotaCreditoRepository cuotaCreditoRepository;

    @Mock
    private ResumenRepository resumenRepository;

    @Mock
    private EspacioTrabajoApi espacioTrabajoApi;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private MeterRegistry meterRegistry;

    @InjectMocks
    private ResumenScheduler resumenScheduler;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        ReflectionTestUtils.setField(resumenScheduler, "meterRegistry", meterRegistry);
    }

    @Test
    void cerrarResumenesDiarios_sinTarjetas_noHaceNada() {
        when(tarjetaRepository.findAll()).thenReturn(List.of());

        resumenScheduler.cerrarResumenesDiarios();

        verify(cuotaCreditoRepository, never()).findByTarjetaSinResumenEnRango(any(), any(), any());
    }

    @Test
    void cerrarResumenesDiarios_tarjetasSinCierreHoy_noProcesa() {
        Tarjeta tarjeta = TransaccionesTestDataFactory.crearTarjeta(1L);
        tarjeta.setDiaCierre(99); // Día que no coincide con ayer
        when(tarjetaRepository.findAll()).thenReturn(List.of(tarjeta));

        resumenScheduler.cerrarResumenesDiarios();

        verify(cuotaCreditoRepository, never()).findByTarjetaSinResumenEnRango(any(), any(), any());
    }
}
