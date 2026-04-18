package br.com.fiap.on.smarthas.auth.internal.models.entities.orm;

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
    @Column(name = "id")
    private Integer id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idUsuario", referencedColumnName = "id")
    private UsuarioORM usuario;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idPerfil", referencedColumnName = "id")
    private PerfilORM perfil;

    @Column(name = "dataHora")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataHora;
}