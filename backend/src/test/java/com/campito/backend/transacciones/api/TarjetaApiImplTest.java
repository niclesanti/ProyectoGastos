package com.campito.backend.transacciones.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campito.backend.common.test.TestIds;
import com.campito.backend.common.test.TransaccionesTestDataFactory;
import com.campito.backend.transacciones.domain.entity.Tarjeta;
import com.campito.backend.transacciones.repository.TarjetaRepository;

@ExtendWith(MockitoExtension.class)
class TarjetaApiImplTest {

    @Mock
    private TarjetaRepository tarjetaRepository;

    @InjectMocks
    private TarjetaApiImpl tarjetaApi;

    private UUID idEspacio;

    @BeforeEach
    void setUp() {
        idEspacio = TestIds.ESPACIO_TRABAJO_ID;
    }

    @Test
    void listarParaCierre_conTarjetas_retornaResumenes() {
        List<Tarjeta> tarjetas = List.of(
            TransaccionesTestDataFactory.crearTarjeta(1L),
            TransaccionesTestDataFactory.crearTarjeta(2L)
        );
        when(tarjetaRepository.findByIdEspacioTrabajo(idEspacio)).thenReturn(tarjetas);

        List<TarjetaApi.TarjetaResumen> result = tarjetaApi.listarParaCierre(idEspacio);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(15, result.get(0).diaCierre());
        assertEquals(5, result.get(0).diaVencimientoPago());
    }

    @Test
    void listarParaCierre_sinTarjetas_retornaListaVacia() {
        when(tarjetaRepository.findByIdEspacioTrabajo(idEspacio)).thenReturn(List.of());

        List<TarjetaApi.TarjetaResumen> result = tarjetaApi.listarParaCierre(idEspacio);

        assertTrue(result.isEmpty());
    }
}
