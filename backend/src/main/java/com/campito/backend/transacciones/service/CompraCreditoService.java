package com.campito.backend.transacciones.service;

import java.util.List;
import java.util.UUID;

import com.campito.backend.transacciones.domain.dto.CompraCreditoDTORequest;
import com.campito.backend.transacciones.domain.dto.CompraCreditoBusquedaDTO;
import com.campito.backend.transacciones.domain.dto.CompraCreditoDTOResponse;
import com.campito.backend.transacciones.domain.dto.CuotaCreditoDTOResponse;
import com.campito.backend.transacciones.domain.dto.PaginatedResponse;
import com.campito.backend.transacciones.domain.dto.PagarResumenTarjetaRequest;
import com.campito.backend.transacciones.domain.dto.ResumenDTOResponse;
import com.campito.backend.transacciones.domain.dto.TarjetaDTORequest;
import com.campito.backend.transacciones.domain.dto.TarjetaDTOResponse;
import com.campito.backend.transacciones.domain.dto.TarjetaDTOUpdate;

public interface CompraCreditoService {
    public CompraCreditoDTOResponse registrarCompraCredito(CompraCreditoDTORequest compraCreditoDTO);
    public void removerCompraCredito(Long id);
    public PaginatedResponse<CompraCreditoDTOResponse> listarComprasCreditoDebeCuotas(UUID idEspacioTrabajo, Integer page, Integer size);
    public List<CompraCreditoDTOResponse> BuscarComprasCredito(UUID idEspacioTrabajo);
    public PaginatedResponse<CompraCreditoDTOResponse> buscarComprasCredito(CompraCreditoBusquedaDTO datosBusqueda);
    public TarjetaDTOResponse registrarTarjeta(TarjetaDTORequest tarjetaDTO);
    public void removerTarjeta(Long id);
    public List<TarjetaDTOResponse> listarTarjetas(UUID idEspacioTrabajo);
    public List<CuotaCreditoDTOResponse> listarCuotasPorTarjeta(Long idTarjeta);
    public void pagarResumenTarjeta(PagarResumenTarjetaRequest request);
    public List<ResumenDTOResponse> listarResumenesPorTarjeta(Long idTarjeta);
    public List<ResumenDTOResponse> listarResumenesPorEspacioTrabajo(UUID idEspacioTrabajo);
    public TarjetaDTOResponse modificarTarjeta(Long id, TarjetaDTOUpdate tarjetaUpdate);
}
