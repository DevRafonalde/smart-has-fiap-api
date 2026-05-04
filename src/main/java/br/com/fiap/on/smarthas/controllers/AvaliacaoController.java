package br.com.fiap.on.smarthas.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {
    @PostMapping("/avaliacoes/{id}/submeter")

    @GetMapping("/avaliacoes/{id}/resultado")
}
