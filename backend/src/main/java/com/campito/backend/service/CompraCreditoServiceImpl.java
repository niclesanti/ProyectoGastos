package com.campito.backend.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campito.backend.dao.CompraCreditoRepository;
import com.campito.backend.dao.ContactoTransferenciaRepository;
import com.campito.backend.dao.CuotaCreditoRepository;
import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dao.MotivoTransaccionRepository;
import com.campito.backend.dao.TarjetaRepository;
import com.campito.backend.dto.CompraCreditoDTO;
import com.campito.backend.dto.CompraCreditoListadoDTO;
import com.campito.backend.dto.CuotaCreditoDTO;
import com.campito.backend.dto.TarjetaDTO;
import com.campito.backend.dto.TarjetaListadoDTO;
import com.campito.backend.dto.TransaccionDTO;
import com.campito.backend.model.CompraCredito;
import com.campito.backend.model.ContactoTransferencia;
import com.campito.backend.model.CuotaCredito;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.MotivoTransaccion;
import com.campito.backend.model.Tarjeta;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CompraCreditoServiceImpl implements CompraCreditoService {

    private static final Logger logger = LoggerFactory.getLogger(CompraCreditoServiceImpl.class);

    private final CompraCreditoRepository compraCreditoRepository;
    private final EspacioTrabajoRepository espacioRepository;
    private final MotivoTransaccionRepository motivoRepository;
    private final ContactoTransferenciaRepository contactoRepository;
    private final CuotaCreditoRepository cuotaCreditoRepository;
    private final TarjetaRepository tarjetaRepository;

    @Autowired
    public CompraCreditoServiceImpl(
        CompraCreditoRepository compraCreditoRepository,
        EspacioTrabajoRepository espacioRepository,
        MotivoTransaccionRepository motivoRepository,
        ContactoTransferenciaRepository contactoRepository,
        CuotaCreditoRepository cuotaCreditoRepository,
        TarjetaRepository tarjetaRepository) {
        this.compraCreditoRepository = compraCreditoRepository;
        this.espacioRepository = espacioRepository;
        this.motivoRepository = motivoRepository;
        this.contactoRepository = contactoRepository;
        this.cuotaCreditoRepository = cuotaCreditoRepository;
        this.tarjetaRepository = tarjetaRepository;
    }

    @Override
    @Transactional
    public CompraCreditoListadoDTO registrarCompraCredito(CompraCreditoDTO compraCreditoDTO) {
        if (compraCreditoDTO == null) {
            logger.warn("Intento de registrar una compraCreditoDTO nula.");
            throw new IllegalArgumentException("La compra credito no puede ser nula");
        }
        logger.info("Iniciando registro de compraCredito por monto {} con cantidad de cuotas {} en espacio ID {}", compraCreditoDTO.montoTotal(), compraCreditoDTO.cantidadCuotas(), compraCreditoDTO.espacioTrabajoId());

        try {
            if (compraCreditoDTO.espacioTrabajoId() == null) {
                logger.warn("ID de espacio de trabajo nulo al registrar compra credito.");
                throw new IllegalArgumentException("El espacio de trabajo de la compra credito no puede ser nulo");
            }
            if (compraCreditoDTO.motivoId() == null) {
                logger.warn("ID de motivo nulo al registrar compra credito.");
                throw new IllegalArgumentException("El motivo de la compra credito no puede ser nulo");
            }
            if (compraCreditoDTO.tarjetaId() == null) {
                logger.warn("ID de tarjeta nulo al registrar compra credito.");
                throw new IllegalArgumentException("La tarjeta de la compra credito no puede ser nulo");
            }

            EspacioTrabajo espacio = espacioRepository.findById(compraCreditoDTO.espacioTrabajoId()).orElseThrow(() -> {
                String msg = "Espacio de trabajo con ID " + compraCreditoDTO.espacioTrabajoId() + " no encontrado";
                logger.warn(msg);
                return new EntityNotFoundException(msg);
            });
            MotivoTransaccion motivo = motivoRepository.findById(compraCreditoDTO.motivoId()).orElseThrow(() -> {
                String msg = "Motivo de transaccion con ID " + compraCreditoDTO.motivoId() + " no encontrado";
                logger.warn(msg);
                return new EntityNotFoundException(msg);
            });
            Tarjeta tarjeta = tarjetaRepository.findById(compraCreditoDTO.tarjetaId()).orElseThrow(() -> {
                String msg = "Tarjeta con ID " + compraCreditoDTO.tarjetaId() + " no encontrado";
                logger.warn(msg);
                return new EntityNotFoundException(msg);
            });

            CompraCredito compraCredito = compraCreditoDTO.toCompraCredito();

            if (compraCreditoDTO.comercioId() != null) {
                ContactoTransferencia comercio = contactoRepository.findById(compraCreditoDTO.comercioId()).orElseThrow(() -> {
                    String msg = "Comercio con ID " + compraCreditoDTO.comercioId() + " no encontrado";
                    logger.warn(msg);
                    return new EntityNotFoundException(msg);
                });
                compraCredito.setComercio(comercio);
            }

            ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");
            ZonedDateTime nowInBuenosAires = ZonedDateTime.now(buenosAiresZone);
            compraCredito.setFechaCreacion(nowInBuenosAires.toLocalDateTime());

            compraCredito.setEspacioTrabajo(espacio);
            compraCredito.setMotivo(motivo);
            compraCredito.setTarjeta(tarjeta);

            CompraCredito compraCreditoGuardada = compraCreditoRepository.save(compraCredito);
            crearCuotas(compraCreditoGuardada);
            logger.info("Compra credito ID {} registrada exitosamente en espacio ID {}.", compraCreditoGuardada.getId(), espacio.getId());
            
            return new CompraCreditoListadoDTO(
                compraCreditoGuardada.getId(),
                compraCreditoGuardada.getFechaCompra(),
                compraCreditoGuardada.getMontoTotal(),
                compraCreditoGuardada.getCantidadCuotas(),
                compraCreditoGuardada.getCuotasPagadas(),
                compraCreditoGuardada.getDescripcion(),
                compraCreditoGuardada.getNombreCompletoAuditoria(),
                compraCreditoGuardada.getFechaCreacion(),
                compraCreditoGuardada.getEspacioTrabajo().getId(),
                compraCreditoGuardada.getMotivo().getId(),
                compraCreditoGuardada.getComercio() != null ? compraCreditoGuardada.getComercio().getId() : null,
                compraCreditoGuardada.getTarjeta().getId()
            );

        } catch (Exception e) {
            logger.error("Error inesperado al registrar una compra credito en espacio ID {}: {}", compraCreditoDTO.espacioTrabajoId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void removerCompraCredito(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removerCompraCredito'");
    }

    @Override
    @Transactional
    public List<CompraCreditoListadoDTO> listarComprasCreditoDebeCuotas(Long idEspacioTrabajo) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listarComprasCreditoDebeCuotas'");
    }

    @Override
    @Transactional
    public List<CompraCreditoListadoDTO> BuscarComprasCredito(Long idEspacioTrabajo) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'BuscarComprasCredito'");
    }

    @Override
    @Transactional
    public TarjetaListadoDTO registrarTarjeta(TarjetaDTO tarjetaDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'registrarTarjeta'");
    }

    @Override
    @Transactional
    public void removerTarjeta(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removerTarjeta'");
    }

    @Override
    @Transactional
    public List<TarjetaListadoDTO> listarTarjetas(Long idEspacioTrabajo) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listarTarjetas'");
    }

    @Override
    @Transactional
    public void listarCuotasPorTarjeta(Long idTarjeta, LocalDate fechaActual) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listarCuotasPorTarjeta'");
    }

    @Override
    @Transactional
    public void pagarResumenTarjeta(List<CuotaCreditoDTO> cuotas, TransaccionDTO transaccion) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pagarResumenTarjeta'");
    }

    private void crearCuotas(CompraCredito compraCredito) {
        Tarjeta tarjeta = compraCredito.getTarjeta();

        if (compraCredito.getCantidadCuotas() <= 0) {
            logger.warn("Intento de crear cuotas para una compra con 0 o menos cuotas. Compra ID: {}", compraCredito.getId());
            return;
        }

        Float montoCuota = compraCredito.getMontoTotal() / compraCredito.getCantidadCuotas();
        
        LocalDate fechaCompra = compraCredito.getFechaCompra();
        Integer diaCierre = tarjeta.getDiaCierre();
        Integer diaVencimiento = tarjeta.getDiaVencimientoPago();

        LocalDate primerVencimiento;

        // Si la compra se realiza después del día de cierre de la tarjeta en el mes de la compra
        if (fechaCompra.getDayOfMonth() > diaCierre) {
            // La cuota entrará en el resumen del mes siguiente, y vencerá el mes subsiguiente.
            // Ejemplo: Cierre día 25. Compra día 28 de Julio. Entra en resumen que cierra 25 de Agosto. Vence en Septiembre.
            primerVencimiento = fechaCompra.plusMonths(2).withDayOfMonth(diaVencimiento);
        } else {
            // La cuota entrará en el resumen de este mes, y vencerá el mes siguiente.
            // Ejemplo: Cierre día 25. Compra día 20 de Julio. Entra en resumen que cierra 25 de Julio. Vence en Agosto.
            primerVencimiento = fechaCompra.plusMonths(1).withDayOfMonth(diaVencimiento);
        }

        for (int i = 0; i < compraCredito.getCantidadCuotas(); i++) {
            CuotaCredito cuota = new CuotaCredito();
            cuota.setCompraCredito(compraCredito);
            cuota.setNumeroCuota(i + 1);
            cuota.setPagada(false);
            cuota.setMontoCuota(montoCuota);
            cuota.setFechaVencimiento(primerVencimiento.plusMonths(i));
            cuotaCreditoRepository.save(cuota);
        }
        logger.info("Se crearon {} cuotas para la compra a crédito ID {}", compraCredito.getCantidadCuotas(), compraCredito.getId());
    }

}
