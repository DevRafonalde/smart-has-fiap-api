package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.annotations.Permissao;
import br.com.fiap.on.smarthas.model.entities.dto.TentativaAvaliacaoDTO;
import br.com.fiap.on.smarthas.services.AvaliacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {
    private final AvaliacaoService avaliacaoService;

    @PostMapping("/submeter")
    @Permissao(rota = "submeteravaliacao")
    public ResponseEntity<TentativaAvaliacaoDTO> submeterAvaliacao(@RequestBody TentativaAvaliacaoDTO tentativaAvaliacaoDTO) {
        return ResponseEntity.ok(avaliacaoService.submeterAvaliacao(tentativaAvaliacaoDTO));
    }
}
