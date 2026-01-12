package com.campito.backend.service;

import java.util.List;

import com.campito.backend.dto.ContactoDTORequest;
import com.campito.backend.dto.ContactoDTOResponse;
import com.campito.backend.dto.MotivoDTORequest;
import com.campito.backend.dto.MotivoDTOResponse;
import com.campito.backend.dto.TransaccionBusquedaDTO;
import com.campito.backend.dto.TransaccionDTORequest;
import com.campito.backend.dto.TransaccionDTOResponse;

public interface TransaccionService {
    public TransaccionDTOResponse registrarTransaccion(TransaccionDTORequest transaccionDTO);
    public void removerTransaccion(Long id);
    public List<TransaccionDTOResponse> buscarTransaccion(TransaccionBusquedaDTO datosBusqueda);
    public ContactoDTOResponse registrarContactoTransferencia(ContactoDTORequest contactoDTO);
    public MotivoDTOResponse nuevoMotivoTransaccion(MotivoDTORequest motivoDTO);
    public List<ContactoDTOResponse> listarContactos(Long idEspacioTrabajo);
    public List<MotivoDTOResponse> listarMotivos(Long idEspacioTrabajo);
    public List<TransaccionDTOResponse> buscarTransaccionesRecientes(Long idEspacioTrabajo);
}
