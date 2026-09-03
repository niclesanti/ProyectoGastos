package com.campito.backend.descuentos.service;

import java.util.List;
import java.util.UUID;

import com.campito.backend.descuentos.domain.dto.DescuentoDTORequest;
import com.campito.backend.descuentos.domain.dto.DescuentoDTOResponse;

public interface DescuentoService {
    public DescuentoDTOResponse crearDescuento(DescuentoDTORequest dto);
    public List<DescuentoDTOResponse> listarDescuentos(UUID idEspacioTrabajo);
    public void eliminarDescuento(Long id);
}
