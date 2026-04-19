package br.com.fiap.on.smarthas.auth.internal.models.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_refreshTokens")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RefreshTokenORM {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", referencedColumnName = "id", nullable = false)
    private UsuarioORM usuario;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "expiracao", nullable = false)
    private LocalDateTime expiracao;

    @Column(name = "revogado", nullable = false)
    private Boolean revogado = false;

    @Column(name = "criadoEm", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public RefreshTokenORM(UsuarioORM usuario, String token, LocalDateTime expiracao) {
        this.usuario = usuario;
        this.token = token;
        this.expiracao = expiracao;
        this.revogado = false;
        this.criadoEm = LocalDateTime.now();
    }

    public boolean estaExpirado() {
        return LocalDateTime.now().isAfter(this.expiracao);
    }

    public boolean estaValido() {
        return !this.revogado && !estaExpirado();
    }
}