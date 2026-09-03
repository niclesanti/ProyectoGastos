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
import com.campito.backend.transacciones.repository.TarjetaRepository;
import com.campito.backend.usuarios.api.EspacioTrabajoApi;

@ExtendWith(MockitoExtension.class)
class TarjetaCierreSchedulerTest {

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private EspacioTrabajoApi espacioTrabajoApi;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TarjetaCierreScheduler tarjetaCierreScheduler;

    @BeforeEach
    void setUp() {
        // No stubbings needed - each test configures its own stubs
    }

    @Test
    void recordarProximosCierres_sinTarjetas_noPublicaEventos() {
        when(tarjetaRepository.findByDiaCierre(anyInt())).thenReturn(List.of());

        tarjetaCierreScheduler.recordarProximosCierres();

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void recordarProximosCierres_conTarjetas_publicaEventos() {
        Tarjeta tarjeta = TransaccionesTestDataFactory.crearTarjeta(1L);
        when(tarjetaRepository.findByDiaCierre(anyInt())).thenReturn(List.of(tarjeta));

        tarjetaCierreScheduler.recordarProximosCierres();

        verify(eventPublisher, times(1)).publishEvent(any());
    }
}
