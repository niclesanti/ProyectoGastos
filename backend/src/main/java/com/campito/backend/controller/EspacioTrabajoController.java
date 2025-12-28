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

import com.campito.backend.dto.EspacioTrabajoDTORequest;
import com.campito.backend.dto.EspacioTrabajoDTOResponse;
import com.campito.backend.dto.UsuarioDTOResponse;
import com.campito.backend.service.EspacioTrabajoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/espaciotrabajo")
@Tag(name = "EspacioTrabajo", description = "Operaciones para la gestión de espacios de trabajo")
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
public class EspacioTrabajoController {

    private final EspacioTrabajoService espacioTrabajoService;

    @Operation(
        summary = "Registrar un nuevo espacio de trabajo",
        description = "Permite registrar un nuevo espacio de trabajo en el sistema."
    )
    @ApiResponse(responseCode = "201", description = "Espacio de trabajo registrado correctamente")
    @ApiResponse(responseCode = "400", description = "Error al registrar el espacio de trabajo")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping("/registrar")
    public ResponseEntity<Void> registrarEspacioTrabajo(@Valid @RequestBody EspacioTrabajoDTORequest espacioTrabajoDTO) {
        espacioTrabajoService.registrarEspacioTrabajo(espacioTrabajoDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(
        summary = "Compartir espacio de trabajo",
        description = "Permite compartir un espacio de trabajo con un usuario."
    )
    @ApiResponse(responseCode = "200", description = "Espacio de trabajo compartido correctamente")
    @ApiResponse(responseCode = "400", description = "Error al compartir el espacio de trabajo")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PutMapping("/compartir/{email}/{idEspacioTrabajo}/{idUsuarioAdmin}")
    public ResponseEntity<Void> compartirEspacioTrabajo(
            @PathVariable String email,
            @PathVariable Long idEspacioTrabajo,
            @PathVariable Long idUsuarioAdmin) {
        espacioTrabajoService.compartirEspacioTrabajo(email, idEspacioTrabajo, idUsuarioAdmin);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
        summary = "Listar espacios de trabajo según usuario",
        description = "Permite listar todos los espacios de trabajo donde participa un usuario."
    )
    @ApiResponse(responseCode = "200", description = "Espacios de trabajo listados correctamente")
    @ApiResponse(responseCode = "400", description = "Error al listar los espacios de trabajo")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/listar/{idUsuario}")
    public ResponseEntity<List<EspacioTrabajoDTOResponse>> listarEspaciosTrabajoPorUsuario(@PathVariable Long idUsuario) {
        List<EspacioTrabajoDTOResponse> espacios = espacioTrabajoService.listarEspaciosTrabajoPorUsuario(idUsuario);
        return new ResponseEntity<>(espacios, HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener una lista de miembros de un espacio de trabajo",
        description = "Permite obtener una lista de todos los usuarios que son miembros de un espacio de trabajo específico."
    )
    @ApiResponse(responseCode = "200", description = "Miembros del espacio de trabajo obtenidos correctamente")
    @ApiResponse(responseCode = "400", description = "Error al obtener los miembros del espacio de trabajo")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/miembros/{idEspacioTrabajo}")
    public ResponseEntity<List<UsuarioDTOResponse>> obtenerMiembrosEspacioTrabajo(@PathVariable Long idEspacioTrabajo) {
        List<UsuarioDTOResponse> miembros = espacioTrabajoService.obtenerMiembrosEspacioTrabajo(idEspacioTrabajo);
        return new ResponseEntity<>(miembros, HttpStatus.OK);
    }

}
