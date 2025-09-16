package com.campito.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campito.backend.dto.CompraCreditoDTO;
import com.campito.backend.dto.CompraCreditoListadoDTO;
import com.campito.backend.service.CompraCreditoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/comprascredito")
@Tag(name = "ComprasCredito", description = "Operaciones para la gestión de compras con crédito")
public class ComprasCreditoController {

    private final CompraCreditoService comprasCreditoService;

    @Autowired
    public ComprasCreditoController(CompraCreditoService comprasCreditoService) {
        this.comprasCreditoService = comprasCreditoService;
    }

    @Operation(summary = "Registrar una nueva compra con crédito",
                description = "Permite registrar una nueva compra con crédito en el sistema.",
                responses = {
                    @ApiResponse(responseCode = "201", description = "Compra con crédito registrada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al registrar la compra con crédito"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @PostMapping("/registrar")
    public ResponseEntity<CompraCreditoListadoDTO> registrarCompraCredito(@Valid @RequestBody CompraCreditoDTO comprasCreditoDTO) {
        CompraCreditoListadoDTO nuevaCompra = comprasCreditoService.registrarCompraCredito(comprasCreditoDTO);
        return new ResponseEntity<>(nuevaCompra, HttpStatus.CREATED);
    }


}
