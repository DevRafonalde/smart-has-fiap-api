package br.com.fiap.on.smarthas.auth.internal.models.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private Integer id;
    private String nomeAmigavel;
    private String nomeUser;
    private String token;
}
