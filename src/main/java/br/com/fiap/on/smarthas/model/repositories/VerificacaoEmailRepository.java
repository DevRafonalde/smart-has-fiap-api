package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import br.com.fiap.on.smarthas.model.entities.orm.VerificacaoEmailORM;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificacaoEmailRepository extends CrudRepository<VerificacaoEmailORM, Long> {

    // Busca o código mais recente não utilizado de um usuário
    Optional<VerificacaoEmailORM> findFirstByUsuarioAndUtilizadoFalseOrderByCriadoEmDesc(UsuarioORM usuario);

    // Busca por usuário — usado para invalidar códigos anteriores antes de emitir um novo
    List<VerificacaoEmailORM> findByUsuario(UsuarioORM usuario);
}
