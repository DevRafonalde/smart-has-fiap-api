package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.AulaORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AulaRepository extends JpaRepository<AulaORM, Long> {
    long countByModulo_Id(Long moduloId);
    List<AulaORM> findByModulo_Id(Long moduloId);

    Optional<AulaORM> findByLinkAulaLike(String linkAula);
}
