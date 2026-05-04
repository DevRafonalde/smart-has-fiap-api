package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_tokenResetSenha")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenResetSenhaORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", referencedColumnName = "id", nullable = false)
    private UsuarioORM usuario;

    @Column(name = "token", nullable = false, length = 6)
    private String token;

    @Column(name = "expiracao", nullable = false)
    private LocalDateTime expiracao;

    @Column(name = "utilizado", nullable = false)
    private Boolean utilizado = false;

    @Column(name = "criadoEm", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public TokenResetSenhaORM(UsuarioORM usuario, String token, LocalDateTime expiracao) {
        this.usuario = usuario;
        this.token = token;
        this.expiracao = expiracao;
        this.utilizado = false;
        this.criadoEm = LocalDateTime.now();
    }

    public boolean estaExpirado() {
        return LocalDateTime.now().isAfter(this.expiracao);
    }

    public boolean estaValido() {
        return !this.utilizado && !estaExpirado();
    }
}
