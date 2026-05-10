package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.model.entities.dto.VerificacaoEmailRequestDTO;
import br.com.fiap.on.smarthas.services.JwtService;
import br.com.fiap.on.smarthas.services.VerificacaoEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth/verificacao-email")
@RequiredArgsConstructor
@Tag(name = "Verificação de E-mail", description = "Confirmação do endereço de e-mail do usuário após o registro. " +
        "O código de 6 dígitos é enviado automaticamente ao cadastrar e pode ser reenviado quando necessário.")
public class VerificacaoEmailController {

    private final VerificacaoEmailService verificacaoEmailService;
    private final JwtService jwtService;

    @Operation(
            summary = "Verificar código de confirmação",
            description = "Recebe o código de 6 dígitos enviado ao e-mail do usuário e confirma o endereço. " +
                    "O código expira em 15 minutos (configurável em `EMAIL_VERIFICACAO_EXPIRACAO`). " +
                    "Após a verificação bem-sucedida o campo `emailVerificado` do usuário passa a `true`. " +
                    "O ID do usuário é extraído do JWT — não precisa ser informado no body. " +
                    "Requer autenticação.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "E-mail verificado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Código ausente ou com formato inválido (diferente de 6 dígitos)",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido — ou código incorreto — " +
                    "ou código expirado (use /reenviar para obter um novo)",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Nenhum código de verificação pendente para este usuário",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/verificar")
    public ResponseEntity<Void> verificar(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Código de 6 dígitos recebido por e-mail",
                    required = true
            )
            @RequestBody @Valid VerificacaoEmailRequestDTO request
    ) {
        Long idUsuario = extrairIdDoToken(authHeader);
        verificacaoEmailService.verificar(idUsuario, request.getCodigo());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reenviar código de verificação",
            description = "Invalida todos os códigos anteriores do usuário e envia um novo código de 6 dígitos " +
                    "para o e-mail cadastrado. O envio é **assíncrono** — a resposta `202 Accepted` confirma " +
                    "que a solicitação foi recebida, não que o e-mail já foi entregue. " +
                    "Use este endpoint quando o código expirou ou não chegou. " +
                    "O ID do usuário é extraído do JWT. " +
                    "Requer autenticação.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Solicitação de reenvio aceita — e-mail sendo processado em background"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/reenviar")
    public ResponseEntity<Void> reenviar(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader
    ) {
        Long idUsuario = extrairIdDoToken(authHeader);
        verificacaoEmailService.reenviarCodigo(idUsuario);
        return ResponseEntity.accepted().build();
    }

    private Long extrairIdDoToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtService.validarTokenERetornarId(token);
    }
}