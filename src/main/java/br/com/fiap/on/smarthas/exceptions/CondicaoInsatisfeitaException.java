package br.com.fiap.on.smarthas.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CondicaoInsatisfeitaException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public CondicaoInsatisfeitaException(String message) {
        super(message);
    }
}
