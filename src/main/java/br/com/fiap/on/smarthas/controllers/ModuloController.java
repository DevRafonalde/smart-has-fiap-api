package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.annotations.Permissao;
import br.com.fiap.on.smarthas.model.entities.dto.ModuloCompletoDTO;
import br.com.fiap.on.smarthas.model.entities.dto.ModuloDTO;
import br.com.fiap.on.smarthas.services.ModuloService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/modulos")
@RequiredArgsConstructor
@Tag(name = "Módulos", description = "Listagem de módulos por curso e detalhamento com aulas e avaliação")
public class ModuloController {

    private final ModuloService moduloService;

    @Operation(
            summary = "Listar módulos de um curso",
            description = "Retorna todos os módulos ativos de um curso, ordenados por `ordemConteudo`. " +
                    "Não requer autenticação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Módulos retornados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Curso não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/curso/{id}")
    public ResponseEntity<List<ModuloDTO>> buscarModulosPorCurso(
            @Parameter(description = "ID do curso", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok().body(moduloService.buscarModulosPorCurso(id));
    }

    @Operation(
            summary = "Buscar módulo por ID com aulas e avaliação",
            description = "Retorna o módulo completo incluindo suas aulas ordenadas e a avaliação vinculada (se existir). " +
                    "Requer autenticação e permissão `buscarmoduloporid`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Módulo encontrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `buscarmoduloporid`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Módulo não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    @Permissao(rota = "buscarmoduloporid")
    public ResponseEntity<ModuloCompletoDTO> buscarModuloPorId(
            @Parameter(description = "ID do módulo", required = true, example = "5")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok().body(moduloService.buscarModuloPorId(id));
    }
}