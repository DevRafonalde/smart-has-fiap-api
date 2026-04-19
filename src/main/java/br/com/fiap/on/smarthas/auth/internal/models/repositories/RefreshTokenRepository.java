package br.com.fiap.on.smarthas.auth.internal.models.repositories;

import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.RefreshTokenORM;
import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.UsuarioORM;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshTokenORM, Integer> {
    Optional<RefreshTokenORM> findByToken(String token);
    List<RefreshTokenORM> findByUsuario(UsuarioORM usuario);
}