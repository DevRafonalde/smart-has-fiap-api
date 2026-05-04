package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.ProgressoModuloORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressoModuloRepository extends JpaRepository<ProgressoModuloORM, Long> {
    List<ProgressoModuloORM> findByAluno_IdAndModulo_Curso_IdAndConcluido(Long alunoId, Long moduloCursoId, boolean concluido);
}
