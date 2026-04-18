package br.com.fiap.on.smarthas.auth.internal.models.repositories;

import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.PerfilORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerfilRepository extends JpaRepository<PerfilORM, Integer> {
    List<PerfilORM> findByNomeContaining(String nome);
}
