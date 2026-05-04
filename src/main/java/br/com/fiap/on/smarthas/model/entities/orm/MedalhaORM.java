package br.com.fiap.on.smarthas.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_medalhas")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MedalhaORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id;

    @Column(name = "iconeMedalha", nullable = false)
    private String iconeMedalha;

    @Column(name = "mensagem", nullable = false)
    private String mensagem;

    @Column(name = "mnemonico", nullable = false, updatable = false)
    private String mnemonico;
}
