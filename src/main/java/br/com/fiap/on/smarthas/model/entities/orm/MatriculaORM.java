package br.com.fiap.on.smarthas.model.entities.orm;

import br.com.fiap.on.smarthas.utils.StatusMatricula;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_matriculas")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MatriculaORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Integer id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusMatricula status = StatusMatricula.PENDENTE;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idUsuario", referencedColumnName = "id", nullable = false, updatable = false)
    private UsuarioORM aluno;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idCurso", referencedColumnName = "id", nullable = false, updatable = false)
    private CursoORM curso;

    @Column(name = "dataInicio")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataInicio;

    @Column(name = "dataConclusao")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataConclusao;
}
