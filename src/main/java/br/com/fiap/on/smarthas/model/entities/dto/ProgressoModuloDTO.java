package br.com.fiap.on.smarthas.model.entities.dto;

import br.com.fiap.on.smarthas.model.entities.orm.ModuloORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;

import java.time.LocalDateTime;

public class ProgressoModuloDTO {
    private UsuarioORM aluno;
    private ModuloORM modulo;
    private boolean concluido;
    private LocalDateTime dataConclusao;
}
