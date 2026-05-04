package br.com.fiap.on.smarthas.model.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProgressoDTO {
    List<ProgressoModuloDTO> qtdModulosConcluidos;
    double porcentagemConcluido;
}
