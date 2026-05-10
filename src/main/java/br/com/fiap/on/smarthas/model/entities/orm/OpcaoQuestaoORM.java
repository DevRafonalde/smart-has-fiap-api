package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_opcoes_questao")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OpcaoQuestaoORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idQuestao", referencedColumnName = "id", nullable = false, updatable = false)
    private QuestaoORM questao;

    @Column(name = "texto")
    private String texto;

    @Column(name = "linkFoto")
    private String linkFoto;

    @Column(name = "correta", nullable = false)
    private boolean correta;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
