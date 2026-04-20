package br.com.fiap.on.smarthas.auth.api.controllers;

import br.com.fiap.on.smarthas.auth.internal.models.entities.dto.VerificacaoEmailRequestDTO;
import br.com.fiap.on.smarthas.auth.internal.services.JwtService;
import br.com.fiap.on.smarthas.auth.internal.services.VerificacaoEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth/verificacao-email")
@RequiredArgsConstructor
public class VerificacaoEmailController {

    private final VerificacaoEmailService verificacaoEmailService;
    private final JwtService jwtService;

    // Verifica o código enviado pelo usuário
    // O usuário precisa estar logado (JWT válido) para verificar o próprio e-mail
    @PostMapping("/verificar")
    public ResponseEntity<Void> verificar(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid VerificacaoEmailRequestDTO request
    ) {
        int idUsuario = extrairIdDoToken(authHeader);
        verificacaoEmailService.verificar(idUsuario, request.getCodigo());
        return ResponseEntity.noContent().build();
    }

    // Reenvia um novo código para o e-mail do usuário autenticado
    // Útil quando o código anterior expirou ou não chegou
    @PostMapping("/reenviar")
    public ResponseEntity<Void> reenviar(
            @RequestHeader("Authorization") String authHeader
    ) {
        int idUsuario = extrairIdDoToken(authHeader);
        verificacaoEmailService.reenviarCodigo(idUsuario);
        return ResponseEntity.accepted().build(); // 202 — processamento assíncrono
    }

    private int extrairIdDoToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtService.validarTokenERetornarId(token);
    }
}