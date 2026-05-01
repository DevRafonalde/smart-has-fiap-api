package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.model.entities.orm.RefreshTokenORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import br.com.fiap.on.smarthas.model.repositories.RefreshTokenRepository;
import br.com.fiap.on.smarthas.exceptions.AcessoNaoAutorizadoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.refresh-expiration}")
    private long refreshExpiracaoSegundos;

    @Transactional
    public RefreshTokenORM gerar(UsuarioORM usuario) {
        revogarTodos(usuario);

        String tokenUuid = UUID.randomUUID().toString();
        LocalDateTime expiracao = LocalDateTime.now().plusSeconds(refreshExpiracaoSegundos);

        RefreshTokenORM novoToken = new RefreshTokenORM(usuario, tokenUuid, expiracao);
        RefreshTokenORM tokenSalvo = refreshTokenRepository.save(novoToken);

        log.debug("Refresh token gerado para usuário id={}", usuario.getId());
        return tokenSalvo;
    }

    public RefreshTokenORM validar(String token) {
        RefreshTokenORM refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AcessoNaoAutorizadoException("Refresh token não encontrado"));

        if (refreshToken.estaExpirado()) {
            revogarUm(refreshToken);
            throw new AcessoNaoAutorizadoException("Refresh token expirado. Faça login novamente");
        }

        if (refreshToken.getRevogado()) {
            log.warn("Tentativa de uso de refresh token já revogado. Usuário id={}",
                    refreshToken.getUsuario().getId());
            throw new AcessoNaoAutorizadoException("Refresh token inválido");
        }

        return refreshToken;
    }

    @Transactional
    public void revogarUm(RefreshTokenORM refreshToken) {
        refreshToken.setRevogado(true);
        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token revogado para usuário id={}", refreshToken.getUsuario().getId());
    }

    @Transactional
    public void revogarTodos(UsuarioORM usuario) {
        List<RefreshTokenORM> tokens = refreshTokenRepository.findByUsuario(usuario);
        tokens.forEach(t -> t.setRevogado(true));
        refreshTokenRepository.saveAll(tokens);

        if (!tokens.isEmpty()) {
            log.debug("Revogados {} refresh token(s) para usuário id={}", tokens.size(), usuario.getId());
        }
    }
}