package com.campito.backend.descuentos.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campito.backend.descuentos.domain.dto.DescuentoDTORequest;
import com.campito.backend.descuentos.domain.dto.DescuentoDTOResponse;
import com.campito.backend.descuentos.service.DescuentoService;
import com.campito.backend.service.SecurityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/descuento")
@Tag(name = "Descuento", description = "Operaciones para la gestión de descuentos")
@RequiredArgsConstructor
@Validated
public class DescuentoController {

    private final SecurityService securityService;
    private final DescuentoService descuentoService;

    @Operation(
        summary = "Crear un nuevo descuento",
        description = "Permite registrar un descuento disponible asociado a un espacio de trabajo."
    )
    @ApiResponse(responseCode = "201", description = "Descuento creado correctamente")
    @ApiResponse(responseCode = "400", description = "Error de validación en los datos del descuento")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping
    public ResponseEntity<Void> crearDescuento(
        @Valid
        @NotNull(message = "El descuento es obligatorio")
        @RequestBody DescuentoDTORequest descuentoDTO) {

        securityService.validateWorkspaceAccess(descuentoDTO.idEspacioTrabajo());
        descuentoService.crearDescuento(descuentoDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(
        summary = "Listar descuentos por espacio de trabajo",
        description = "Retorna todos los descuentos registrados para un espacio de trabajo."
    )
    @ApiResponse(responseCode = "200", description = "Descuentos listados correctamente")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/espacio/{idEspacioTrabajo}")
    public ResponseEntity<List<DescuentoDTOResponse>> listarDescuentos(
        @PathVariable @NotNull(message = "El id del espacio de trabajo es obligatorio") UUID idEspacioTrabajo) {

        securityService.validateWorkspaceAccess(idEspacioTrabajo);
        List<DescuentoDTOResponse> descuentos = descuentoService.listarDescuentos(idEspacioTrabajo);
        return new ResponseEntity<>(descuentos, HttpStatus.OK);
    }

    @Operation(
        summary = "Eliminar un descuento",
        description = "Elimina un descuento por su ID."
    )
    @ApiResponse(responseCode = "204", description = "Descuento eliminado correctamente")
    @ApiResponse(responseCode = "404", description = "Descuento no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDescuento(
        @PathVariable @NotNull(message = "El id del descuento es obligatorio") Long id) {
        
        securityService.validateDescuentoOwnership(id);
        descuentoService.eliminarDescuento(id);
        return ResponseEntity.noContent().build();
    }
}
