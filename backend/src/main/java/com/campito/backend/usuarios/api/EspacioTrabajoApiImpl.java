package com.campito.backend.usuarios.api;

import com.campito.backend.usuarios.domain.entity.EspacioTrabajo;
import com.campito.backend.usuarios.repository.EspacioTrabajoRepository;
import com.campito.backend.common.event.SaldoActualizadoEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EspacioTrabajoApiImpl implements EspacioTrabajoApi {

    private final EspacioTrabajoRepository espacioTrabajoRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public boolean existe(UUID idEspacio) {
        return espacioTrabajoRepository.existsById(idEspacio);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerSaldo(UUID idEspacio) {
        return buscarEspacioTrabajoPorId(idEspacio).getSaldo();
    }

    @Override
    @Transactional(readOnly = true)
    public String obtenerNombre(UUID idEspacio) {
        return buscarEspacioTrabajoPorId(idEspacio).getNombre();
    }

    @Override
    @Transactional(readOnly = true)
    public UUID obtenerIdUsuarioAdmin(UUID idEspacio) {
        return buscarEspacioTrabajoPorId(idEspacio).getUsuarioAdmin().getId();
    }

    @Override
    @Transactional
    public void aplicarMovimientoSaldo(UUID idEspacio, BigDecimal delta) {
        EspacioTrabajo espacio = buscarEspacioTrabajoPorId(idEspacio);
        espacio.setSaldo(espacio.getSaldo().add(delta));
        espacioTrabajoRepository.save(espacio);

        // Sincronizar el read-model del dashboard con el saldo COMPLETO (idempotente)
        eventPublisher.publishEvent(new SaldoActualizadoEvent(idEspacio, espacio.getSaldo()));
    }

    private EspacioTrabajo buscarEspacioTrabajoPorId(UUID idEspacioTrabajo) {
        return espacioTrabajoRepository.findById(idEspacioTrabajo).orElseThrow(() -> {
            String msg = "Espacio de trabajo con ID " + idEspacioTrabajo + " no encontrado";
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });
    }
}
