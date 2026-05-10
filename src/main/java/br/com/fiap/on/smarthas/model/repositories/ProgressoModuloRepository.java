package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.CursoORM;
import br.com.fiap.on.smarthas.model.entities.orm.ModuloORM;
import br.com.fiap.on.smarthas.model.entities.orm.ProgressoModuloORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressoModuloRepository extends JpaRepository<ProgressoModuloORM, Long> {
    List<ProgressoModuloORM> findByAluno_IdAndModulo_Curso_IdAndConcluido(Long alunoId, Long moduloCursoId, boolean concluido);
    Optional<ProgressoModuloORM> findByAlunoAndModulo(UsuarioORM aluno, ModuloORM modulo);
    long countByModuloCursoAndModuloAtivo(CursoORM curso, boolean b);
    long countByAlunoAndModuloCursoAndConcluidoTrue(UsuarioORM aluno, CursoORM curso);
}
