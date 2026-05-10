package br.com.fiap.on.smarthas.model.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TentativaAvaliacaoResponseDTO {
    private Long id;
    private boolean aprovado;
    private Integer tentativaAtual;
    private Integer maxTentativas;
    private Integer tentativasRestantes;
    private boolean tutoriaObrigatoria;
}
