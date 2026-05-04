package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.exceptions.ElementoExistenteException;
import br.com.fiap.on.smarthas.exceptions.ElementoNaoEncontradoException;
import br.com.fiap.on.smarthas.model.entities.dto.CursoDTO;
import br.com.fiap.on.smarthas.model.entities.dto.MatriculaDTO;
import br.com.fiap.on.smarthas.model.entities.dto.ProgressoDTO;
import br.com.fiap.on.smarthas.model.entities.orm.CursoORM;
import br.com.fiap.on.smarthas.model.entities.orm.MatriculaORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import br.com.fiap.on.smarthas.model.repositories.CursoRepository;
import br.com.fiap.on.smarthas.model.repositories.MatriculaRepository;
import br.com.fiap.on.smarthas.model.repositories.UsuarioRepository;
import br.com.fiap.on.smarthas.utils.StatusMatricula;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CursoService {
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MatriculaRepository matriculaRepository;
    private final ModelMapper modelMapper;

    public List<CursoDTO> buscarTodos() {
        return cursoRepository.findAll()
                .stream()
                .map(cursoORM -> modelMapper.map(cursoORM, CursoDTO.class))
                .toList();
    }

    public CursoDTO buscarPorId(Long id) {
        return modelMapper.map(
                cursoRepository.findById(id)
                        .orElseThrow(() -> new ElementoNaoEncontradoException("Curso não encontrado")),
                CursoDTO.class
        );
    }

    public MatriculaDTO matricularAluno(Long idAluno, Long idCurso) {
        if (matriculaRepository.existsMatriculaORMByAluno_IdAndCurso_Id(idAluno, idCurso)) {
            throw new ElementoExistenteException("Esse aluno já está matriculado nesse curso");
        }

        UsuarioORM usuarioORM = usuarioRepository.findById(idAluno).orElseThrow(() -> new ElementoNaoEncontradoException("Usuário não encontrado"));
        CursoORM cursoORM = cursoRepository.findById(idCurso).orElseThrow(() -> new ElementoNaoEncontradoException("Curso não encontrado"));

        MatriculaORM matriculaORM = new MatriculaORM();
        matriculaORM.setAluno(usuarioORM);
        matriculaORM.setCurso(cursoORM);
        matriculaORM.setDataInicio(LocalDateTime.now());
        matriculaORM.setStatus(StatusMatricula.ATIVA); // TODO Verificar o que poderia ativar e criar como pendente

        return modelMapper.map(matriculaRepository.save(matriculaORM), MatriculaDTO.class);
    }

    public MatriculaDTO cancelarMatricula(Long idAluno, Long idCurso) {
        MatriculaORM matriculaORM = matriculaRepository.findByAluno_IdAndCurso_Id(idAluno, idCurso).orElseThrow(() -> new ElementoNaoEncontradoException("Matrícula não encontrada"));
        matriculaORM.setStatus(StatusMatricula.CANCELADA);
        // TODO Trocar perfil do usuário para usuário com matrícula cancelada

        return modelMapper.map(matriculaRepository.save(matriculaORM), MatriculaDTO.class);
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
