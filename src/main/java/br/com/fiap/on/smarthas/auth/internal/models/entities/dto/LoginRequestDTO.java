package br.com.fiap.on.smarthas.auth.internal.models.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "Nome de usuário é obrigatório")
    private String nomeUser;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;
}
