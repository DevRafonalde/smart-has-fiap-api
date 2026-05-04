package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_modulos")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ModuloORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idCurso", referencedColumnName = "id", updatable = false, nullable = false)
    private CursoORM curso;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descricao", nullable = false)
    private String descricao;

    @Column(name = "ordemConteudo", nullable = false)
    private Integer ordem;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;
}
