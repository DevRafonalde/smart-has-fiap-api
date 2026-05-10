package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.exceptions.AtributoJaUtilizadoException;
import br.com.fiap.on.smarthas.exceptions.ElementoNaoEncontradoException;
import br.com.fiap.on.smarthas.model.entities.dto.AulaDTO;
import br.com.fiap.on.smarthas.model.entities.orm.AulaAssistidaORM;
import br.com.fiap.on.smarthas.model.entities.orm.AulaORM;
import br.com.fiap.on.smarthas.model.entities.orm.ModuloORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import br.com.fiap.on.smarthas.model.repositories.AulaAssistidaRepository;
import br.com.fiap.on.smarthas.model.repositories.AulaRepository;
import br.com.fiap.on.smarthas.model.repositories.ModuloRepository;
import br.com.fiap.on.smarthas.model.repositories.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AulaService {
    private final AulaRepository aulaRepository;
    private final AulaAssistidaRepository aulaAssistidaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;
    private final ModuloRepository moduloRepository;

    public void aulaAssistida(Long idAula, Long idAluno) {
        AulaORM aula = aulaRepository.findById(idAula).orElseThrow(() -> new ElementoNaoEncontradoException("Aula não encontrada"));
        UsuarioORM usuario = usuarioRepository.findById(idAluno).orElseThrow(() -> new ElementoNaoEncontradoException("Usuário não encontrado"));

        AulaAssistidaORM aulaAssistidaORM = new AulaAssistidaORM();
        aulaAssistidaORM.setAluno(usuario);
        aulaAssistidaORM.setAula(aula);

        aulaAssistidaRepository.save(aulaAssistidaORM);
    }

    public AulaDTO novaAula(@Valid AulaDTO aulaRecebida) {
        AulaORM aulaComMesmoVideo = aulaRepository.findByLinkAulaLike(aulaRecebida.getLinkAula()).orElse(null);
        if (aulaComMesmoVideo != null && aulaComMesmoVideo.getLinkAula().equalsIgnoreCase(aulaRecebida.getLinkAula())) {
            throw new AtributoJaUtilizadoException("Já existe aula cadastrada com esse vídeo");
        }

        ModuloORM moduloORM = moduloRepository.findById(aulaRecebida.getModulo().getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Módulo não encontrado"));
        AulaORM aula = new AulaORM();
        aula.setModulo(moduloORM);
        aula.setTitulo(aulaRecebida.getTitulo());
        aula.setDescricao(aulaRecebida.getDescricao());
        aula.setOrdem(aulaRecebida.getOrdem());
        aula.setLinkAula(aulaRecebida.getLinkAula());

        return modelMapper.map(aulaRepository.save(aula), AulaDTO.class);
    }

    public List<AulaDTO> listarTodas(Pageable pageable) {
        Page<AulaORM> aulas = aulaRepository.findAll(pageable);

        return aulas.stream()
                .map(usuario -> modelMapper.map(usuario, AulaDTO.class))
                .toList();
    }

    public AulaDTO buscarPorID(Long id) {
        AulaORM aula = aulaRepository.findById(id).orElseThrow(() -> new ElementoNaoEncontradoException("Aula não encontrada"));
        return modelMapper.map(aula, AulaDTO.class);
    }

    public AulaDTO editar(@Valid AulaDTO aulaRecebida) {
        AulaORM aula = aulaRepository.findById(aulaRecebida.getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Aula não encontrada"));

        if (!aulaRecebida.getLinkAula().equalsIgnoreCase(aula.getLinkAula())) {
            AulaORM aulaComMesmoVideo = aulaRepository.findByLinkAulaLike(aulaRecebida.getLinkAula()).orElse(null);
            if (aulaComMesmoVideo != null && aulaComMesmoVideo.getLinkAula().equalsIgnoreCase(aulaRecebida.getLinkAula())) {
                throw new AtributoJaUtilizadoException("Já existe aula cadastrada com esse vídeo");
            }
        }

        aula.setTitulo(aulaRecebida.getTitulo());
        aula.setDescricao(aulaRecebida.getDescricao());
        aula.setOrdem(aulaRecebida.getOrdem());
        aula.setLinkAula(aulaRecebida.getLinkAula());

        return modelMapper.map(aulaRepository.save(aula), AulaDTO.class);
    }

    @Transactional
    public void deletar(Long id) {
        aulaAssistidaRepository.deleteAllByAula_Id(id);
        aulaRepository.deleteById(id);
    }
}
