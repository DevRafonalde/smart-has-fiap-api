package br.com.fiap.on.smarthas.model.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OpcaoQuestaoDTO {
    private Long id;
    private String texto;
    private String linkFoto;
    private boolean correta;
}
