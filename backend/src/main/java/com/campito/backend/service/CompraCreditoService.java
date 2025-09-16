package com.campito.backend.service;

import java.time.LocalDate;
import java.util.List;

import com.campito.backend.dto.CompraCreditoDTO;
import com.campito.backend.dto.CompraCreditoListadoDTO;
import com.campito.backend.dto.CuotaCreditoDTO;
import com.campito.backend.dto.TarjetaDTO;
import com.campito.backend.dto.TarjetaListadoDTO;
import com.campito.backend.dto.TransaccionDTO;

public interface CompraCreditoService {
    public CompraCreditoListadoDTO registrarCompraCredito(CompraCreditoDTO compraCreditoDTO);
    public void removerCompraCredito(Long id);
    public List<CompraCreditoListadoDTO> listarComprasCreditoDebeCuotas(Long idEspacioTrabajo);
    public List<CompraCreditoListadoDTO> BuscarComprasCredito(Long idEspacioTrabajo);
    public TarjetaListadoDTO registrarTarjeta(TarjetaDTO tarjetaDTO);
    public void removerTarjeta(Long id);
    public List<TarjetaListadoDTO> listarTarjetas(Long idEspacioTrabajo);
    public void listarCuotasPorTarjeta(Long idTarjeta, LocalDate fechaActual);
    public void pagarResumenTarjeta(List<CuotaCreditoDTO> cuotas, TransaccionDTO transaccion);
}
