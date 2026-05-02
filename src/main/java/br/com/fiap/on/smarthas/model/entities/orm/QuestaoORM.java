package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_questoes")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class QuestaoORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Integer id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idAvaliacao", referencedColumnName = "id", insertable = false, updatable = false)
    private AvaliacaoORM avaliacao;

    @Column(name = "enunciado", nullable = false)
    private String enunciado;

    // Pode ser nulo, nem toda questão precisa de uma ilustração
    @Column(name = "linkFoto")
    private String linkFoto;

    // Quanto vale a questão, no fim a soma do valor das questões de cada avaliação precisa dar 100
    @Column(name = "valor", nullable = false)
    private Integer valor;
}
