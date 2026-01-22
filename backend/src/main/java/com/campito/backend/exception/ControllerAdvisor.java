package com.campito.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class ControllerAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAdvisor.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionInfo> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        ExceptionInfo exceptionInfo = new ExceptionInfo(
                ex.getMessage(),
                request.getDescription(false),
                String.valueOf(System.currentTimeMillis()),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(exceptionInfo, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionInfo> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        ExceptionInfo exceptionInfo = new ExceptionInfo(
                ex.getMessage(),
                request.getDescription(false),
                String.valueOf(System.currentTimeMillis()),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(exceptionInfo, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ExceptionInfo> handleUsuarioNoEncontradoException(UsuarioNoEncontradoException ex, WebRequest request) {
        ExceptionInfo exceptionInfo = new ExceptionInfo(
                ex.getMessage(),
                request.getDescription(false),
                String.valueOf(System.currentTimeMillis()),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(exceptionInfo, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntidadDuplicadaException.class)
    public ResponseEntity<ExceptionInfo> handleEntidadDuplicadaException(EntidadDuplicadaException ex, WebRequest request) {
        ExceptionInfo exceptionInfo = new ExceptionInfo(
                ex.getMessage(),
                request.getDescription(false),
                String.valueOf(System.currentTimeMillis()),
                HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(exceptionInfo, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionInfo> handleGeneralException(Exception ex, WebRequest request) {
        logger.error("Error inesperado: {} - Request: {}", ex.getMessage(), request.getDescription(false), ex);
        ExceptionInfo exceptionInfo = new ExceptionInfo(
                ex.getMessage(),
                request.getDescription(false),
                String.valueOf(System.currentTimeMillis()),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(exceptionInfo, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionInfo> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((message1, message2) -> message1 + ", " + message2)
                .orElse("Validation error");

        ExceptionInfo exceptionInfo = new ExceptionInfo(
                errorMessage,
                request.getDescription(false),
                String.valueOf(System.currentTimeMillis()),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(exceptionInfo, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ExceptionInfo> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        ExceptionInfo exceptionInfo = new ExceptionInfo(
                ex.getMessage(),
                request.getDescription(false),
                String.valueOf(System.currentTimeMillis()),
                HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(exceptionInfo, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(PermisosDenegadosException.class)
    public ResponseEntity<ExceptionInfo> handlePermisosDenegadosException(PermisosDenegadosException ex, WebRequest request) {
        ExceptionInfo exceptionInfo = new ExceptionInfo(
                ex.getMessage(),
                request.getDescription(false),
                String.valueOf(System.currentTimeMillis()),
                HttpStatus.FORBIDDEN.value()
        );
        return new ResponseEntity<>(exceptionInfo, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ExceptionInfo> handleSaldoInsuficienteException(SaldoInsuficienteException ex, WebRequest request) {
        ExceptionInfo exceptionInfo = new ExceptionInfo(
                ex.getMessage(),
                request.getDescription(false),
                String.valueOf(System.currentTimeMillis()),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(exceptionInfo, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ExceptionInfo> handleOperacionNoPermitidaException(OperacionNoPermitidaException ex, WebRequest request) {
        ExceptionInfo exceptionInfo = new ExceptionInfo(
                ex.getMessage(),
                request.getDescription(false),
                String.valueOf(System.currentTimeMillis()),
                HttpStatus.UNPROCESSABLE_ENTITY.value()
        );
        return new ResponseEntity<>(exceptionInfo, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
