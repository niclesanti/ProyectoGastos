package com.campito.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campito.backend.dto.ContactoDTORequest;
import com.campito.backend.dto.ContactoDTOResponse;
import com.campito.backend.dto.DashboardStatsDTO;
import com.campito.backend.dto.MotivoDTORequest;
import com.campito.backend.dto.MotivoDTOResponse;
import com.campito.backend.dto.TransaccionBusquedaDTO;
import com.campito.backend.dto.TransaccionDTORequest;
import com.campito.backend.dto.TransaccionDTOResponse;
import com.campito.backend.service.TransaccionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;



@RestController
@RequestMapping("/api/transaccion")
@Tag(name = "Transaccion", description = "Operaciones para la gestión de transacciones")
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
public class TransaccionController {

    private final TransaccionService transaccionService;

    @Operation(summary = "Registrar una nueva transacción",
                description = "Permite registrar una nueva transacción en el sistema.",
                responses = {
                    @ApiResponse(responseCode = "201", description = "Transacción registrada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al registrar la transacción"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @PostMapping("/registrar")
    public ResponseEntity<TransaccionDTOResponse> registrarTransaccion(@Valid @RequestBody TransaccionDTORequest transaccionDTO) {
        TransaccionDTOResponse nuevaTransaccion = transaccionService.registrarTransaccion(transaccionDTO);
        return new ResponseEntity<>(nuevaTransaccion, HttpStatus.CREATED);
    }

    @Operation(summary = "Remover transacción",
                description = "Permite remover una transacción existente en el sistema.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Transacción removida correctamente"),
                    @ApiResponse(responseCode = "404", description = "Transacción no encontrada"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @DeleteMapping("/remover/{id}")
    public ResponseEntity<Void> removerTransaccion(@PathVariable Long id) {
        transaccionService.removerTransaccion(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Buscar transacciones",
                description = "Permite buscar transacciones según criterios específicos.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Transacciones encontradas"),
                    @ApiResponse(responseCode = "400", description = "Error en los criterios de búsqueda"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @PostMapping("/buscar")
    public ResponseEntity<List<TransaccionDTOResponse>> buscarTransaccion(@Valid @RequestBody TransaccionBusquedaDTO datosBusqueda) {
        List<TransaccionDTOResponse> transacciones = transaccionService.buscarTransaccion(datosBusqueda);
        return new ResponseEntity<>(transacciones, HttpStatus.OK);
    }

    @Operation(summary = "Registrar contacto emisor/destinatario de la transacción.",
                description = "Permite registrar un nuevo contacto emisor/destinatario de la transacción.",
                responses = {
                    @ApiResponse(responseCode = "201", description = "Contacto registrado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al registrar el contacto"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @PostMapping("/contacto/registrar")
    public ResponseEntity<ContactoDTOResponse> registrarContactoTransferencia(@Valid @RequestBody ContactoDTORequest contactoDTO) {
        ContactoDTOResponse nuevoContacto = transaccionService.registrarContactoTransferencia(contactoDTO);
        return new ResponseEntity<>(nuevoContacto, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar contactos de transacción por espacio de trabajo",
                description = "Permite listar los contactos de transacción asociados a un espacio de trabajo.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Contactos listados correctamente"),
                    @ApiResponse(responseCode = "404", description = "Espacio de trabajo no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @GetMapping("/contacto/listar/{idEspacioTrabajo}")
    public ResponseEntity<List<ContactoDTOResponse>> listarContactos(@PathVariable Long idEspacioTrabajo) {
        List<ContactoDTOResponse> contactos = transaccionService.listarContactos(idEspacioTrabajo);
        return new ResponseEntity<>(contactos, HttpStatus.OK);
    }

    @Operation(summary = "Registrar motivo de transacción",
                description = "Permite registrar un nuevo motivo de transacción.",
                responses = {
                    @ApiResponse(responseCode = "201", description = "Motivo registrado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Error al registrar el motivo"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @PostMapping("/motivo/registrar")
    public ResponseEntity<MotivoDTOResponse> nuevoMotivoTransaccion(@Valid @RequestBody MotivoDTORequest motivoDTO) {
        MotivoDTOResponse nuevoMotivo = transaccionService.nuevoMotivoTransaccion(motivoDTO);
        return new ResponseEntity<>(nuevoMotivo, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar motivos de transacción por espacio de trabajo",
                description = "Permite listar los motivos de transacción asociados a un espacio de trabajo.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Motivos listados correctamente"),
                    @ApiResponse(responseCode = "404", description = "Espacio de trabajo no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @GetMapping("/motivo/listar/{idEspacioTrabajo}")
    public ResponseEntity<List<MotivoDTOResponse>> listarMotivos(@PathVariable Long idEspacioTrabajo) {
        List<MotivoDTOResponse> motivos = transaccionService.listarMotivos(idEspacioTrabajo);
        return new ResponseEntity<>(motivos, HttpStatus.OK);
    }

    @Operation(summary = "Buscar transacciones recientes",
                description = "Busca las ultimas 6 transaciones realizadas en un espacio de trabajo.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Transacciones encontradas"),
                    @ApiResponse(responseCode = "400", description = "Error en los criterios de búsqueda"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @GetMapping("/buscarRecientes/{idEspacio}")
    public ResponseEntity<List<TransaccionDTOResponse>> buscarTransaccionesRecientes(@PathVariable Long idEspacio) {
        List<TransaccionDTOResponse> transacciones = transaccionService.buscarTransaccionesRecientes(idEspacio);
        return new ResponseEntity<>(transacciones, HttpStatus.OK);
    }

    @Operation(summary = "Obtener estadísticas consolidadas del dashboard",
                description = "Obtiene todas las estadísticas del dashboard (KPIs + charts) en una sola llamada.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Estadísticas del dashboard obtenidas correctamente"),
                    @ApiResponse(responseCode = "404", description = "Espacio de trabajo no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @GetMapping("/dashboard-stats/{idEspacio}")
    public ResponseEntity<DashboardStatsDTO> obtenerDashboardStats(@PathVariable Long idEspacio) {
        DashboardStatsDTO dashboardStats = transaccionService.obtenerDashboardStats(idEspacio);
        return new ResponseEntity<>(dashboardStats, HttpStatus.OK);
    }
}
