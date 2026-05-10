package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.MedalhasUsuarioORM;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedalhasUsuarioRepository extends JpaRepository<MedalhasUsuarioORM, Long> {
}
