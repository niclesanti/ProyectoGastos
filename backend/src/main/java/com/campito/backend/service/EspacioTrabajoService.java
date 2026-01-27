package com.campito.backend.service;

import java.util.List;
import java.util.UUID;

import com.campito.backend.dto.EspacioTrabajoDTORequest;
import com.campito.backend.dto.EspacioTrabajoDTOResponse;
import com.campito.backend.dto.UsuarioDTOResponse;

public interface EspacioTrabajoService {
    public void registrarEspacioTrabajo(EspacioTrabajoDTORequest espacioTrabajoDTO);
    public void compartirEspacioTrabajo(String email, UUID idEspacioTrabajo);
    public List<EspacioTrabajoDTOResponse> listarEspaciosTrabajoPorUsuario(UUID idUsuario);
    public List<UsuarioDTOResponse> obtenerMiembrosEspacioTrabajo(UUID idEspacioTrabajo);
}
