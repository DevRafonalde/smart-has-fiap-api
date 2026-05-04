package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.OpcaoQuestaoORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpcaoQuestaoRepository extends JpaRepository<OpcaoQuestaoORM, Long> {
    List<OpcaoQuestaoORM> findByQuestao_Id(Long questaoId);
    List<OpcaoQuestaoORM> findAllbyQuestao_Id(List<Long> questaoId);
}
