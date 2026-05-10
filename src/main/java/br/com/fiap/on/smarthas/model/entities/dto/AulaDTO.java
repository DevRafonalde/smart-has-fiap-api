package br.com.fiap.on.smarthas.model.entities.dto;

import br.com.fiap.on.smarthas.model.entities.orm.ModuloORM;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AulaDTO {
    private Long id;

    @NotNull(message = "Obrigatório informar a qual módulo essa aula pertence")
    private ModuloORM modulo;

    @NotBlank(message = "Obrigatório informar o título da aula")
    private String titulo;

    @NotBlank(message = "Obrigatório informar a descrição/\"sinopse\" da aula")
    private String descricao;

    @NotNull(message = "Obrigatório informar a ordem dessa aula em relação às outras")
    private Integer ordem;

    @NotBlank(message = "Obrigatório inserir o link do vídeo da aula")
    private String linkAula;
}
