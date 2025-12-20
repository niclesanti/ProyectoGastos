package com.campito.backend.service;

import java.util.List;

import com.campito.backend.dto.EspacioTrabajoDTORequest;
import com.campito.backend.dto.EspacioTrabajoDTOResponse;

public interface EspacioTrabajoService {
    public void registrarEspacioTrabajo(EspacioTrabajoDTORequest espacioTrabajoDTO);
    public void compartirEspacioTrabajo(String email, Long idEspacioTrabajo, Long idUsuarioAdmin);
    public List<EspacioTrabajoDTOResponse> listarEspaciosTrabajoPorUsuario(Long idUsuario);
}
