package br.com.fiap.on.smarthas.auth.internal.models.entities.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ─── Request: cliente envia o código recebido por e-mail ──────────────────────
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificacaoEmailRequestDTO {

    @NotBlank(message = "O código de verificação é obrigatório")
    @Size(min = 6, max = 6, message = "O código deve ter exatamente 6 dígitos")
    private String codigo;
}
