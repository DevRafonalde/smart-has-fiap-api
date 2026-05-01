package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<UsuarioORM, Integer> {
    List<UsuarioORM> findByNomeAmigavelContaining(String nomeAmigavel);
    UsuarioORM findByEmail(String email);
    UsuarioORM findByCpf(String cpf);
}
