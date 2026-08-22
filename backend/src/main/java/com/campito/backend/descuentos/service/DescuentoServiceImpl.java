package com.campito.backend.descuentos.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campito.backend.descuentos.domain.dto.DescuentoDTORequest;
import com.campito.backend.descuentos.domain.dto.DescuentoDTOResponse;
import com.campito.backend.descuentos.domain.entity.Descuento;
import com.campito.backend.descuentos.mapper.DescuentoMapper;
import com.campito.backend.descuentos.repository.DescuentoRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio para gestión de descuentos.
 * 
 * Proporciona métodos para crear descuentos, listarlos y eliminarlos.
 */
@Service
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
@Slf4j
public class DescuentoServiceImpl implements DescuentoService{

    private final DescuentoRepository descuentoRepository;
    private final DescuentoMapper descuentoMapper;

    /**
     * Crea un nuevo descuento para el espacio de trabajo indicado.
     *
     * @param dto Datos del descuento a crear.
     * @throws EntityNotFoundException si el espacio de trabajo no existe.
     */
    @Override
    @Transactional
    public DescuentoDTOResponse crearDescuento(DescuentoDTORequest dto) {
        log.info("Creando descuento '{}' para banco '{}' en espacio de trabajo ID: {}", dto.comercio(), dto.banco(), dto.idEspacioTrabajo());

        Descuento descuento = descuentoMapper.toEntity(dto);
        descuento.setIdEspacioTrabajo(dto.idEspacioTrabajo());
        Descuento descuentoGuardado = descuentoRepository.save(descuento);
        log.info("Descuento '{}' creado exitosamente.", dto.comercio());
        return descuentoMapper.toResponse(descuentoGuardado);
    }

    /**
     * Lista todos los descuentos de un espacio de trabajo.
     *
     * @param idEspacioTrabajo UUID del espacio de trabajo.
     * @return Lista de descuentos del espacio de trabajo.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DescuentoDTOResponse> listarDescuentos(UUID idEspacioTrabajo) {
        log.info("Listando descuentos para el espacio de trabajo ID: {}", idEspacioTrabajo);

        List<DescuentoDTOResponse> descuentos = descuentoRepository
            .findByIdEspacioTrabajoOrderByDiaAsc(idEspacioTrabajo)
            .stream()
            .map(descuentoMapper::toResponse)
            .toList();

        log.info("Encontrados {} descuentos para el espacio de trabajo ID: {}", descuentos.size(), idEspacioTrabajo);
        return descuentos;
    }

    /**
     * Elimina un descuento por su ID.
     *
     * @param id ID del descuento a eliminar.
     * @throws EntityNotFoundException si el descuento no existe.
     */
    @Override
    @Transactional
    public void eliminarDescuento(Long id) {
        log.info("Eliminando descuento ID: {}", id);

        if (!descuentoRepository.existsById(id)) {
            String mensaje = "Descuento con ID " + id + " no encontrado";
            log.warn(mensaje);
            throw new EntityNotFoundException(mensaje);
        }

        descuentoRepository.deleteById(id);
        log.info("Descuento ID: {} eliminado exitosamente.", id);
    }

    /*
    ===========================================================================
        MÉTODOS AUXILIARES PRIVADOS
    ===========================================================================
    */

}
