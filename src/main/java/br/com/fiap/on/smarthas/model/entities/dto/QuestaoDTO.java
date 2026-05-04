package br.com.fiap.on.smarthas.model.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestaoDTO {
    private Long id;
    private String enunciado;
    // Pode ser nulo, nem toda questão precisa de uma ilustração
    private String linkFoto;
    // Quanto vale a questão, no fim a soma do valor das questões de cada avaliação precisa dar 100
    private Integer valor;
    private List<OpcaoQuestaoDTO> alternativas;
}
