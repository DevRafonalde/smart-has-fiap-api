package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.PermissaoORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissaoRepository extends JpaRepository<PermissaoORM, Integer> {
    Optional<PermissaoORM> findByNome(String nome);
}
