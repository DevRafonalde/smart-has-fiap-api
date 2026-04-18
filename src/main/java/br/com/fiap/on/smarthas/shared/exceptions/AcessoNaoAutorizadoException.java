package br.com.fiap.on.smarthas.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AcessoNaoAutorizadoException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public AcessoNaoAutorizadoException(String message) {
        super(message);
    }
}
