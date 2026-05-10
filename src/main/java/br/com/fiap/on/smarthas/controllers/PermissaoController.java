package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.annotations.Permissao;
import br.com.fiap.on.smarthas.model.entities.dto.PermissaoDTO;
import br.com.fiap.on.smarthas.services.PermissaoService;
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
@RequestMapping("/auth/permissoes")
@RequiredArgsConstructor
@Tag(name = "Permissões", description = "Gerenciamento do catálogo de permissões da plataforma. " +
        "Na maioria dos casos as permissões são criadas automaticamente pelo seeder — " +
        "use estes endpoints para gerenciamento manual.")
public class PermissaoController {

    private final PermissaoService permissaoService;

    @Operation(
            summary = "Cadastrar nova permissão",
            description = "Cadastra uma nova permissão manualmente no catálogo. " +
                    "O campo `nome` deve ser idêntico ao valor de `rota` na annotation `@Permissao` " +
                    "do endpoint que ela protege. " +
                    "Requer autenticação e permissão `cadastrarpermissao`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissão criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos — `nome` ou `descricao` ausentes",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `cadastrarpermissao`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Já existe uma permissão com esse `nome`",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/cadastrar")
    @Permissao(rota = "cadastrarpermissao")
    public ResponseEntity<PermissaoDTO> cadastrarPermissao(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da permissão. O `nome` deve corresponder exatamente ao valor de `rota` " +
                            "na `@Permissao` do endpoint que será protegido.",
                    required = true
            )
            @RequestBody @Valid PermissaoDTO permissaoRecebida
    ) {
        return new ResponseEntity<>(permissaoService.novaPermissao(permissaoRecebida), HttpStatus.OK);
    }

    @Operation(
            summary = "Listar todas as permissões",
            description = "Retorna o catálogo completo de permissões com paginação. " +
                    "Suporta os parâmetros Spring Pageable: `page`, `size` e `sort`. " +
                    "Requer autenticação e permissão `listartodaspermissoes`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de permissões retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `listartodaspermissoes`",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/listar")
    @Permissao(rota = "listartodaspermissoes")
    public ResponseEntity<List<PermissaoDTO>> listarTodasPermissoes(Pageable pageable) {
        return new ResponseEntity<>(permissaoService.listarTodas(pageable), HttpStatus.OK);
    }

    @Operation(
            summary = "Buscar permissão por ID",
            description = "Retorna os dados de uma permissão específica. " +
                    "Requer autenticação e permissão `listarpermissaoespecifica`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissão encontrada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `listarpermissaoespecifica`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Permissão não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/listar-especifico/{id}")
    @Permissao(rota = "listarpermissaoespecifica")
    public ResponseEntity<PermissaoDTO> listarPermissaoEspecifica(
            @Parameter(description = "ID da permissão", required = true, example = "3")
            @PathVariable Long id
    ) {
        return new ResponseEntity<>(permissaoService.listarPorId(id), HttpStatus.OK);
    }

    @Operation(
            summary = "Editar permissão",
            description = "Atualiza os dados de uma permissão existente. " +
                    "Atenção: alterar o `nome` de uma permissão pode quebrar o controle de acesso " +
                    "caso o valor não corresponda mais ao `rota` da `@Permissao` no controller. " +
                    "Requer autenticação e permissão `editarpermissao`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissão atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no body",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `editarpermissao`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Permissão não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/editar")
    @Permissao(rota = "editarpermissao")
    public ResponseEntity<PermissaoDTO> editarPermissao(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados da permissão. O `id` é obrigatório.",
                    required = true
            )
            @RequestBody @Valid PermissaoDTO permissaoDTO
    ) {
        return new ResponseEntity<>(permissaoService.editar(permissaoDTO), HttpStatus.OK);
    }

    @Operation(
            summary = "Deletar permissão",
            description = "Remove a permissão do catálogo e todos os seus vínculos com perfis. " +
                    "Atenção: deletar uma permissão que ainda está em uso por um perfil tornará " +
                    "o endpoint correspondente inacessível para usuários desse perfil. " +
                    "Requer autenticação e permissão `deletarpermissao`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissão removida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `deletarpermissao`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Permissão não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/deletar/{id}")
    @Permissao(rota = "deletarpermissao")
    public ResponseEntity<Boolean> deletarPermissao(
            @Parameter(description = "ID da permissão a remover", required = true, example = "3")
            @PathVariable Long id
    ) {
        permissaoService.deletar(id);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}