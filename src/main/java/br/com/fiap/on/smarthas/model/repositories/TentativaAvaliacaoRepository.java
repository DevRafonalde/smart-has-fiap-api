package br.com.fiap.on.smarthas.model.repositories;

import br.com.fiap.on.smarthas.model.entities.orm.AvaliacaoORM;
import br.com.fiap.on.smarthas.model.entities.orm.TentativaAvaliacaoORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TentativaAvaliacaoRepository extends JpaRepository<TentativaAvaliacaoORM, Long> {
    List<TentativaAvaliacaoORM> findByAvaliacaoAndAluno(AvaliacaoORM avaliacao, UsuarioORM aluno);

}
