package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.model.entities.dto.ModuloDTO;
import br.com.fiap.on.smarthas.services.ModuloService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/modulos")
@RequiredArgsConstructor
public class ModuloController {
    private final ModuloService moduloService;

    @GetMapping("/curso/{id}")
    public ResponseEntity<List<ModuloDTO>> buscarModulosPorCurso(@PathVariable Long id) {
        return ResponseEntity.ok().body(moduloService.buscarModulosPorCurso(id));
    }

}
