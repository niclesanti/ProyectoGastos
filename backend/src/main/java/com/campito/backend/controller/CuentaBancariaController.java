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

import com.campito.backend.dto.CuentaBancariaDTORequest;
import com.campito.backend.dto.CuentaBancariaDTOResponse;
import com.campito.backend.dto.DescuentoDTORequest;
import com.campito.backend.dto.DescuentoDTOResponse;
import com.campito.backend.dto.TransaccionCuentaRequest;
import com.campito.backend.service.CuentaBancariaService;
import com.campito.backend.service.SecurityService;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/cuentas-bancarias")
@Tag(name = "CuentaBancaria", description = "Operaciones para la gestión de cuentas bancarias")
@RequiredArgsConstructor
@Validated
public class CuentaBancariaController {

    private final CuentaBancariaService cuentaBancariaService;
    private final SecurityService securityService;

    @Operation(
        summary = "Crear una nueva cuenta bancaria",
        description = "Permite crear una nueva cuenta bancaria asociada a un espacio de trabajo."
    )
    @ApiResponse(responseCode = "201", description = "Cuenta bancaria creada correctamente")
    @ApiResponse(responseCode = "400", description = "Error al crear la cuenta bancaria")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping
    public ResponseEntity<Void> crearCuentaBancaria(
        @Valid 
        @NotNull(message = "La cuenta bancaria es obligatoria") 
        @RequestBody CuentaBancariaDTORequest cuentaBancariaDTO) {
        
        securityService.validateWorkspaceAccess(cuentaBancariaDTO.idEspacioTrabajo());
        cuentaBancariaService.crearCuentaBancaria(cuentaBancariaDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(
        summary = "Listar cuentas bancarias por espacio de trabajo",
        description = "Permite listar todas las cuentas bancarias asociadas a un espacio de trabajo."
    )
    @ApiResponse(responseCode = "200", description = "Cuentas bancarias listadas correctamente")
    @ApiResponse(responseCode = "400", description = "Error al listar las cuentas bancarias")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/espacio/{idEspacioTrabajo}")
    public ResponseEntity<List<CuentaBancariaDTOResponse>> listarCuentasBancarias(
        @PathVariable @NotNull(message = "El id del espacio de trabajo es obligatorio") UUID idEspacioTrabajo) {
        
        securityService.validateWorkspaceAccess(idEspacioTrabajo);
        List<CuentaBancariaDTOResponse> cuentas = cuentaBancariaService.listarCuentasBancarias(idEspacioTrabajo);
        return new ResponseEntity<>(cuentas, HttpStatus.OK);
    }

    @Operation(
        summary = "Realizar una transacción entre cuentas bancarias",
        description = "Permite realizar una transacción de dinero entre dos cuentas bancarias."
    )
    @ApiResponse(responseCode = "200", description = "Transacción realizada correctamente")
    @ApiResponse(responseCode = "400", description = "Error al realizar la transacción")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping("/transacciones")
    public ResponseEntity<Void> realizarTransaccion(
            @Valid @NotNull(message = "La transacción es obligatoria")
            @RequestBody TransaccionCuentaRequest request) {
            
        securityService.validateCuentaBancariaOwnership(request.idCuentaOrigen());
        securityService.validateCuentaBancariaOwnership(request.idCuentaDestino());
        cuentaBancariaService.transaccionEntreCuentas(request.idCuentaOrigen(), request.idCuentaDestino(), request.monto());
        return ResponseEntity.ok().build();
    }

    // =========================================================
    // Endpoints de Descuentos
    // =========================================================

    @Operation(
        summary = "Crear un nuevo descuento",
        description = "Permite registrar un descuento disponible asociado a un espacio de trabajo."
    )
    @ApiResponse(responseCode = "201", description = "Descuento creado correctamente")
    @ApiResponse(responseCode = "400", description = "Error de validación en los datos del descuento")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping("/descuentos")
    public ResponseEntity<Void> crearDescuento(
        @Valid
        @NotNull(message = "El descuento es obligatorio")
        @RequestBody DescuentoDTORequest descuentoDTO) {

        securityService.validateWorkspaceAccess(descuentoDTO.idEspacioTrabajo());
        cuentaBancariaService.crearDescuento(descuentoDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(
        summary = "Listar descuentos por espacio de trabajo",
        description = "Retorna todos los descuentos registrados para un espacio de trabajo."
    )
    @ApiResponse(responseCode = "200", description = "Descuentos listados correctamente")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/descuentos/espacio/{idEspacioTrabajo}")
    public ResponseEntity<List<DescuentoDTOResponse>> listarDescuentos(
        @PathVariable @NotNull(message = "El id del espacio de trabajo es obligatorio") UUID idEspacioTrabajo) {

        securityService.validateWorkspaceAccess(idEspacioTrabajo);
        List<DescuentoDTOResponse> descuentos = cuentaBancariaService.listarDescuentos(idEspacioTrabajo);
        return new ResponseEntity<>(descuentos, HttpStatus.OK);
    }

    @Operation(
        summary = "Eliminar un descuento",
        description = "Elimina un descuento por su ID."
    )
    @ApiResponse(responseCode = "204", description = "Descuento eliminado correctamente")
    @ApiResponse(responseCode = "404", description = "Descuento no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @DeleteMapping("/descuentos/{id}")
    public ResponseEntity<Void> eliminarDescuento(
        @PathVariable @NotNull(message = "El id del descuento es obligatorio") Long id) {
        
        securityService.validateDescuentoOwnership(id);
        cuentaBancariaService.eliminarDescuento(id);
        return ResponseEntity.noContent().build();
    }

}
