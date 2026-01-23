package com.campito.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campito.backend.dto.CompraCreditoDTORequest;
import com.campito.backend.dto.CompraCreditoDTOResponse;
import com.campito.backend.dto.CuotaCreditoDTOResponse;
import com.campito.backend.dto.PagarResumenTarjetaRequest;
import com.campito.backend.dto.ResumenDTOResponse;
import com.campito.backend.dto.TarjetaDTORequest;
import com.campito.backend.dto.TarjetaDTOResponse;
import com.campito.backend.service.CompraCreditoService;
import com.campito.backend.service.SecurityService;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comprascredito")
@Tag(name = "ComprasCredito", description = "Operaciones para la gestión de compras con crédito")
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
@Validated
public class ComprasCreditoController {

    private final CompraCreditoService comprasCreditoService;
    private final SecurityService securityService;

    @Operation(summary = "Registrar una nueva compra con crédito",
                description = "Permite registrar una nueva compra con crédito en el sistema.",
                responses = {
                    @ApiResponse(responseCode = "201", description = "Compra con crédito registrada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al registrar la compra con crédito"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @PostMapping("/registrar")
    public ResponseEntity<CompraCreditoDTOResponse> registrarCompraCredito(
        @Valid 
        @NotNull(message = "La compra a crédito es obligatoria") 
        @RequestBody CompraCreditoDTORequest comprasCreditoDTO) {
        
        securityService.validateWorkspaceAccess(comprasCreditoDTO.espacioTrabajoId());
        CompraCreditoDTOResponse nuevaCompra = comprasCreditoService.registrarCompraCredito(comprasCreditoDTO);
        return new ResponseEntity<>(nuevaCompra, HttpStatus.CREATED);
    }

    @Operation(summary = "Registrar una nueva tarjeta de credito",
                description = "Permite registrar una nueva tarjeta de crédito en el sistema.",
                responses = {
                    @ApiResponse(responseCode = "201", description = "Tarjeta de crédito registrada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al registrar la tarjeta de crédito"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @PostMapping("/registrarTarjeta")
    public ResponseEntity<TarjetaDTOResponse> registrarTarjeta(
        @Valid 
        @NotNull(message = "La tarjeta es obligatoria") 
        @RequestBody TarjetaDTORequest tarjetaDTO) {
        
        securityService.validateWorkspaceAccess(tarjetaDTO.espacioTrabajoId());
        TarjetaDTOResponse nuevaTarjeta = comprasCreditoService.registrarTarjeta(tarjetaDTO);
        return new ResponseEntity<>(nuevaTarjeta, HttpStatus.CREATED);
    }

    @Operation(summary = "Remover una compra a crédito",
                description = "Permite eliminar una compra a crédito del sistema. Solo se permite si ninguna cuota ha sido pagada.",
                responses = {
                    @ApiResponse(responseCode = "204", description = "Compra a crédito eliminada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al eliminar la compra a crédito"),
                    @ApiResponse(responseCode = "404", description = "Compra a crédito no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerCompraCredito(
        @PathVariable @NotNull(message = "El id de la compra a crédito es obligatorio") Long id) {
        
        securityService.validateCompraCreditoOwnership(id);
        comprasCreditoService.removerCompraCredito(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Listar compras a crédito con cuotas pendientes",
                description = "Obtiene todas las compras a crédito que tienen cuotas pendientes de pago en un espacio de trabajo.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de compras a crédito obtenida correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al obtener las compras a crédito"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @GetMapping("/pendientes/{idEspacioTrabajo}")
    public ResponseEntity<List<CompraCreditoDTOResponse>> listarComprasCreditoDebeCuotas(
        @PathVariable @NotNull(message = "El id del espacio de trabajo es obligatorio") UUID idEspacioTrabajo) {
        
        securityService.validateWorkspaceAccess(idEspacioTrabajo);
        List<CompraCreditoDTOResponse> compras = comprasCreditoService.listarComprasCreditoDebeCuotas(idEspacioTrabajo);
        return new ResponseEntity<>(compras, HttpStatus.OK);
    }

    @Operation(summary = "Buscar todas las compras a crédito",
                description = "Obtiene todas las compras a crédito de un espacio de trabajo.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de compras a crédito obtenida correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al obtener las compras a crédito"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @GetMapping("/buscar/{idEspacioTrabajo}")
    public ResponseEntity<List<CompraCreditoDTOResponse>> buscarComprasCredito(
        @PathVariable @NotNull(message = "El id del espacio de trabajo es obligatorio") UUID idEspacioTrabajo) {
        
        securityService.validateWorkspaceAccess(idEspacioTrabajo);
        List<CompraCreditoDTOResponse> compras = comprasCreditoService.BuscarComprasCredito(idEspacioTrabajo);
        return new ResponseEntity<>(compras, HttpStatus.OK);
    }

    @Operation(summary = "Remover una tarjeta",
                description = "Permite eliminar una tarjeta del sistema. Solo se permite si no tiene compras a crédito asociadas.",
                responses = {
                    @ApiResponse(responseCode = "204", description = "Tarjeta eliminada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al eliminar la tarjeta"),
                    @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @DeleteMapping("/tarjeta/{id}")
    public ResponseEntity<Void> removerTarjeta(
        @PathVariable @NotNull(message = "El id de la tarjeta es obligatorio") Long id) {
        
        securityService.validateTarjetaOwnership(id);
        comprasCreditoService.removerTarjeta(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Listar tarjetas de un espacio de trabajo",
                description = "Obtiene todas las tarjetas registradas en un espacio de trabajo.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de tarjetas obtenida correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al obtener las tarjetas"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @GetMapping("/tarjetas/{idEspacioTrabajo}")
    public ResponseEntity<List<TarjetaDTOResponse>> listarTarjetas(
        @PathVariable @NotNull(message = "El id del espacio de trabajo es obligatorio") UUID idEspacioTrabajo) {
        
        securityService.validateWorkspaceAccess(idEspacioTrabajo);
        List<TarjetaDTOResponse> tarjetas = comprasCreditoService.listarTarjetas(idEspacioTrabajo);
        return new ResponseEntity<>(tarjetas, HttpStatus.OK);
    }

    @Operation(summary = "Listar cuotas por tarjeta",
                description = "Obtiene las cuotas del período actual de una tarjeta específica.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de cuotas obtenida correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al obtener las cuotas"),
                    @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @GetMapping("/cuotas/{idTarjeta}")
    public ResponseEntity<List<CuotaCreditoDTOResponse>> listarCuotasPorTarjeta(
        @PathVariable @NotNull(message = "El id de la tarjeta es obligatorio") Long idTarjeta) {
        
        securityService.validateTarjetaOwnership(idTarjeta);
        List<CuotaCreditoDTOResponse> cuotas = comprasCreditoService.listarCuotasPorTarjeta(idTarjeta);
        return new ResponseEntity<>(cuotas, HttpStatus.OK);
    }

    @Operation(summary = "Pagar resumen de tarjeta",
                description = "Registra el pago de un resumen de tarjeta, marcando todas las cuotas asociadas como pagadas y registrando la transacción.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Resumen de tarjeta pagado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al pagar el resumen de tarjeta"),
                    @ApiResponse(responseCode = "404", description = "Resumen no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @PostMapping("/pagar-resumen")
    public ResponseEntity<Void> pagarResumenTarjeta(
        @Valid 
        @NotNull(message = "El pago del resumen es obligatorio") 
        @RequestBody PagarResumenTarjetaRequest request) {
        
        comprasCreditoService.pagarResumenTarjeta(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Listar resúmenes por tarjeta",
                description = "Obtiene todos los resúmenes de una tarjeta específica ordenados por fecha descendente.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de resúmenes obtenida correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al obtener los resúmenes"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @GetMapping("/resumenes/tarjeta/{idTarjeta}")
    public ResponseEntity<List<ResumenDTOResponse>> listarResumenesPorTarjeta(
        @PathVariable @NotNull(message = "El id de la tarjeta es obligatorio") Long idTarjeta) {
        
        securityService.validateTarjetaOwnership(idTarjeta);
        List<ResumenDTOResponse> resumenes = comprasCreditoService.listarResumenesPorTarjeta(idTarjeta);
        return new ResponseEntity<>(resumenes, HttpStatus.OK);
    }

    @Operation(summary = "Listar resúmenes por espacio de trabajo",
                description = "Obtiene todos los resúmenes de un espacio de trabajo ordenados por fecha descendente.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de resúmenes obtenida correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al obtener los resúmenes"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @GetMapping("/resumenes/espacio/{idEspacioTrabajo}")
    public ResponseEntity<List<ResumenDTOResponse>> listarResumenesPorEspacioTrabajo(
        @PathVariable @NotNull(message = "El id del espacio de trabajo es obligatorio") UUID idEspacioTrabajo) {
        
        securityService.validateWorkspaceAccess(idEspacioTrabajo);
        List<ResumenDTOResponse> resumenes = comprasCreditoService.listarResumenesPorEspacioTrabajo(idEspacioTrabajo);
        return new ResponseEntity<>(resumenes, HttpStatus.OK);
    }
}
