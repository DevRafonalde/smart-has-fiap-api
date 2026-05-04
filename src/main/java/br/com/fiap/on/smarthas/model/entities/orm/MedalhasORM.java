package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_medalhas_usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MedalhasORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idUsuario", referencedColumnName = "id", nullable = false, updatable = false)
    private UsuarioORM aluno;

    @Column(name = "iconeMedalha", nullable = false)
    private String iconeMedalha;

    @Column(name = "mensagem", nullable = false)
    private String mensagem;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idTentativa", referencedColumnName = "id", nullable = false, updatable = false)
    private TentativaAvaliacaoORM tentativaAvaliacao;
}
