package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.annotations.Permissao;
import br.com.fiap.on.smarthas.model.entities.dto.*;
import br.com.fiap.on.smarthas.model.entities.orm.RefreshTokenORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import br.com.fiap.on.smarthas.services.JwtService;
import br.com.fiap.on.smarthas.services.RefreshTokenService;
import br.com.fiap.on.smarthas.services.UsuarioService;
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
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/cadastrar")
    @Permissao(rota = "cadastrarusuario")
    public ResponseEntity<UsuarioPerfilDTO> cadastrarUsuario(@Valid @RequestBody UsuarioPerfilDTO usuarioPerfilRecebido) {
        UsuarioPerfilDTO usuarioCadastrado = usuarioService.novoUsuario(usuarioPerfilRecebido);

        return new ResponseEntity<>(usuarioCadastrado, HttpStatus.OK);
    }

    @PostMapping("/registrar")
    public ResponseEntity<UsuarioPerfilDTO> autoRegistroUsuario(@Valid @RequestBody UsuarioPerfilDTO usuarioPerfilRecebido) {
        UsuarioPerfilDTO usuarioCadastrado = usuarioService.novoUsuario(usuarioPerfilRecebido);

        return new ResponseEntity<>(usuarioCadastrado, HttpStatus.OK);
    }

    @GetMapping("/listar")
    @Permissao(rota = "listartodosusuarios")
    public ResponseEntity<List<UsuarioDTO>> listarTodosUsuarios(Pageable pageable) {
        List<UsuarioDTO> usuarios = usuarioService.listarTodos(pageable);

        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    @GetMapping("/listar-especifico/{id}")
    @Permissao(rota = "listarusuarioespecifico")
    public ResponseEntity<UsuarioPerfilDTO> listarUsuarioEspecifico(@PathVariable Long id) {
        UsuarioPerfilDTO usuario = usuarioService.listarEspecifico(id);

        return new ResponseEntity<>(usuario, HttpStatus.OK);
    }

    @GetMapping("/clonar/{id}")
    @Permissao(rota = "clonarusuario")
    public ResponseEntity<UsuarioPerfilDTO> clonarUsuario(@PathVariable Long id) {
        UsuarioPerfilDTO usuarioClonado = usuarioService.clonar(id);

        return new ResponseEntity<>(usuarioClonado, HttpStatus.OK);
    }

    @DeleteMapping("/deletar/{id}")
    @Permissao(rota = "deletarusuario")
    public ResponseEntity<Boolean> deletarUsuario(@PathVariable Long id) {
        usuarioService.deletar(id);

        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PutMapping("/editar")
    @Permissao(rota = "editarusuario")
    public ResponseEntity<UsuarioPerfilDTO> editarUsuario(@Valid @RequestBody UsuarioPerfilDTO modeloCadastroUsuarioPerfil) {
        UsuarioPerfilDTO usuarioEditado = usuarioService.editar(modeloCadastroUsuarioPerfil);

        return new ResponseEntity<>(usuarioEditado, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        UsuarioORM usuario = usuarioService.autenticar(loginRequest);

        String accessToken = jwtService.gerarToken(usuario);
        RefreshTokenORM refreshToken = refreshTokenService.gerar(usuario);

        LoginResponseDTO response = new LoginResponseDTO(
                usuario.getId(),
                usuario.getNomeAmigavel(),
                usuario.getCpf(),
                usuario.getEmail(),
                accessToken,
                refreshToken.getToken(),
                jwtService.getExpiracaoSegundos()
        );

        log.debug("Login realizado para usuário id={}", usuario.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        // Valida o refresh token recebido (lança exceção se inválido/expirado)
        RefreshTokenORM refreshTokenValido = refreshTokenService.validar(request.getRefreshToken());

        UsuarioORM usuario = refreshTokenValido.getUsuario();

        // Revoga o token usado e gera um novo (rotação)
        refreshTokenService.revogarUm(refreshTokenValido);
        RefreshTokenORM novoRefreshToken = refreshTokenService.gerar(usuario);

        String novoAccessToken = jwtService.gerarToken(usuario);

        RefreshTokenResponseDTO response = new RefreshTokenResponseDTO(
                novoAccessToken,
                novoRefreshToken.getToken(),
                jwtService.getExpiracaoSegundos()
        );

        log.debug("Tokens renovados para usuário id={}", usuario.getId());
        return ResponseEntity.ok(response);
    }

    // Logout — revoga todos os refresh tokens do usuário autenticado
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long idUsuario = jwtService.validarTokenERetornarId(token);

        UsuarioORM usuario = usuarioService.buscarOrmPorId(idUsuario);
        refreshTokenService.revogarTodos(usuario);

        log.debug("Logout realizado para usuário id={}", idUsuario);
        return ResponseEntity.noContent().build();
    }
}
