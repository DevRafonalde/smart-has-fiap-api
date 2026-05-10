package br.com.fiap.on.smarthas.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AcessoNaoAutorizadoException.class)
    public ResponseEntity<Map<String, Object>> handleUNAUTHORIZED(AcessoNaoAutorizadoException ex) {
        return montarRetorno(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AtributoJaUtilizadoException.class)
    public ResponseEntity<Map<String, Object>> handleCONFLICTAtributo(AtributoJaUtilizadoException ex) {
        return montarRetorno(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CondicaoInsatisfeitaException.class)
    public ResponseEntity<Map<String, Object>> handleFORBIDDEN(CondicaoInsatisfeitaException ex) {
        return montarRetorno(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(CPFInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleBAD_REQUESTCPF(CPFInvalidoException ex) {
        return montarRetorno(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ElementoExistenteException.class)
    public ResponseEntity<Map<String, Object>> handleCONFLICTElemento(ElementoExistenteException ex) {
        return montarRetorno(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ElementoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleNOT_FOUNDNaoEncontrado(ElementoNaoEncontradoException ex) {
        return montarRetorno(ex, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<Map<String, Object>> montarRetorno(RuntimeException ex, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        body.put("status", status.value());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}
