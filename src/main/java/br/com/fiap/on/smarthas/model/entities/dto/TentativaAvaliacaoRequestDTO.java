package br.com.fiap.on.smarthas.model.entities.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TentativaAvaliacaoRequestDTO {
    @NotNull(message = "O ID da avaliação é obrigatório")
    private Long idAvaliacao;

    @NotNull(message = "O ID do aluno é obrigatório")
    private Long idAluno;

    @NotNull(message = "A nota é obrigatória")
    @Min(value = 0, message = "A nota mínima é 0")
    @Max(value = 100, message = "A nota máxima é 100")
    private Integer nota;
}
