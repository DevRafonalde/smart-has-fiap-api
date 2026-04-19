package br.com.fiap.on.smarthas.auth.internal.services;

import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.UsuarioORM;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Slf4j
@Service
public class JwtService {
    private final Key chave;

    @Getter
    private final long expiracaoSegundos;

    @SneakyThrows
    public JwtService(
            @Value("${security.jwt.secret}") String chaveString,
            @Value("${security.jwt.expiration}") long expiracaoSegundos
    ) {
        this.chave = Keys.hmacShaKeyFor(chaveString.getBytes());
        this.expiracaoSegundos = expiracaoSegundos;
    }

    public String gerarToken(UsuarioORM usuario) {
        log.debug("Gerando access token para usuário id={}", usuario.getId());

        return Jwts.builder()
                .setSubject(usuario.getNomeUser())
                .claim("idUsuario", usuario.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiracaoSegundos * 1000))
                .signWith(chave, SignatureAlgorithm.HS256)
                .compact();
    }

    public int validarTokenERetornarId(String token) {
        log.debug("Validando access token");

        return Jwts.parserBuilder()
                .setSigningKey(chave)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("idUsuario", Integer.class);
    }
}