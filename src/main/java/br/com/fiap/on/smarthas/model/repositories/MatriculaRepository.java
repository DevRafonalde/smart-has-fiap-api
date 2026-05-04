package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.MatriculaORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatriculaRepository extends JpaRepository<MatriculaORM, Long> {
    boolean existsMatriculaORMByAluno_IdAndCurso_Id(Long idAluno, Long idCurso);
    Optional<MatriculaORM> findByAluno_IdAndCurso_Id(Long idAluno, Long idCurso);
}
