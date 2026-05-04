package br.com.fiap.on.smarthas.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/modulos")
@RequiredArgsConstructor
public class ModuloController {
    @PostMapping("/{id}/concluir")
    @GetMapping("/curso/{id}") // buscar módulos por curso

}
