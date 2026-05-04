package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.services.AulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aulas")
@RequiredArgsConstructor
public class AulaController {
    private final AulaService aulaService;

    @PostMapping("/assistida/{idAula}/aluno/{idAluno}")
    public ResponseEntity<?> aulaAssistida(@PathVariable Long idAula, @PathVariable Long idAluno) {
        aulaService.aulaAssistida(idAula, idAluno);

        return ResponseEntity.ok().build();
    }
}
