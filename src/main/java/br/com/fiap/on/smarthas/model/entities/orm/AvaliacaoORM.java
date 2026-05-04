package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_avaliacoes")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AvaliacaoORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id;

    @Column(name = "maxTentativas", nullable = false)
    private Integer maxTentativas = 3;

    @Column(name = "pontuacaoMinima", nullable = false)
    private Integer pontuacaoMinima = 70;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idModulo", referencedColumnName = "id", unique = true, nullable = false, updatable = false)
    private ModuloORM modulo;
}
