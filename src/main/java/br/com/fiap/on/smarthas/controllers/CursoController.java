package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.annotations.Permissao;
import br.com.fiap.on.smarthas.model.entities.dto.CursoDTO;
import br.com.fiap.on.smarthas.model.entities.dto.MatriculaDTO;
import br.com.fiap.on.smarthas.model.entities.dto.ProgressoDTO;
import br.com.fiap.on.smarthas.services.CursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoController {
    private final CursoService cursoService;

    @GetMapping("/")
    public ResponseEntity<List<CursoDTO>> buscarTodos() {
        return ResponseEntity.ok(cursoService.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cursoService.buscarPorId(id));
    }

    @PostMapping("/matricular/{idAluno}/curso/{idCurso}")
    @Permissao(rota = "matricular")
    public ResponseEntity<MatriculaDTO> matricular(@PathVariable Long idAluno, @PathVariable Long idCurso) {
        return ResponseEntity.ok(cursoService.matricularAluno(idAluno, idCurso));
    }

    @PostMapping("/cancelar-matricula/{idAluno}/curso/{idCurso}")
    @Permissao(rota = "cancelarmatricula")
    public ResponseEntity<MatriculaDTO> cancelarMatricula(@PathVariable Long idAluno, @PathVariable Long idCurso) {
        return ResponseEntity.ok(cursoService.cancelarMatricula(idAluno, idCurso));
    }

    @GetMapping("/meu-progresso/{idAluno}/curso/{idCurso}")
    @Permissao(rota = "consultarprogresso")
    public ResponseEntity<ProgressoDTO> consultarProgresso(@PathVariable Long idAluno, @PathVariable Long idCurso) {
        return ResponseEntity.ok(cursoService.consultarProgresso(idAluno, idCurso));
    }

    @PostMapping("/")
    @Permissao(rota = "criarcurso")
    public ResponseEntity<CursoDTO> criarCurso(@RequestBody CursoDTO cursoDTO) {
        return ResponseEntity.ok(cursoService.criarCurso(cursoDTO));
    }

    @PutMapping("/{id}")
    @Permissao(rota = "editarcurso")
    public ResponseEntity<CursoDTO> editarCurso(@PathVariable Long id, @RequestBody CursoDTO cursoDTO) {
        return ResponseEntity.ok(cursoService.editarCurso(id, cursoDTO));
    }
}
