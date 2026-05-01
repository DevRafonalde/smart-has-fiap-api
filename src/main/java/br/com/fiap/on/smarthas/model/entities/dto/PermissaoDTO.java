package br.com.fiap.on.smarthas.model.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PermissaoDTO {
    private Integer id;

    @NotBlank(message = "O nome da permissão é obrigatório")
    private String nome;

    @NotBlank(message = "Insira uma breve descrição sobre o funcionamento da permissão")
    private String descricao;

    private Boolean ativo = true;
}