package br.com.fiap.on.smarthas.model.entities.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AvaliacaoDTO {
    private Long id;
    private Integer maxTentativas;
    private Integer pontuacaoMinima;

    @NotNull(message = "Obrigatório informar o tempo de prova")
    private Integer tempoProvaEmMinutos;
}
