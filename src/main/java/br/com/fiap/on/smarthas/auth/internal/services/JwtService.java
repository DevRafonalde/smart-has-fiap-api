package br.com.fiap.on.smarthas.auth.internal.services;

import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.UsuarioORM;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private final Key chave;

    @SneakyThrows
    public JwtService() {
        String chaveString = Files.readString(Paths.get("jwt/jwt_secret_key.txt"));
        this.chave = Keys.hmacShaKeyFor(chaveString.getBytes());
    }

    public String gerarToken(UsuarioORM usuario) {
        System.out.println("Token Gerado");
        return Jwts.builder()
                .setSubject(usuario.getNomeUser())
                .claim("idUsuario", usuario.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 horas
                .signWith(chave, SignatureAlgorithm.HS256)
                .compact();
    }

    public int validarTokenERetornarId(String token) {
        System.out.println("Entrou no método validar token");
        return Jwts.parserBuilder()
                .setSigningKey(chave)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("idUsuario", Integer.class);
    }
}
