package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.services.AulaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aulas")
@RequiredArgsConstructor
@Tag(name = "Aulas", description = "Registro de aulas assistidas pelos alunos")
public class AulaController {

    private final AulaService aulaService;

    @Operation(
            summary = "Registrar aula como assistida",
            description = "Marca uma aula como assistida pelo aluno. A operação é idempotente — " +
                    "chamar mais de uma vez para a mesma aula não cria registros duplicados. " +
                    "Quando todas as aulas de um módulo forem assistidas, o progresso do módulo " +
                    "é atualizado automaticamente pelo service. " +
                    "Requer autenticação.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aula registrada como assistida"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Aula ou aluno não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/assistida/{idAula}/aluno/{idAluno}")
    public ResponseEntity<?> aulaAssistida(
            @Parameter(description = "ID da aula assistida", required = true, example = "10")
            @PathVariable Long idAula,
            @Parameter(description = "ID do aluno", required = true, example = "42")
            @PathVariable Long idAluno
    ) {
        aulaService.aulaAssistida(idAula, idAluno);
        return ResponseEntity.ok().build();
    }
}