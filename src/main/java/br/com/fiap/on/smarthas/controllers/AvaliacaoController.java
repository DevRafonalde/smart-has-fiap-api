package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.annotations.Permissao;
import br.com.fiap.on.smarthas.model.entities.dto.AvaliacaoCompletaDTO;
import br.com.fiap.on.smarthas.model.entities.dto.TentativaAvaliacaoRequestDTO;
import br.com.fiap.on.smarthas.model.entities.dto.TentativaAvaliacaoResponseDTO;
import br.com.fiap.on.smarthas.services.AvaliacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {
    private final AvaliacaoService avaliacaoService;

    @PostMapping("/submeter")
    @Permissao(rota = "submeteravaliacao")
    public ResponseEntity<TentativaAvaliacaoResponseDTO> submeterAvaliacao(@RequestBody TentativaAvaliacaoRequestDTO tentativaAvaliacaoRequestDTO) {
        return ResponseEntity.ok(avaliacaoService.submeterAvaliacao(tentativaAvaliacaoRequestDTO));
    }

    @GetMapping("/modulo/{idModulo}/aluno/{idAluno}")
    @Permissao(rota = "buscaravaliacao")
    public ResponseEntity<AvaliacaoCompletaDTO> buscarAvaliacao(@PathVariable Long idModulo, @PathVariable Long idAluno) {
        return ResponseEntity.ok(avaliacaoService.buscarAvaliacao(idModulo, idAluno, false));
    }

    @GetMapping("/modulo/{idModulo}/")
    @Permissao(rota = "buscaravaliacaoadmin")
    public ResponseEntity<AvaliacaoCompletaDTO> buscarAvaliacaoAdmin(@PathVariable Long idModulo) {
        return ResponseEntity.ok(avaliacaoService.buscarAvaliacao(idModulo, null, true));
    }

}
