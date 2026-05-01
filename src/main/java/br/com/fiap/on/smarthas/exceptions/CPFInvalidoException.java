package br.com.fiap.on.smarthas.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CPFInvalidoException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public CPFInvalidoException(String mensagem) {
        super(mensagem);
    }
}