package br.com.fiap.on.smarthas.model.entities.dto;

import br.com.fiap.on.smarthas.model.entities.orm.CursoORM;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ModuloDTO {
    private CursoORM curso;
    private String titulo;
    private String descricao;
    private Integer ordem;
    private Boolean ativo;
}
