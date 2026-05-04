package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.AvaliacaoORM;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoRepository extends JpaRepository<AvaliacaoORM, Long> {
    AvaliacaoORM findByModulo_Id(Long moduloId);
}
