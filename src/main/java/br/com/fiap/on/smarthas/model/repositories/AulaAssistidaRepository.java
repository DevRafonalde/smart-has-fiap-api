package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.AulaAssistidaORM;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AulaAssistidaRepository extends JpaRepository<AulaAssistidaORM, Long> {
//    @Query("""
//    SELECT COUNT(aa)
//    FROM AulaAssistidaORM aa
//    JOIN aa.aula a
//    WHERE aa.aluno.id = :alunoId
//      AND a.modulo.id = :moduloId
//""")
//    long countAssistidas(Long alunoId, Long moduloId);

    long countAulaAssistidaORMByAluno_IdAndAula_Modulo_Id(Long alunoId, Long moduloId);
}
