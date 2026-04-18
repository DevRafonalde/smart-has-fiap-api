package br.com.fiap.on.smarthas.auth.internal.models.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_usuarios")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UsuarioORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "cpf")
    private String cpf;

    @Column(name = "nomeCompleto")
    private String nomeCompleto;

    @Column(name = "nomeAmigavel")
    private String nomeAmigavel;

    @Column(name = "nomeUser")
    private String nomeUser;

    @Column(name = "senhaUser")
    private String senhaUser;

    @Column(name = "senhaAtualizada")
    private Boolean senhaAtualizada;

    @Column(name = "ativo")
    private Boolean ativo = true;
}