package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.model.entities.orm.ModuloORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ModuloRepository extends JpaRepository<ModuloORM, Long> {
    int countByCurso_Id(Long cursoId);
    List<ModuloORM> findByCurso_Id(Long cursoId);
}
