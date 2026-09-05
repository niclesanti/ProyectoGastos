package com.campito.backend.transacciones.service;

import java.util.List;
import java.util.UUID;

import com.campito.backend.transacciones.domain.dto.ContactoDTORequest;
import com.campito.backend.transacciones.domain.dto.ContactoDTOResponse;
import com.campito.backend.transacciones.domain.dto.MotivoDTORequest;
import com.campito.backend.transacciones.domain.dto.MotivoDTOResponse;
import com.campito.backend.transacciones.domain.dto.PaginatedResponse;
import com.campito.backend.transacciones.domain.dto.TransaccionBusquedaDTO;
import com.campito.backend.transacciones.domain.dto.TransaccionDTORequest;
import com.campito.backend.transacciones.domain.dto.TransaccionDTOResponse;

public interface TransaccionService {
    public TransaccionDTOResponse registrarTransaccion(TransaccionDTORequest transaccionDTO);
    public void removerTransaccion(Long id);
    public PaginatedResponse<TransaccionDTOResponse> buscarTransaccion(TransaccionBusquedaDTO datosBusqueda);
    public ContactoDTOResponse registrarContactoTransferencia(ContactoDTORequest contactoDTO);
    public MotivoDTOResponse nuevoMotivoTransaccion(MotivoDTORequest motivoDTO);
    public List<ContactoDTOResponse> listarContactos(UUID idEspacioTrabajo);
    public List<MotivoDTOResponse> listarMotivos(UUID idEspacioTrabajo);
    public List<TransaccionDTOResponse> buscarTransaccionesRecientes(UUID idEspacioTrabajo);
}
