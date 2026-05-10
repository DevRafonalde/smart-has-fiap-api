package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_questoes")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class QuestaoORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idAvaliacao", referencedColumnName = "id", nullable = false, updatable = false)
    private AvaliacaoORM avaliacao;

    @Column(name = "enunciado", nullable = false)
    private String enunciado;

    // Pode ser nulo, nem toda questão precisa de uma ilustração
    @Column(name = "linkFoto")
    private String linkFoto;

    // Quanto vale a questão, no fim a soma do valor das questões de cada avaliação precisa dar 100
    @Column(name = "valor", nullable = false)
    private Integer valor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
