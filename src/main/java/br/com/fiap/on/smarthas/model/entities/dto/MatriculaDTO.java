package br.com.fiap.on.smarthas.model.entities.dto;

import br.com.fiap.on.smarthas.utils.StatusMatricula;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MatriculaDTO {
    private StatusMatricula status;
    private UsuarioDTO aluno;
    private CursoDTO curso;
    private LocalDateTime dataInicio;
    private LocalDateTime dataConclusao;
}
