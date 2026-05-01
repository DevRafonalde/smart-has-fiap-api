package br.com.fiap.on.smarthas.model.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    // Pode logar por e-mail ou por CPF
    private String email;
    private String cpf;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;
}
