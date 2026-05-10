package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.MedalhaORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedalhaRepository extends JpaRepository<MedalhaORM, Long> {
    Optional<MedalhaORM> findByMnemonico(String medalhaModuloConcluido);
}
