package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.annotations.Permissao;
import br.com.fiap.on.smarthas.model.entities.dto.*;
import br.com.fiap.on.smarthas.model.entities.orm.RefreshTokenORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import br.com.fiap.on.smarthas.services.JwtService;
import br.com.fiap.on.smarthas.services.RefreshTokenService;
import br.com.fiap.on.smarthas.services.UsuarioService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/auth/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Autenticação, registro e gerenciamento de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    // ── Rotas públicas ────────────────────────────────────────────────────────

    @Operation(
            summary = "Login",
            description = "Autentica o usuário com CPF/e-mail e senha. " +
                    "Retorna um access token JWT (15 min) e um refresh token UUID (7 dias)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciais de acesso (CPF ou e-mail + senha)",
                    required = true
            )
            @Valid @RequestBody LoginRequestDTO loginRequest
    ) {
        UsuarioORM usuario = usuarioService.autenticar(loginRequest);
        String accessToken = jwtService.gerarToken(usuario);
        RefreshTokenORM refreshToken = refreshTokenService.gerar(usuario);

        LoginResponseDTO response = new LoginResponseDTO(
                usuario.getId(), usuario.getNomeAmigavel(), usuario.getCpf(),
                usuario.getEmail(), accessToken, refreshToken.getToken(),
                jwtService.getExpiracaoSegundos()
        );

        log.debug("Login realizado para usuário id={}", usuario.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Registro público de usuário",
            description = "Cadastro realizado pelo próprio cidadão. O CPF é validado pelo algoritmo oficial " +
                    "dos dígitos verificadores. O perfil padrão é atribuído automaticamente pelo sistema. " +
                    "Não requer autenticação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "CPF inválido ou campos obrigatórios ausentes",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "CPF ou e-mail já cadastrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/registrar")
    public ResponseEntity<UsuarioPerfilDTO> autoRegistroUsuario(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do usuário. `senhaUser` deve ter mínimo 8 caracteres.",
                    required = true
            )
            @Valid @RequestBody UsuarioPerfilDTO usuarioPerfilRecebido
    ) {
        UsuarioPerfilDTO usuarioCadastrado = usuarioService.novoUsuario(usuarioPerfilRecebido);
        return new ResponseEntity<>(usuarioCadastrado, HttpStatus.OK);
    }

    @Operation(
            summary = "Renovar tokens (refresh)",
            description = "Recebe o refresh token e retorna um novo par de tokens. " +
                    "O refresh token enviado é **revogado imediatamente** (rotação). " +
                    "Se o refresh token estiver expirado, retorna 401 e o usuário deve fazer login novamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campo `refreshToken` ausente",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Refresh token não encontrado, expirado ou já revogado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDTO> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token atual",
                    required = true
            )
            @Valid @RequestBody RefreshTokenRequestDTO request
    ) {
        RefreshTokenORM refreshTokenValido = refreshTokenService.validar(request.getRefreshToken());
        UsuarioORM usuario = refreshTokenValido.getUsuario();

        refreshTokenService.revogarUm(refreshTokenValido);
        RefreshTokenORM novoRefreshToken = refreshTokenService.gerar(usuario);
        String novoAccessToken = jwtService.gerarToken(usuario);

        RefreshTokenResponseDTO response = new RefreshTokenResponseDTO(
                novoAccessToken, novoRefreshToken.getToken(), jwtService.getExpiracaoSegundos()
        );

        log.debug("Tokens renovados para usuário id={}", usuario.getId());
        return ResponseEntity.ok(response);
    }

    // ── Rotas autenticadas ────────────────────────────────────────────────────

    @Operation(
            summary = "Logout",
            description = "Revoga todos os refresh tokens do usuário autenticado. " +
                    "O access token expira naturalmente após seu tempo de vida. " +
                    "Requer autenticação.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        Long idUsuario = jwtService.validarTokenERetornarId(token);

        UsuarioORM usuario = usuarioService.buscarOrmPorId(idUsuario);
        refreshTokenService.revogarTodos(usuario);

        log.debug("Logout realizado para usuário id={}", idUsuario);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Cadastrar usuário (admin)",
            description = "Cadastra um novo usuário já vinculando perfis específicos. " +
                    "Uso exclusivo por administradores. O CPF é validado pelo algoritmo oficial. " +
                    "Requer autenticação e permissão `cadastrarusuario`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "CPF inválido ou campos obrigatórios ausentes",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `cadastrarusuario`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "CPF ou e-mail já cadastrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/cadastrar")
    @Permissao(rota = "cadastrarusuario")
    public ResponseEntity<UsuarioPerfilDTO> cadastrarUsuario(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do usuário e lista de perfis a vincular",
                    required = true
            )
            @Valid @RequestBody UsuarioPerfilDTO usuarioPerfilRecebido
    ) {
        UsuarioPerfilDTO usuarioCadastrado = usuarioService.novoUsuario(usuarioPerfilRecebido);
        return new ResponseEntity<>(usuarioCadastrado, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna usuários ativos paginados. Suporta parâmetros Spring Pageable: " +
                    "`page`, `size` e `sort`. " +
                    "Requer autenticação e permissão `listartodosusuarios`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `listartodosusuarios`",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/listar")
    @Permissao(rota = "listartodosusuarios")
    public ResponseEntity<List<UsuarioDTO>> listarTodosUsuarios(Pageable pageable) {
        List<UsuarioDTO> usuarios = usuarioService.listarTodos(pageable);
        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    @Operation(
            summary = "Buscar usuário por ID com perfis",
            description = "Retorna os dados completos de um usuário e seus perfis vinculados. " +
                    "Requer autenticação e permissão `listarusuarioespecifico`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `listarusuarioespecifico`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/listar-especifico/{id}")
    @Permissao(rota = "listarusuarioespecifico")
    public ResponseEntity<UsuarioPerfilDTO> listarUsuarioEspecifico(
            @Parameter(description = "ID do usuário", required = true, example = "42")
            @PathVariable Long id
    ) {
        UsuarioPerfilDTO usuario = usuarioService.listarEspecifico(id);
        return new ResponseEntity<>(usuario, HttpStatus.OK);
    }

    @Operation(
            summary = "Clonar configuração de um usuário",
            description = "Retorna os perfis de um usuário existente com `usuario: null`, " +
                    "útil para criar um novo usuário com os mesmos vínculos de perfil. " +
                    "Requer autenticação e permissão `clonarusuario`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuração clonada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `clonarusuario`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/clonar/{id}")
    @Permissao(rota = "clonarusuario")
    public ResponseEntity<UsuarioPerfilDTO> clonarUsuario(
            @Parameter(description = "ID do usuário a clonar", required = true, example = "42")
            @PathVariable Long id
    ) {
        UsuarioPerfilDTO usuarioClonado = usuarioService.clonar(id);
        return new ResponseEntity<>(usuarioClonado, HttpStatus.OK);
    }

    @Operation(
            summary = "Editar usuário",
            description = "Atualiza os dados do usuário e redefine seus perfis vinculados. " +
                    "A senha **não** é alterada por este endpoint. " +
                    "Requer autenticação e permissão `editarusuario`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "CPF inválido ou campos obrigatórios ausentes",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `editarusuario`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Novo e-mail já em uso por outro usuário",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/editar")
    @Permissao(rota = "editarusuario")
    public ResponseEntity<UsuarioPerfilDTO> editarUsuario(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do usuário e novos perfis",
                    required = true
            )
            @Valid @RequestBody UsuarioPerfilDTO modeloCadastroUsuarioPerfil
    ) {
        UsuarioPerfilDTO usuarioEditado = usuarioService.editar(modeloCadastroUsuarioPerfil);
        return new ResponseEntity<>(usuarioEditado, HttpStatus.OK);
    }

    @Operation(
            summary = "Deletar usuário",
            description = "Remove o usuário e todos os seus vínculos de perfil. " +
                    "Requer autenticação e permissão `deletarusuario`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Sem permissão `deletarusuario`",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/deletar/{id}")
    @Permissao(rota = "deletarusuario")
    public ResponseEntity<Boolean> deletarUsuario(
            @Parameter(description = "ID do usuário a remover", required = true, example = "42")
            @PathVariable Long id
    ) {
        usuarioService.deletar(id);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}