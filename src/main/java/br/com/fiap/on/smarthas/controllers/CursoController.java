package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.annotations.Permissao;
import br.com.fiap.on.smarthas.model.entities.dto.CursoDTO;
import br.com.fiap.on.smarthas.model.entities.dto.MatriculaDTO;
import br.com.fiap.on.smarthas.model.entities.dto.ProgressoDTO;
import br.com.fiap.on.smarthas.services.CursoService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cursos")
@RequiredArgsConstructor
@Tag(name = "Cursos", description = "Gerenciamento de cursos, matrículas e progresso dos alunos")
public class CursoController {

    private final CursoService cursoService;

    // ── Listagem e leitura (públicos) ─────────────────────────────────────────

    @Operation(
            summary = "Listar todos os cursos",
            description = "Retorna todos os cursos ativos disponíveis na plataforma. Não requer autenticação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de cursos retornada com sucesso")
    })
    @GetMapping("/")
    public ResponseEntity<List<CursoDTO>> buscarTodos() {
        return ResponseEntity.ok(cursoService.buscarTodos());
    }

    @Operation(
            summary = "Buscar curso por ID",
            description = "Retorna os dados de um curso específico. Não requer autenticação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Curso encontrado"),
            @ApiResponse(responseCode = "404", description = "Curso não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CursoDTO> buscarPorId(
            @Parameter(description = "ID do curso", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(cursoService.buscarPorId(id));
    }

    // ── Matrícula ─────────────────────────────────────────────────────────────

    @Operation(
            summary = "Matricular aluno em um curso",
            description = "Cria uma matrícula com status PENDENTE para o aluno no curso informado. " +
                    "Requer autenticação e permissão `matricular`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matrícula criada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `matricular`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Aluno ou curso não encontrado",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Aluno já matriculado neste curso",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/matricular/{idAluno}/curso/{idCurso}")
    @Permissao(rota = "matricular")
    public ResponseEntity<MatriculaDTO> matricular(
            @Parameter(description = "ID do aluno", required = true, example = "42")
            @PathVariable Long idAluno,
            @Parameter(description = "ID do curso", required = true, example = "1")
            @PathVariable Long idCurso
    ) {
        return ResponseEntity.ok(cursoService.matricularAluno(idAluno, idCurso));
    }

    @Operation(
            summary = "Cancelar matrícula de um aluno",
            description = "Altera o status da matrícula para CANCELADA. " +
                    "Requer autenticação e permissão `cancelarmatricula`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matrícula cancelada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `cancelarmatricula`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Matrícula não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/cancelar-matricula/{idAluno}/curso/{idCurso}")
    @Permissao(rota = "cancelarmatricula")
    public ResponseEntity<MatriculaDTO> cancelarMatricula(
            @Parameter(description = "ID do aluno", required = true, example = "42")
            @PathVariable Long idAluno,
            @Parameter(description = "ID do curso", required = true, example = "1")
            @PathVariable Long idCurso
    ) {
        return ResponseEntity.ok(cursoService.cancelarMatricula(idAluno, idCurso));
    }

    // ── Progresso ─────────────────────────────────────────────────────────────

    @Operation(
            summary = "Consultar progresso do aluno em um curso",
            description = "Retorna o percentual de conclusão, módulos concluídos e status da matrícula. " +
                    "Requer autenticação e permissão `consultarprogresso`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progresso retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `consultarprogresso`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Matrícula não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/meu-progresso/{idAluno}/curso/{idCurso}")
    @Permissao(rota = "consultarprogresso")
    public ResponseEntity<ProgressoDTO> consultarProgresso(
            @Parameter(description = "ID do aluno", required = true, example = "42")
            @PathVariable Long idAluno,
            @Parameter(description = "ID do curso", required = true, example = "1")
            @PathVariable Long idCurso
    ) {
        return ResponseEntity.ok(cursoService.consultarProgresso(idAluno, idCurso));
    }

    // ── Administração ─────────────────────────────────────────────────────────

    @Operation(
            summary = "Criar novo curso",
            description = "Cadastra um novo curso na plataforma. " +
                    "Requer autenticação e permissão `criarcurso`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Curso criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no body",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `criarcurso`",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/")
    @Permissao(rota = "criarcurso")
    public ResponseEntity<CursoDTO> criarCurso(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do novo curso",
                    required = true
            )
            @Valid @RequestBody CursoDTO cursoDTO
    ) {
        return ResponseEntity.ok(cursoService.criarCurso(cursoDTO));
    }

    @Operation(
            summary = "Editar curso existente",
            description = "Atualiza os dados de um curso. " +
                    "Requer autenticação e permissão `editarcurso`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Curso atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no body",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `editarcurso`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Curso não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/{id}")
    @Permissao(rota = "editarcurso")
    public ResponseEntity<CursoDTO> editarCurso(
            @Parameter(description = "ID do curso a editar", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do curso",
                    required = true
            )
            @Valid @RequestBody CursoDTO cursoDTO
    ) {
        return ResponseEntity.ok(cursoService.editarCurso(id, cursoDTO));
    }
}