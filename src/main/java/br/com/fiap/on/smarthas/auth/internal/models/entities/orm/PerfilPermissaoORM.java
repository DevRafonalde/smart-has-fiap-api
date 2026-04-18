package br.com.fiap.on.smarthas.auth.internal.models.entities.orm;

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
    @Column(name = "id")
    private Integer id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idPerfil", referencedColumnName = "id")
    private PerfilORM perfil;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idPermissao", referencedColumnName = "id")
    private PermissaoORM permissao;

    @Column(name = "dataHora")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataHora;
}
