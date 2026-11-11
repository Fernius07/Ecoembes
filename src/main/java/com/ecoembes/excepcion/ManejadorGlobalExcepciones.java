package com.ecoembes.excepcion;

import com.ecoembes.dto.ErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(ExcepcionNoAutorizado.class)
    public ResponseEntity<ErrorDTO> manejarNoAutorizado(ExcepcionNoAutorizado ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.UNAUTHORIZED, "No autorizado", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ExcepcionNoEncontrado.class)
    public ResponseEntity<ErrorDTO> manejarNoEncontrado(ExcepcionNoEncontrado ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ExcepcionPeticionInvalida.class)
    public ResponseEntity<ErrorDTO> manejarPeticionInvalida(ExcepcionPeticionInvalida ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Petición inválida", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Error de validación", detalles, request.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDTO> manejarParametroFaltante(MissingServletRequestParameterException ex, HttpServletRequest request) {
        if ("token".equals(ex.getParameterName())) {
            return construirRespuesta(HttpStatus.UNAUTHORIZED, "No autorizado",
                    "El parámetro 'token' es obligatorio", request.getRequestURI());
        }
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Petición inválida",
                "Falta el parámetro obligatorio: " + ex.getParameterName(), request.getRequestURI());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorDTO> manejarErrorFormato(Exception ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Formato inválido",
                "Los datos enviados no tienen el formato esperado", request.getRequestURI());
    }

    private ResponseEntity<ErrorDTO> construirRespuesta(HttpStatus status, String tipoError, String mensaje, String ruta) {
        ErrorDTO cuerpo = new ErrorDTO(
                LocalDateTime.now(),
                status.value(),
                tipoError,
                mensaje,
                ruta
        );
        return ResponseEntity.status(status).body(cuerpo);
    }
}
