package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_cursos")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CursoORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",  updatable = false, nullable = false)
    private Integer id;

    @Column(name = "titulo",  nullable = false)
    private String titulo;

    @Column(name = "descricao",   nullable = false)
    private String descricao;

    @Column(name = "ativo",  nullable = false)
    private Boolean ativo = true;
}
