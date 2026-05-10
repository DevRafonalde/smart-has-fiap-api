package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.annotations.Permissao;
import br.com.fiap.on.smarthas.model.entities.dto.PerfilDTO;
import br.com.fiap.on.smarthas.model.entities.dto.PerfilPermissaoDTO;
import br.com.fiap.on.smarthas.model.entities.dto.PerfilUsuarioDTO;
import br.com.fiap.on.smarthas.services.PerfilService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/perfis")
@RequiredArgsConstructor
@Tag(name = "Perfis", description = "Gerenciamento de perfis de acesso e vínculo com permissões e usuários")
public class PerfilController {

    private final PerfilService perfilService;

    @Operation(
            summary = "Cadastrar novo perfil",
            description = "Cria um novo perfil e vincula as permissões informadas. " +
                    "Requer autenticação e permissão `cadastrarperfil`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no body",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `cadastrarperfil`",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/cadastrar")
    @Permissao(rota = "cadastrarperfil")
    public ResponseEntity<PerfilPermissaoDTO> cadastrarPerfil(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do perfil e lista de permissões a vincular",
                    required = true
            )
            @RequestBody PerfilPermissaoDTO perfilPermissaoDTO
    ) {
        return ResponseEntity.ok(perfilService.novoPerfil(perfilPermissaoDTO));
    }

    @Operation(
            summary = "Listar todos os perfis",
            description = "Retorna todos os perfis ativos com paginação. " +
                    "Suporta os parâmetros Spring Pageable: `page`, `size` e `sort`. " +
                    "Requer autenticação e permissão `listartodosperfis`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de perfis retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `listartodosperfis`",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/listar")
    @Permissao(rota = "listartodosperfis")
    public ResponseEntity<List<PerfilDTO>> listarTodosPerfis(Pageable pageable) {
        return ResponseEntity.ok(perfilService.listarTodos(pageable));
    }

    @Operation(
            summary = "Listar usuários vinculados a um perfil",
            description = "Retorna o perfil com a lista de usuários ativos que possuem esse perfil atribuído. " +
                    "Requer autenticação e permissão `listarusuariosvinculados`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil e usuários retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `listarusuariosvinculados`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/listar-usuarios-vinculados/{id}")
    @Permissao(rota = "listarusuariosvinculados")
    public ResponseEntity<PerfilUsuarioDTO> listarUsuariosVinculados(
            @Parameter(description = "ID do perfil", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(perfilService.listarUsuariosVinculados(id));
    }

    @Operation(
            summary = "Buscar perfil por ID com permissões",
            description = "Retorna os dados completos de um perfil incluindo suas permissões vinculadas. " +
                    "Requer autenticação e permissão `listarperfilespecifico`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil encontrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `listarperfilespecifico`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/listar-especifico/{id}")
    @Permissao(rota = "listarperfilespecifico")
    public ResponseEntity<PerfilPermissaoDTO> listarPerfilEspecifico(
            @Parameter(description = "ID do perfil", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(perfilService.listarEspecifico(id));
    }

    @Operation(
            summary = "Clonar configuração de um perfil",
            description = "Retorna as permissões de um perfil existente com `perfil: null`, " +
                    "útil para criar um novo perfil com as mesmas permissões sem precisar buscá-las manualmente. " +
                    "Requer autenticação e permissão `clonarperfil`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuração clonada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `clonarperfil`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/clonar/{id}")
    @Permissao(rota = "clonarperfil")
    public ResponseEntity<PerfilPermissaoDTO> clonarPerfil(
            @Parameter(description = "ID do perfil a clonar", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(perfilService.clonar(id));
    }

    @Operation(
            summary = "Editar perfil",
            description = "Atualiza os dados do perfil e redefine completamente suas permissões vinculadas — " +
                    "as permissões anteriores são removidas e substituídas pelas informadas no body. " +
                    "Requer autenticação e permissão `editarperfil`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no body",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `editarperfil`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/editar")
    @Permissao(rota = "editarperfil")
    public ResponseEntity<PerfilPermissaoDTO> editarPerfil(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do perfil e nova lista de permissões",
                    required = true
            )
            @RequestBody @Valid PerfilPermissaoDTO perfilPermissaoRecebido
    ) {
        return ResponseEntity.ok(perfilService.editar(perfilPermissaoRecebido));
    }

    @Operation(
            summary = "Deletar perfil",
            description = "Remove o perfil, seus vínculos com permissões e seus vínculos com usuários. " +
                    "Requer autenticação e permissão `deletarperfil`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `deletarperfil`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/deletar/{id}")
    @Permissao(rota = "deletarperfil")
    public ResponseEntity<Boolean> deletarPerfil(
            @Parameter(description = "ID do perfil a remover", required = true, example = "1")
            @PathVariable Long id
    ) {
        perfilService.deletar(id);
        return ResponseEntity.ok(true);
    }
}