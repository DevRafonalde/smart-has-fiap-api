package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_progresso_modulo_usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProgressoModuloORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idUsuario", referencedColumnName = "id", nullable = false, updatable = false)
    private UsuarioORM aluno;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idModulo", referencedColumnName = "id", nullable = false, updatable = false)
    private ModuloORM modulo;

    @Column(name = "concluido", nullable = false)
    private boolean concluido = false;

    @Column(name = "dataConclusao")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataConclusao;
}
