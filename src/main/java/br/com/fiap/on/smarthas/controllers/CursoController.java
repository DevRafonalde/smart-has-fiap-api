package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.annotations.Permissao;
import br.com.fiap.on.smarthas.model.entities.dto.CursoDTO;
import br.com.fiap.on.smarthas.model.entities.dto.MatriculaDTO;
import br.com.fiap.on.smarthas.model.entities.dto.ProgressoDTO;
import br.com.fiap.on.smarthas.services.CursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
        return new ResponseEntity<>(cursoService.buscarTodos(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoDTO> buscarPorId(@PathVariable Long id) {
        return new ResponseEntity<>(cursoService.buscarPorId(id), HttpStatus.OK);
    }

    @PostMapping("/matricular/{idAluno}/curso/{idCurso}")
    @Permissao(rota = "matricular")
    public ResponseEntity<MatriculaDTO> matricular(@PathVariable Long idAluno, @PathVariable Long idCurso) {
        return new ResponseEntity<>(cursoService.matricularAluno(idAluno, idCurso), HttpStatus.OK);
    }

    @PostMapping("/cancelar-matricula/{idAluno}/curso/{idCurso}")
    @Permissao(rota = "cancelarmatricula")
    public ResponseEntity<MatriculaDTO> cancelarMatricula(@PathVariable Long idAluno, @PathVariable Long idCurso) {
        return new ResponseEntity<>(cursoService.cancelarMatricula(idAluno, idCurso), HttpStatus.OK);
    }

    @GetMapping("/meu-progresso/{idAluno}/curso/{idCurso}")
    @Permissao(rota = "consultarprogresso")
    public ResponseEntity<ProgressoDTO> consultarProgresso(@PathVariable Long idAluno, @PathVariable Long idCurso) {
        return new ResponseEntity<>(cursoService.consultarProgresso(idAluno, idCurso), HttpStatus.OK);
    }

    @PostMapping("/")// — criar curso
    @Permissao(rota = "criarcurso")
    public ResponseEntity<CursoDTO> criarCurso(@RequestBody CursoDTO cursoDTO) {
        return new ResponseEntity<>(cursoService.criarCurso(cursoDTO), HttpStatus.OK);
    }

    @PutMapping("/{id}") // editar
    @Permissao(rota = "editarcurso")
    public ResponseEntity<CursoDTO> editarCurso(@PathVariable Long id, @RequestBody CursoDTO cursoDTO) {
        return new ResponseEntity<>(cursoService.editarCurso(id, cursoDTO), HttpStatus.OK);
    }
}
