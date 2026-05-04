package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.PerfilORM;
import br.com.fiap.on.smarthas.model.entities.orm.PerfilPermissaoORM;
import br.com.fiap.on.smarthas.model.entities.orm.PermissaoORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerfilPermissaoRepository extends JpaRepository<PerfilPermissaoORM, Long> {
    List<PerfilPermissaoORM> findByPerfil(PerfilORM perfil);
    List<PerfilPermissaoORM> findByPermissao(PermissaoORM permissao);
}
