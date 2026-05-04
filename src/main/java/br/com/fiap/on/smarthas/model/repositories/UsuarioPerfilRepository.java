package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.PerfilORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioPerfilORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioPerfilRepository extends JpaRepository<UsuarioPerfilORM, Long> {
    List<UsuarioPerfilORM> findByUsuario(UsuarioORM usuario);
    List<UsuarioPerfilORM> findByPerfil(PerfilORM perfil);
}
