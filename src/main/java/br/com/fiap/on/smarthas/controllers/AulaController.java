package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.annotations.Permissao;
import br.com.fiap.on.smarthas.model.entities.dto.AulaDTO;
import br.com.fiap.on.smarthas.services.AulaService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(
            summary = "Cadastrar aula (admin)",
            description = "Cadastra uma nova aula" +
                    "Requer autenticação e permissão `cadastraraula`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aula cadastrada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `cadastraraula`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Módulo não encontrado",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Link de aula já cadastrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/cadastrar")
    @Permissao(rota = "cadastraraula")
    public ResponseEntity<AulaDTO> cadastrarAula(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da aula",
                    required = true
            )
            @Valid @RequestBody AulaDTO aulaRecebida
    ) {
        AulaDTO aulaCadastrado = aulaService.novaAula(aulaRecebida);
        return new ResponseEntity<>(aulaCadastrado, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar todas as aulas",
            description = "Retorna aulas paginados. Suporta parâmetros Spring Pageable: " +
                    "`page`, `size` e `sort`. " +
                    "Requer autenticação e permissão `listartodasaulas`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `listartodasaulas`",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/listar")
    @Permissao(rota = "listartodasauals")
    public ResponseEntity<List<AulaDTO>> listarTodasAulas(Pageable pageable) {
        List<AulaDTO> aulas = aulaService.listarTodas(pageable);
        return new ResponseEntity<>(aulas, HttpStatus.OK);
    }

    @Operation(
            summary = "Buscar aula por ID",
            description = "Retorna uma aula específica pelo ID dela" +
                    "Requer autenticação e permissão `buscaraulaporid`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `buscaraulaporid`",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/listar/{id}")
    @Permissao(rota = "buscaraulaporid")
    public ResponseEntity<AulaDTO> buscarAulaPorId(Long id) {
        AulaDTO aula = aulaService.buscarPorID(id);
        return new ResponseEntity<>(aula, HttpStatus.OK);
    }

    @Operation(
            summary = "Editar aula",
            description = "Atualiza os dados da aula" +
                    "Requer autenticação e permissão `editaraula`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aula atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `editaraula`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Aula não encontrada",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Link de aula já cadastrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/editar")
    @Permissao(rota = "editaraula")
    public ResponseEntity<AulaDTO> editarAula(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados da aula",
                    required = true
            )
            @Valid @RequestBody AulaDTO aulaRecebida
    ) {
        AulaDTO aulaEditada = aulaService.editar(aulaRecebida);
        return new ResponseEntity<>(aulaEditada, HttpStatus.OK);
    }

    @Operation(
            summary = "Deletar aula",
            description = "Remove a aula" +
                    "Requer autenticação e permissão `deletaraula`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aula removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `deletaraula`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Aula não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/deletar/{id}")
    @Permissao(rota = "deletaraula")
    public ResponseEntity<Boolean> deletarAula(
            @Parameter(description = "ID da aula a remover", required = true, example = "42")
            @PathVariable Long id
    ) {
        aulaService.deletar(id);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}