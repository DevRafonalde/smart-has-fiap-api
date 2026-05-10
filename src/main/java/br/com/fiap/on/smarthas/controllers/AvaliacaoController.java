package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.annotations.Permissao;
import br.com.fiap.on.smarthas.model.entities.dto.AvaliacaoCompletaDTO;
import br.com.fiap.on.smarthas.model.entities.dto.TentativaAvaliacaoRequestDTO;
import br.com.fiap.on.smarthas.model.entities.dto.TentativaAvaliacaoResponseDTO;
import br.com.fiap.on.smarthas.services.AvaliacaoService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
@Tag(name = "Avaliações", description = "Submissão de tentativas e consulta de avaliações por módulo")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @Operation(
            summary = "Submeter tentativa de avaliação",
            description = "Registra a nota do aluno em uma tentativa de avaliação e aplica as regras de negócio: " +
                    "aprovação com nota ≥ `pontuacaoMinima` (padrão 70), limite de `maxTentativas` (padrão 3), " +
                    "emissão de medalha em caso de aprovação (RN04), e marcação de tutoria obrigatória " +
                    "quando todas as tentativas são esgotadas sem aprovação (RN03). " +
                    "Requer autenticação e permissão `submeteravaliacao`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tentativa registrada. Verifique `aprovado`, " +
                    "`tentativasRestantes` e `tutoriaObrigatoria` no response para orientar o fluxo do app."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no body",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `submeteravaliacao` — ou aluno já aprovado " +
                    "nesta avaliação — ou tentativas esgotadas sem tutoria solicitada",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Avaliação ou usuário não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/submeter")
    @Permissao(rota = "submeteravaliacao")
    public ResponseEntity<TentativaAvaliacaoResponseDTO> submeterAvaliacao(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nota e IDs da avaliação e do aluno",
                    required = true
            )
            @RequestBody TentativaAvaliacaoRequestDTO tentativaAvaliacaoRequestDTO
    ) {
        return ResponseEntity.ok(avaliacaoService.submeterAvaliacao(tentativaAvaliacaoRequestDTO));
    }

    @Operation(
            summary = "Buscar avaliação de um módulo (visão do aluno)",
            description = "Retorna a avaliação vinculada ao módulo com suas questões e opções de resposta. " +
                    "Requer autenticação e permissão `buscaravaliacao`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `buscaravaliacao`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Módulo sem avaliação vinculada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/modulo/{idModulo}/aluno/{idAluno}")
    @Permissao(rota = "buscaravaliacao")
    public ResponseEntity<AvaliacaoCompletaDTO> buscarAvaliacao(
            @Parameter(description = "ID do módulo", required = true, example = "5")
            @PathVariable Long idModulo,
            @Parameter(description = "ID do aluno — usado para verificar se o aluno já assistiu todas as aulas e pode visualizar a avaliação",
                    required = true, example = "42")
            @PathVariable Long idAluno
    ) {
        return ResponseEntity.ok(avaliacaoService.buscarAvaliacao(idModulo, idAluno, false));
    }

    @Operation(
            summary = "Buscar avaliação de um módulo (visão admin)",
            description = "Retorna a avaliação completa com gabarito — o campo `correta` das opções " +
                    "é retornado com o valor real. Exclusivo para administradores. " +
                    "Requer autenticação e permissão `buscaravaliacaoadmin`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação com gabarito retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `buscaravaliacaoadmin`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Módulo sem avaliação vinculada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/modulo/{idModulo}/")
    @Permissao(rota = "buscaravaliacaoadmin")
    public ResponseEntity<AvaliacaoCompletaDTO> buscarAvaliacaoAdmin(
            @Parameter(description = "ID do módulo", required = true, example = "5")
            @PathVariable Long idModulo
    ) {
        return ResponseEntity.ok(avaliacaoService.buscarAvaliacao(idModulo, null, true));
    }
}