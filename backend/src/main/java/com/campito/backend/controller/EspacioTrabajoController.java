package com.campito.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campito.backend.dto.EspacioTrabajoDTORequest;
import com.campito.backend.dto.EspacioTrabajoDTOResponse;
import com.campito.backend.dto.SolicitudPendienteEspacioTrabajoDTOResponse;
import com.campito.backend.dto.UsuarioDTOResponse;
import com.campito.backend.service.EspacioTrabajoService;
import com.campito.backend.service.SecurityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/espaciotrabajo")
@Tag(name = "EspacioTrabajo", description = "Operaciones para la gestión de espacios de trabajo")
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
@Validated
public class EspacioTrabajoController {

    private final EspacioTrabajoService espacioTrabajoService;
    private final SecurityService securityService;

    @Operation(
        summary = "Registrar un nuevo espacio de trabajo",
        description = "Permite registrar un nuevo espacio de trabajo en el sistema."
    )
    @ApiResponse(responseCode = "201", description = "Espacio de trabajo registrado correctamente")
    @ApiResponse(responseCode = "400", description = "Error al registrar el espacio de trabajo")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping("/registrar")
    public ResponseEntity<Void> registrarEspacioTrabajo(
        @Valid 
        @NotNull(message = "El espacio de trabajo es obligatorio") 
        @RequestBody EspacioTrabajoDTORequest espacioTrabajoDTO) {
        
        espacioTrabajoService.registrarEspacioTrabajo(espacioTrabajoDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(
        summary = "Compartir espacio de trabajo",
        description = "Permite compartir un espacio de trabajo con un usuario. Solo el administrador puede compartir."
    )
    @ApiResponse(responseCode = "200", description = "Espacio de trabajo compartido correctamente")
    @ApiResponse(responseCode = "400", description = "Error al compartir el espacio de trabajo")
    @ApiResponse(responseCode = "403", description = "No tienes permisos de administrador")
    @ApiResponse(responseCode = "404", description = "Espacio de trabajo o usuario no encontrado")
    @ApiResponse(responseCode = "409", description = "Ya existe una invitación pendiente para este usuario y espacio de trabajo")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PutMapping("/compartir/{email}/{idEspacioTrabajo}")
    public ResponseEntity<Void> compartirEspacioTrabajo(
            @PathVariable 
            @NotBlank(message = "El email no puede estar vacío")
            @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
            @Email(message = "Debe proporcionar un email válido")
            @Pattern(
                regexp = "^[a-zA-Z0-9@.\\-_]+$",
                message = "El email solo puede contener letras, números, @, punto, guiones y barra baja"
            )
            String email,
            @PathVariable @NotNull(message = "El id del espacio de trabajo es obligatorio") UUID idEspacioTrabajo) {
        
        // Validar que el usuario autenticado es el admin del espacio
        securityService.validateWorkspaceAdmin(idEspacioTrabajo);
        
        espacioTrabajoService.compartirEspacioTrabajo(email, idEspacioTrabajo);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
        summary = "Responder a una solicitud pendiente de compartir espacio de trabajo",
        description = "Permite al usuario responder a una solicitud pendiente de compartir un espacio de trabajo, aceptando o rechazando la invitación."
    )
    @ApiResponse(responseCode = "200", description = "Respuesta a la solicitud procesada correctamente")
    @ApiResponse(responseCode = "400", description = "Error al procesar la respuesta a la solicitud")
    @ApiResponse(responseCode = "403", description = "No tienes permisos para responder a esta solicitud")
    @ApiResponse(responseCode = "404", description = "Solicitud pendiente no encontrada")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PutMapping("/solicitud/responder/{idSolicitud}/{aceptada}")
    public ResponseEntity<Void> responderSolicitudCompartirEspacioTrabajo(
            @PathVariable @NotNull(message = "El id de la solicitud es obligatorio") Long idSolicitud,
            @PathVariable @NotNull(message = "La respuesta (aceptada o rechazada) es obligatoria") Boolean aceptada) {
        
        // Validar que el usuario autenticado tiene permiso para responder a esta solicitud
        securityService.validateSolicitudOwnership(idSolicitud);
        
        espacioTrabajoService.respuestaSolicitudCompartirEspacioTrabajo(idSolicitud, aceptada);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
        summary = "Listar mis espacios de trabajo",
        description = "Permite listar todos los espacios de trabajo donde participa el usuario autenticado."
    )
    @ApiResponse(responseCode = "200", description = "Espacios de trabajo listados correctamente")
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/listar")
    public ResponseEntity<List<EspacioTrabajoDTOResponse>> listarMisEspaciosTrabajo() {
        
        // Obtener el ID del usuario autenticado desde el contexto de seguridad
        UUID userId = securityService.getAuthenticatedUserId();
        
        List<EspacioTrabajoDTOResponse> espacios = espacioTrabajoService.listarEspaciosTrabajoPorUsuario(userId);
        return new ResponseEntity<>(espacios, HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener una lista de miembros de un espacio de trabajo",
        description = "Permite obtener una lista de todos los usuarios que son miembros de un espacio de trabajo específico."
    )
    @ApiResponse(responseCode = "200", description = "Miembros del espacio de trabajo obtenidos correctamente")
    @ApiResponse(responseCode = "403", description = "No tienes acceso a este espacio de trabajo")
    @ApiResponse(responseCode = "404", description = "Espacio de trabajo no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/miembros/{idEspacioTrabajo}")
    public ResponseEntity<List<UsuarioDTOResponse>> obtenerMiembrosEspacioTrabajo(
        @PathVariable @NotNull(message = "El id del espacio de trabajo es obligatorio") UUID idEspacioTrabajo) {
        
        // Validar que el usuario autenticado tiene acceso al espacio
        securityService.validateWorkspaceAccess(idEspacioTrabajo);
        
        List<UsuarioDTOResponse> miembros = espacioTrabajoService.obtenerMiembrosEspacioTrabajo(idEspacioTrabajo);
        return new ResponseEntity<>(miembros, HttpStatus.OK);
    }

    @Operation(
        summary = "Listar mis solicitudes pendientes de espacios de trabajo",
        description = "Permite listar todas mis solicitudes pendientes para integrar espacios de trabajo del usuario autenticado."
    )
    @ApiResponse(responseCode = "200", description = "Solicitudes pendientes listadas correctamente")
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/solicitudes/pendientes")
    public ResponseEntity<List<SolicitudPendienteEspacioTrabajoDTOResponse>> listarMisSolicitudesPendientes() {
        
        // Obtener el ID del usuario autenticado desde el contexto de seguridad
        UUID userId = securityService.getAuthenticatedUserId();
        
        List<SolicitudPendienteEspacioTrabajoDTOResponse> solicitudes = espacioTrabajoService.listarSolicitudesPendientes(userId);
        return new ResponseEntity<>(solicitudes, HttpStatus.OK);
    }
}