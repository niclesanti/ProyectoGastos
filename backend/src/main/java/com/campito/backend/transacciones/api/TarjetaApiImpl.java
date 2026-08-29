package com.campito.backend.transacciones.api;

import com.campito.backend.transacciones.repository.TarjetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TarjetaApiImpl implements TarjetaApi {

    private final TarjetaRepository tarjetaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TarjetaResumen> listarParaCierre(UUID idEspacio) {
        return tarjetaRepository.findByIdEspacioTrabajo(idEspacio).stream()
            .map(t -> new TarjetaResumen(t.getId(), t.getDiaCierre(), t.getDiaVencimientoPago()))
            .toList();
    }
}
