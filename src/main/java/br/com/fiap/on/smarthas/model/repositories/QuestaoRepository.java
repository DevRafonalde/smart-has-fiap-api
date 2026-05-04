package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.QuestaoORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestaoRepository extends JpaRepository<QuestaoORM, Long> {
    List<QuestaoORM> findByAvaliacao_Id(Long avaliacaoId);
}
