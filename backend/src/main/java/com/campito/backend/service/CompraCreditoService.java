package com.campito.backend.service;

import java.util.List;
import java.util.UUID;

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
    public List<CompraCreditoDTOResponse> listarComprasCreditoDebeCuotas(UUID idEspacioTrabajo);
    public List<CompraCreditoDTOResponse> BuscarComprasCredito(UUID idEspacioTrabajo);
    public TarjetaDTOResponse registrarTarjeta(TarjetaDTORequest tarjetaDTO);
    public void removerTarjeta(Long id);
    public List<TarjetaDTOResponse> listarTarjetas(UUID idEspacioTrabajo);
    public List<CuotaCreditoDTOResponse> listarCuotasPorTarjeta(Long idTarjeta);
    public void pagarResumenTarjeta(PagarResumenTarjetaRequest request);
    public List<ResumenDTOResponse> listarResumenesPorTarjeta(Long idTarjeta);
    public List<ResumenDTOResponse> listarResumenesPorEspacioTrabajo(UUID idEspacioTrabajo);
    public TarjetaDTOResponse modificarTarjeta(Long id, Integer diaCierre, Integer diaVencimientoPago);
}
