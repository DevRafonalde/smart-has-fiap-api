package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.AulaORM;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AulaRepository extends JpaRepository<AulaORM, Long> {
    long countByModulo_Id(Long moduloId);
}
