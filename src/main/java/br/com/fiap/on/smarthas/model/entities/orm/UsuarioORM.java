package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_usuarios")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UsuarioORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id;

    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    @Column(name = "nomeCompleto", nullable = false)
    private String nomeCompleto;

    @Column(name = "nomeAmigavel", nullable = false)
    private String nomeAmigavel;

    @Column(name = "senhaUser", nullable = false)
    private String senhaUser;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "emailVerificado", nullable = false)
    private boolean emailVerificado = false;

    @Column(name = "senhaAtualizada", nullable = false)
    private boolean senhaAtualizada =  false;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}