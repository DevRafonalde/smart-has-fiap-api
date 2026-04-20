package br.com.fiap.on.smarthas.auth.internal.models.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private Integer id;
    private String nomeAmigavel;
    private String cpf;
    private String email;
    private String accessToken;
    private String refreshToken;
    private Long expiracaoAccessTokenSegundos;
}