package br.com.fiap.on.smarthas.model.entities.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioPerfilDTO {
    @Valid
    private UsuarioDTO usuario;
    private List<PerfilDTO> perfisUsuario;
}
