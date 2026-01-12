package com.campito.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campito.backend.dto.CuentaBancariaDTORequest;
import com.campito.backend.dto.CuentaBancariaDTOResponse;
import com.campito.backend.service.CuentaBancariaService;
import com.campito.backend.validation.ValidMonto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/cuentabancaria")
@Tag(name = "CuentaBancaria", description = "Operaciones para la gestión de cuentas bancarias")
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
@Validated  // Habilita validación en @PathVariable
public class CuentaBancariaController {

    private final CuentaBancariaService cuentaBancariaService;

    @Operation(
        summary = "Crear una nueva cuenta bancaria",
        description = "Permite crear una nueva cuenta bancaria asociada a un espacio de trabajo."
    )
    @ApiResponse(responseCode = "201", description = "Cuenta bancaria creada correctamente")
    @ApiResponse(responseCode = "400", description = "Error al crear la cuenta bancaria")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping("/crear")
    public ResponseEntity<Void> crearCuentaBancaria(@Valid @RequestBody CuentaBancariaDTORequest cuentaBancariaDTO) {
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
    @GetMapping("/listar/{idEspacioTrabajo}")
    public ResponseEntity<List<CuentaBancariaDTOResponse>> listarCuentasBancarias(@PathVariable Long idEspacioTrabajo) {
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
    @PutMapping("/transaccion/{idCuentaOrigen}/{idCuentaDestino}/{monto}")
    public ResponseEntity<Void> realizarTransaccion(
            @PathVariable @NotNull(message = "La cuenta de origen es obligatoria") Long idCuentaOrigen, 
            @PathVariable @NotNull(message = "La cuenta de destino es obligatoria") Long idCuentaDestino, 
            @PathVariable @NotNull(message = "El monto es obligatorio") 
            @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
            @ValidMonto Float monto) {
        cuentaBancariaService.transaccionEntreCuentas(idCuentaOrigen, idCuentaDestino, monto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
