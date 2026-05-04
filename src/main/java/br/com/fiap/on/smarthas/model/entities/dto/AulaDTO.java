package br.com.fiap.on.smarthas.model.entities.dto;

import br.com.fiap.on.smarthas.model.entities.orm.ModuloORM;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AulaDTO {
    private ModuloORM modulo;
    private String titulo;
    private String descricao;
    private Integer ordem;
    private String linkAula;
}
