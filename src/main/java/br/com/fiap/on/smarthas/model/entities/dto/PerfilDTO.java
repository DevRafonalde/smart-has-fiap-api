package br.com.fiap.on.smarthas.model.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class PerfilDTO {
    private Integer id;

    @NotBlank(message = "O nome do perfil é obrigatório")
    private String nome;

    @NotBlank(message = "Insira uma breve descrição sobre o funcionamento do perfil")
    private String descricao;

    private Boolean ativo = true;
}