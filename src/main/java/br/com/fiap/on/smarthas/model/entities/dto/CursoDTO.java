package br.com.fiap.on.smarthas.model.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CursoDTO {
    private String titulo;
    private String descricao;
    private Boolean ativo;
}
