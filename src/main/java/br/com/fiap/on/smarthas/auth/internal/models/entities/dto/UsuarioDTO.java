package br.com.fiap.on.smarthas.auth.internal.models.entities.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class UsuarioDTO {
    private Integer id;

    @NotBlank(message = "Insira o nome completo do usuário")
    private String nomeCompleto;

    @NotBlank(message = "Insira um nome amigável para o usuário")
    private String nomeAmigavel;

    @NotBlank(message = "Insira o nome de usuário pelo qual o mesmo irá fazer login")
    private String nomeUser;

    @NotBlank(message = "Insira uma senha")
    @Size(min = 8, message = "A senha deve conter pelo menos 8 caracteres")
//    @Pattern(
//            regexp = "^(?=.[0-9])(?=.[a-z])(?=.[A-Z])(?=.[@#$%^&+=!])(?=\\S+$).{12,}$",
//            message = "A senha deve conter maiúsculas, minúsculas, números e caracteres especiais"
//    )
    private String senhaUser;

    private Boolean ativo = true;
}
