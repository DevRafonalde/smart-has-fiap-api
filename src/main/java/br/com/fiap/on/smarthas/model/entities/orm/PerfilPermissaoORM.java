package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_perfilPermissao")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PerfilPermissaoORM {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idPerfil", referencedColumnName = "id", nullable = false)
    private PerfilORM perfil;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idPermissao", referencedColumnName = "id", nullable = false)
    private PermissaoORM permissao;

    @Column(name = "dataHora", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataHora;
}
