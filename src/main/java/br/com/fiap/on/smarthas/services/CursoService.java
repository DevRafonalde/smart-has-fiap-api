package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.model.entities.dto.CursoDTO;
import br.com.fiap.on.smarthas.model.entities.dto.MatriculaDTO;
import br.com.fiap.on.smarthas.model.entities.dto.ProgressoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CursoService {
    public List<CursoDTO> buscarTodos() {
        return null;
    }

    public CursoDTO buscarPorId(Long id) {
        return null;
    }

    public MatriculaDTO matricularAluno(Long idAluno, Long idCurso) {
        return null;
    }

    public MatriculaDTO cancelarMatricula(Long idAluno, Long idCurso) {
        return null;
    }

    public ProgressoDTO consultarProgresso(Long idAluno, Long idCurso) {
        return null;
    }

    public CursoDTO criarCurso(CursoDTO cursoDTO) {
        return null;
    }

    public CursoDTO editarCurso(Long id, CursoDTO cursoDTO) {
        return null;
    }
}
