package br.com.fiap.on.smarthas.auth.internal.services;

import br.com.fiap.on.smarthas.auth.internal.models.entities.dto.PerfilDTO;
import br.com.fiap.on.smarthas.auth.internal.models.entities.dto.UsuarioDTO;
import br.com.fiap.on.smarthas.auth.internal.models.entities.dto.UsuarioPerfilDTO;
import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.PerfilORM;
import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.UsuarioORM;
import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.UsuarioPerfilORM;
import br.com.fiap.on.smarthas.auth.internal.models.repositories.UsuarioPerfilRepository;
import br.com.fiap.on.smarthas.auth.internal.models.repositories.UsuarioRepository;
import br.com.fiap.on.smarthas.config.PasswordUtil;
import br.com.fiap.on.smarthas.shared.exceptions.AtributoJaUtilizadoException;
import br.com.fiap.on.smarthas.shared.exceptions.ElementoNaoEncontradoException;
import br.com.fiap.on.smarthas.shared.utils.FormatarNomeMaiusculo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;
    private final ModelMapper mapper;

    public List<UsuarioDTO> listarTodos(Pageable pageable) {
        Page<UsuarioORM> usuarios = usuarioRepository.findAll(pageable);

        return usuarios.stream()
                .filter(usuario -> usuario.getNomeAmigavel() != null && usuario.getAtivo())
                .map(usuario -> mapper.map(usuario, UsuarioDTO.class))
                .toList();
    }

    public UsuarioPerfilDTO listarEspecifico(Integer id) {
        UsuarioORM usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ElementoNaoEncontradoException("Usuário não encontrado no banco de dados"));

        UsuarioPerfilDTO usuarioPerfilDTO = new UsuarioPerfilDTO();
        usuarioPerfilDTO.setUsuario(mapper.map(usuario, UsuarioDTO.class));
        List<PerfilDTO> perfis = usuarioPerfilRepository.findByUsuario(usuario).stream()
                .map(UsuarioPerfilORM::getPerfil)
                .filter(Objects::nonNull)
                .map(perfil -> mapper.map(perfil, PerfilDTO.class))
                .toList();

        usuarioPerfilDTO.setPerfisUsuario(perfis.isEmpty() ? new ArrayList<>() : perfis);

        return usuarioPerfilDTO;
    }

    public UsuarioPerfilDTO clonar(Integer id) {
        UsuarioPerfilDTO usuarioExistente = listarEspecifico(id);
        usuarioExistente.setUsuario(null);
        return usuarioExistente;
    }

    public void deletar(Integer id) {
        UsuarioORM usuarioDelete = usuarioRepository.findById(id)
                .orElseThrow(() -> new ElementoNaoEncontradoException("Usuário não encontrado no banco de dados"));

        List<UsuarioPerfilORM> vinculosUsuario = usuarioPerfilRepository.findByUsuario(usuarioDelete);
        if (!vinculosUsuario.isEmpty()) {
            usuarioPerfilRepository.deleteAll(vinculosUsuario);
        }

        usuarioRepository.delete(usuarioDelete);
    }

    public UsuarioPerfilDTO novoUsuario(UsuarioPerfilDTO usuarioPerfilDTO) {
        System.out.println(usuarioPerfilDTO);
        UsuarioORM usuarioExistente = usuarioRepository.findByNomeUser(usuarioPerfilDTO.getUsuario().getNomeUser());

        if (Objects.nonNull(usuarioExistente)) {
            throw new AtributoJaUtilizadoException("Nome de Usuário já está sendo utilizado");
        }

        UsuarioORM usuarioRecebido = mapper.map(usuarioPerfilDTO.getUsuario(), UsuarioORM.class);
        usuarioRecebido.setSenhaUser(PasswordUtil.hashPassword(usuarioRecebido.getSenhaUser()));
        usuarioRecebido.setNomeAmigavel(FormatarNomeMaiusculo.formatar(usuarioRecebido.getNomeAmigavel()));
        usuarioRecebido.setNomeCompleto(FormatarNomeMaiusculo.formatar(usuarioRecebido.getNomeCompleto()));

        UsuarioORM usuarioCadastrado = usuarioRepository.save(usuarioRecebido);

        List<PerfilORM> perfis = usuarioPerfilDTO.getPerfisUsuario()
                .stream()
                .map(perfilDTO -> mapper.map(perfilDTO, PerfilORM.class))
                .toList();

        List<PerfilDTO> perfisDto = new ArrayList<>();

        for (PerfilORM perfil : perfis) {
            UsuarioPerfilORM usuarioPerfil = new UsuarioPerfilORM();
            usuarioPerfil.setUsuario(usuarioCadastrado);
            usuarioPerfil.setDataHora(LocalDateTime.now());
            usuarioPerfil.setPerfil(perfil);
            usuarioPerfilRepository.save(usuarioPerfil);
            perfisDto.add(mapper.map(perfil, PerfilDTO.class));
        }

        return new UsuarioPerfilDTO(mapper.map(usuarioCadastrado, UsuarioDTO.class), perfisDto);
    }

    public UsuarioPerfilDTO editar(UsuarioPerfilDTO modeloCadastroUsuarioPerfil) {
        UsuarioORM usuarioRecebido = mapper.map(modeloCadastroUsuarioPerfil.getUsuario(), UsuarioORM.class);

        UsuarioORM usuarioBanco = usuarioRepository.findById(usuarioRecebido.getId())
                .orElseThrow(() -> new ElementoNaoEncontradoException("Usuário não encontrado no banco de dados"));

        if (!usuarioRecebido.getNomeUser().equalsIgnoreCase(usuarioBanco.getNomeUser())) {
            UsuarioORM usuarioExistente = usuarioRepository.findByNomeUser(usuarioRecebido.getNomeUser());
            if (Objects.nonNull(usuarioExistente)) {
                throw new AtributoJaUtilizadoException("Nome de usuário já está sendo utilizado");
            }
        }

        usuarioRecebido.setSenhaUser(usuarioBanco.getSenhaUser());
        usuarioRecebido.setNomeAmigavel(FormatarNomeMaiusculo.formatar(usuarioRecebido.getNomeAmigavel()));
        usuarioRecebido.setNomeCompleto(FormatarNomeMaiusculo.formatar(usuarioRecebido.getNomeCompleto()));

        UsuarioORM usuarioSalvo = usuarioRepository.save(usuarioRecebido);

        List<UsuarioPerfilORM> registrosExistentes = usuarioPerfilRepository.findByUsuario(usuarioRecebido);
        usuarioPerfilRepository.deleteAll(registrosExistentes);

        List<PerfilORM> perfis = modeloCadastroUsuarioPerfil.getPerfisUsuario()
                .stream()
                .map(perfilDTO -> mapper.map(perfilDTO, PerfilORM.class))
                .toList();

        List<PerfilDTO> perfisDto = new ArrayList<>();

        for (PerfilORM perfil : perfis) {
            UsuarioPerfilORM usuarioPerfil = new UsuarioPerfilORM();
            usuarioPerfil.setUsuario(usuarioRecebido);
            usuarioPerfil.setDataHora(LocalDateTime.now());
            usuarioPerfil.setPerfil(perfil);
            usuarioPerfilRepository.save(usuarioPerfil);
            perfisDto.add(mapper.map(perfil, PerfilDTO.class));
        }

        return new UsuarioPerfilDTO(mapper.map(usuarioSalvo, UsuarioDTO.class), perfisDto);
    }

    public UsuarioORM autenticar(String nomeUser, String senha) {
        UsuarioORM usuarioEncontrado = usuarioRepository.findByNomeUser(nomeUser);
        if (PasswordUtil.verificarSenha(senha, usuarioEncontrado.getSenhaUser())) {
            return usuarioEncontrado;
        }
        throw new ElementoNaoEncontradoException("Usuário ou senha inválidos");
    }

    public void loginFeito(UsuarioORM usuario) {
        // Lógica opcional pós-login, como atualização de status ou auditoria
        // Exemplo: atualizar data de último acesso, etc.
    }
}
