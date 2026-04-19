package br.com.fiap.on.smarthas.auth.internal.models.repositories;

import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.PerfilORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerfilRepository extends JpaRepository<PerfilORM, Integer> {
    List<PerfilORM> findByNomeContaining(String nome);
    Optional<PerfilORM> findByMnemonico(String mnemonico);
}
