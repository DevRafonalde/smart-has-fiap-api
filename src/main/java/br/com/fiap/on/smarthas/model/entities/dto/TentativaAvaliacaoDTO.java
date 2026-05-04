package br.com.fiap.on.smarthas.model.entities.dto;

import br.com.fiap.on.smarthas.model.entities.orm.AvaliacaoORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TentativaAvaliacaoDTO {
    private AvaliacaoORM avaliacao;
    private UsuarioORM aluno;
    private Integer nota;
    private boolean aprovado;
}
