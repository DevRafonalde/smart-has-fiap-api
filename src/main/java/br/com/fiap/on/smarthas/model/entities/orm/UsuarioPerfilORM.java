package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_usuarioPerfil")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UsuarioPerfilORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Integer id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idUsuario", referencedColumnName = "id", nullable = false)
    private UsuarioORM usuario;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idPerfil", referencedColumnName = "id", nullable = false)
    private PerfilORM perfil;

    @Column(name = "dataHora", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataHora;
}