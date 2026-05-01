package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.model.entities.dto.*;
import br.com.fiap.on.smarthas.model.entities.orm.*;
import br.com.fiap.on.smarthas.model.repositories.*;
import br.com.fiap.on.smarthas.exceptions.ElementoNaoEncontradoException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PerfilService {
    private PerfilRepository perfilRepository;
    private PerfilPermissaoRepository perfilPermissaoRepository;
    private PermissaoRepository permissaoRepository;
    private UsuarioPerfilRepository usuarioPerfilRepository;
    private UsuarioRepository usuarioRepository;
    private ModelMapper mapper;
    private Validator validator;

    /**
     * @return Todos os perfis cadastrados, sem a listagem de suas permissões
     */
    public List<PerfilDTO> listarTodos(Pageable pageable) {
        Page<PerfilORM> perfisBanco = perfilRepository.findAll(pageable);

        // Aqui é uma expressão lambda que passa por todos os itens, os tranforma em DTO e retorna uma lista de DTOs
        return perfisBanco.stream()
                .map(perfil -> mapper.map(perfil, PerfilDTO.class))
                .toList();
    }

    public PerfilUsuarioDTO listarUsuariosVinculados(Integer id) {
        PerfilORM perfil = perfilRepository.findById(id).orElseThrow(() -> new ElementoNaoEncontradoException("Perfil não encontrado no banco de dados"));
        PerfilDTO perfilDTO = mapper.map(perfil, PerfilDTO.class);

        PerfilUsuarioDTO modeloCadastroPerfilUsuario = new PerfilUsuarioDTO();
        modeloCadastroPerfilUsuario.setPerfil(perfilDTO);

        // Aqui eu busco todos os registro de relações por perfil
        // Mapeio todos os usuários, filtro os não nulos, filtro os ativos
        // Ordeno por ordem alfabética os nomes amigáveis, tranformo os itens em DTO e retorno uma lista
        List<UsuarioDTO> usuarios = usuarioPerfilRepository.findByPerfil(perfil).stream()
                .map(UsuarioPerfilORM::getUsuario)
                .filter(Objects::nonNull)
                .filter(UsuarioORM::getAtivo)
                .sorted(Comparator.comparing(UsuarioORM::getNomeAmigavel))
                .map(usuario -> mapper.map(usuario, UsuarioDTO.class))
                .toList();

        if (usuarios.isEmpty()) {
            modeloCadastroPerfilUsuario.setUsuarios(new ArrayList<>());
        } else {
            modeloCadastroPerfilUsuario.setUsuarios(usuarios);
        }

        return modeloCadastroPerfilUsuario;
    }

    /**
     * @param id Id do perfil a ser pesquisado
     * @return Objeto de perfil com todas as permissões vinculadas a ele
     */
    public PerfilPermissaoDTO listarEspecifico(Integer id) {
        PerfilORM perfil = perfilRepository.findById(id).orElseThrow(() -> new ElementoNaoEncontradoException("Perfil não encontrado no banco de dados"));
        PerfilDTO perfilDTO = mapper.map(perfil, PerfilDTO.class);

        PerfilPermissaoDTO perfilPermissaoRecebido = new PerfilPermissaoDTO();
        perfilPermissaoRecebido.setPerfil(perfilDTO);

        List<PermissaoDTO> permissoes = perfilPermissaoRepository.findByPerfil(perfil).stream()
                .map(PerfilPermissaoORM::getPermissao)
                .filter(Objects::nonNull)
                .map(permissao -> mapper.map(permissao, PermissaoDTO.class))
                .toList();

        perfilPermissaoRecebido.setPermissoes(permissoes);

        return perfilPermissaoRecebido;
    }

    public PerfilPermissaoDTO clonar(Integer id) {
        PerfilPermissaoDTO perfilExistente = listarEspecifico(id);
        perfilExistente.setPerfil(new PerfilDTO());
        return perfilExistente;
    }

    public void deletar(int id) {
        PerfilORM perfilDelete = perfilRepository.findById(id).orElseThrow(() -> new ElementoNaoEncontradoException("Perfil não encontrado no banco de dados"));

        List<UsuarioPerfilORM> usuariosPerfil = usuarioPerfilRepository.findByPerfil(perfilDelete);
        if (!usuariosPerfil.isEmpty()) {
            usuarioPerfilRepository.deleteAll(usuariosPerfil);
        }

        List<PerfilPermissaoORM> perfisPermissao = perfilPermissaoRepository.findByPerfil(perfilDelete);
        if (!perfisPermissao.isEmpty()) {
            perfilPermissaoRepository.deleteAll(perfisPermissao);
        }

        perfilRepository.delete(perfilDelete);
    }

    public PerfilPermissaoDTO novoPerfil(PerfilPermissaoDTO perfilPermissaoRecebido) {
        validator.validate(perfilPermissaoRecebido.getPerfil(), PerfilDTO.class);
        PerfilORM perfilRecebido = mapper.map(perfilPermissaoRecebido.getPerfil(), PerfilORM.class);
        PerfilORM perfilNovo = perfilRepository.save(perfilRecebido);

        List<PermissaoORM> permissoes = perfilPermissaoRecebido.getPermissoes().stream()
                .map(permissaoDTO -> mapper.map(permissaoDTO, PermissaoORM.class))
                .toList();

        List<PermissaoDTO> permissoesDTO = new ArrayList<>();

        for (PermissaoORM permissao : permissoes) {
            PerfilPermissaoORM perfilPermissao = new PerfilPermissaoORM();
            perfilPermissao.setPerfil(perfilNovo);
            perfilPermissao.setDataHora(LocalDateTime.now());
            perfilPermissao.setPermissao(permissao);
            perfilPermissaoRepository.save(perfilPermissao);

            permissoesDTO.add(mapper.map(permissao, PermissaoDTO.class));
        }

        return new PerfilPermissaoDTO(mapper.map(perfilNovo, PerfilDTO.class), permissoesDTO);
    }

    public PerfilPermissaoDTO editar(PerfilPermissaoDTO perfilPermissaoRecebido) {
        validator.validate(perfilPermissaoRecebido.getPerfil(), PerfilDTO.class);
        PerfilORM perfilMexido = mapper.map(perfilPermissaoRecebido.getPerfil(), PerfilORM.class);

        perfilRepository.findById(perfilMexido.getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Perfil não encontrado no banco de dados"));

        PerfilORM perfilSalvo = perfilRepository.save(perfilMexido);
        List<PerfilPermissaoORM> registrosExistentes = perfilPermissaoRepository.findByPerfil(perfilMexido);
        perfilPermissaoRepository.deleteAll(registrosExistentes);

        List<PermissaoORM> permissoes = perfilPermissaoRecebido.getPermissoes()
                .stream()
                .map(permissaoDTO -> mapper.map(permissaoDTO, PermissaoORM.class))
                .toList();

        for (PermissaoORM permissao : permissoes) {
            PerfilPermissaoORM perfilPermissao = new PerfilPermissaoORM();
            perfilPermissao.setPerfil(perfilMexido);
            perfilPermissao.setDataHora(LocalDateTime.now());
            perfilPermissao.setPermissao(permissao);
            perfilPermissaoRepository.save(perfilPermissao);
        }

        List<PermissaoDTO> permissoesVinculadas = perfilPermissaoRepository.findByPerfil(perfilSalvo).stream()
                .map(PerfilPermissaoORM::getPermissao)
                .map(permissao -> mapper.map(permissao, PermissaoDTO.class))
                .toList();

        PerfilPermissaoDTO modeloRetorno = new PerfilPermissaoDTO();
        modeloRetorno.setPerfil(mapper.map(perfilSalvo, PerfilDTO.class));
        modeloRetorno.setPermissoes(permissoesVinculadas);

        return modeloRetorno;
    }

    // TODO Refazer esse método futuramente
//    public ModeloCadastroPerfilUsuarioId vincularUsuariosEmLote(ModeloCadastroPerfilUsuarioId modeloCadastroPerfilUsuarioId) {
//        PerfilORM perfilRecebido = mapper.map(modeloCadastroPerfilUsuarioId.getPerfil(), PerfilORM.class);
//        List<Integer> usuariosId = modeloCadastroPerfilUsuarioId.getUsuariosPerfilId();
//        List<UsuarioORM> usuarios = usuarioRepository.findAllById(usuariosId);
//
//        List<UsuarioPerfilORM> registrosExistentes = usuarioPerfilRepository.findByPerfil(perfilRecebido);
//        usuarioPerfilRepository.deleteAll(registrosExistentes);
//
//        for (UsuarioORM usuario : usuarios) {
//            UsuarioPerfilORM usuarioPerfil = new UsuarioPerfilORM();
//            usuarioPerfil.setPerfil(perfilRecebido);
//            usuarioPerfil.setDataHora(LocalDateTime.now());
//            usuarioPerfil.setUsuario(usuario);
//            usuarioPerfilRepository.save(usuarioPerfil);
//        }
//
//        return modeloCadastroPerfilUsuarioId;
//    }
}