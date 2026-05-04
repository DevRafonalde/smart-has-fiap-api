package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.exceptions.ElementoNaoEncontradoException;
import br.com.fiap.on.smarthas.model.entities.orm.AulaAssistidaORM;
import br.com.fiap.on.smarthas.model.entities.orm.AulaORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import br.com.fiap.on.smarthas.model.repositories.AulaAssistidaRepository;
import br.com.fiap.on.smarthas.model.repositories.AulaRepository;
import br.com.fiap.on.smarthas.model.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AulaService {
    private final AulaRepository aulaRepository;
    private final AulaAssistidaRepository aulaAssistidaRepository;
    private final UsuarioRepository usuarioRepository;

    public void aulaAssistida(Long idAula, Long idAluno) {
        AulaORM aula = aulaRepository.findById(idAula).orElseThrow(() -> new ElementoNaoEncontradoException("Aula não encontrada"));
        UsuarioORM usuario = usuarioRepository.findById(idAluno).orElseThrow(() -> new ElementoNaoEncontradoException("Usuário não encontrado"));

        AulaAssistidaORM aulaAssistidaORM = new AulaAssistidaORM();
        aulaAssistidaORM.setAluno(usuario);
        aulaAssistidaORM.setAula(aula);

        aulaAssistidaRepository.save(aulaAssistidaORM);
    }
}
