package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_tentativas_avaliacao")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TentativaAvaliacaoORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idAvaliacao", referencedColumnName = "id", nullable = false, updatable = false)
    private AvaliacaoORM avaliacao;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idUsuario", referencedColumnName = "id", nullable = false, updatable = false)
    private UsuarioORM aluno;

    @Column(name = "nota", nullable = false)
    private Integer nota;

    @Column(name = "aprovado", nullable = false)
    private boolean aprovado;

    @Column(name = "dataConclusao")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataConclusao;
}
