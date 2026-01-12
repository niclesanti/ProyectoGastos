package com.campito.backend.service;

import java.util.List;

import com.campito.backend.dto.CompraCreditoDTORequest;
import com.campito.backend.dto.CompraCreditoDTOResponse;
import com.campito.backend.dto.CuotaCreditoDTOResponse;
import com.campito.backend.dto.PagarResumenTarjetaRequest;
import com.campito.backend.dto.ResumenDTOResponse;
import com.campito.backend.dto.TarjetaDTORequest;
import com.campito.backend.dto.TarjetaDTOResponse;

public interface CompraCreditoService {
    public CompraCreditoDTOResponse registrarCompraCredito(CompraCreditoDTORequest compraCreditoDTO);
    public void removerCompraCredito(Long id);
    public List<CompraCreditoDTOResponse> listarComprasCreditoDebeCuotas(Long idEspacioTrabajo);
    public List<CompraCreditoDTOResponse> BuscarComprasCredito(Long idEspacioTrabajo);
    public TarjetaDTOResponse registrarTarjeta(TarjetaDTORequest tarjetaDTO);
    public void removerTarjeta(Long id);
    public List<TarjetaDTOResponse> listarTarjetas(Long idEspacioTrabajo);
    public List<CuotaCreditoDTOResponse> listarCuotasPorTarjeta(Long idTarjeta);
    public void pagarResumenTarjeta(PagarResumenTarjetaRequest request);
    public List<ResumenDTOResponse> listarResumenesPorTarjeta(Long idTarjeta);
    public List<ResumenDTOResponse> listarResumenesPorEspacioTrabajo(Long idEspacioTrabajo);
}
