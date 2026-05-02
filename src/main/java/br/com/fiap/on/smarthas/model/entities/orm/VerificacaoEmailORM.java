package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_verificacaoEmail")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VerificacaoEmailORM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", referencedColumnName = "id", nullable = false)
    private UsuarioORM usuario;

    // Código de 6 dígitos enviado por e-mail
    @Column(name = "codigo", nullable = false, length = 6)
    private String codigo;

    @Column(name = "expiracao", nullable = false)
    private LocalDateTime expiracao;

    @Column(name = "utilizado", nullable = false)
    private Boolean utilizado = false;

    @Column(name = "criadoEm", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public VerificacaoEmailORM(UsuarioORM usuario, String codigo, LocalDateTime expiracao) {
        this.usuario = usuario;
        this.codigo = codigo;
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
