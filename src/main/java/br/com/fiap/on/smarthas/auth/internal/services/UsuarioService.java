package br.com.fiap.on.smarthas.auth.internal.services;

import br.com.fiap.on.smarthas.auth.internal.models.entities.dto.PerfilDTO;
import br.com.fiap.on.smarthas.auth.internal.models.entities.dto.UsuarioDTO;
import br.com.fiap.on.smarthas.auth.internal.models.entities.dto.UsuarioPerfilDTO;
import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.PerfilORM;
import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.UsuarioORM;
import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.UsuarioPerfilORM;
import br.com.fiap.on.smarthas.auth.internal.models.repositories.PerfilRepository;
import br.com.fiap.on.smarthas.auth.internal.models.repositories.UsuarioPerfilRepository;
import br.com.fiap.on.smarthas.auth.internal.models.repositories.UsuarioRepository;
import br.com.fiap.on.smarthas.config.PasswordUtil;
import br.com.fiap.on.smarthas.shared.exceptions.AtributoJaUtilizadoException;
import br.com.fiap.on.smarthas.shared.exceptions.ElementoNaoEncontradoException;
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
    private final PerfilRepository perfilRepository;
    private final ModelMapper mapper;

    public List<UsuarioDTO> listarTodos(Pageable pageable) {
        Page<UsuarioORM> usuarios = usuarioRepository.findAll(pageable);

        List<UsuarioDTO> usuariosAmigaveis = usuarios.stream()
                .filter(usuario -> usuario.getNomeAmigavel() != null && usuario.getAtivo())
                .map(usuario -> mapper.map(usuario, UsuarioDTO.class))
                .toList();

        usuariosAmigaveis.forEach(
                usuario -> usuario.setNomeAmigavel(
                        usuario.getNomeAmigavel()
                                .substring(0, 1)
                                .toUpperCase()
                                .concat(usuario.getNomeAmigavel().substring(1))
                )
        );

        return usuariosAmigaveis;
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
        UsuarioORM usuarioMexido = mapper.map(modeloCadastroUsuarioPerfil.getUsuario(), UsuarioORM.class);

        UsuarioORM usuarioBanco = usuarioRepository.findById(usuarioMexido.getId())
                .orElseThrow(() -> new ElementoNaoEncontradoException("Usuário não encontrado no banco de dados"));

        if (!usuarioMexido.getNomeUser().equalsIgnoreCase(usuarioBanco.getNomeUser())) {
            UsuarioORM usuarioExistente = usuarioRepository.findByNomeUser(usuarioMexido.getNomeUser());
            if (Objects.nonNull(usuarioExistente)) {
                throw new AtributoJaUtilizadoException("Nome de usuário já está sendo utilizado");
            }
        }

        usuarioMexido.setSenhaUser(usuarioBanco.getSenhaUser());

        UsuarioORM usuarioSalvo = usuarioRepository.save(usuarioMexido);

        List<UsuarioPerfilORM> registrosExistentes = usuarioPerfilRepository.findByUsuario(usuarioMexido);
        usuarioPerfilRepository.deleteAll(registrosExistentes);

        List<PerfilORM> perfis = modeloCadastroUsuarioPerfil.getPerfisUsuario()
                .stream()
                .map(perfilDTO -> mapper.map(perfilDTO, PerfilORM.class))
                .toList();

        List<PerfilDTO> perfisDto = new ArrayList<>();

        for (PerfilORM perfil : perfis) {
            UsuarioPerfilORM usuarioPerfil = new UsuarioPerfilORM();
            usuarioPerfil.setUsuario(usuarioMexido);
            usuarioPerfil.setDataHora(LocalDateTime.now());
            usuarioPerfil.setPerfil(perfil);
            usuarioPerfilRepository.save(usuarioPerfil);
            perfisDto.add(mapper.map(perfil, PerfilDTO.class));
        }

        return new UsuarioPerfilDTO(mapper.map(usuarioSalvo, UsuarioDTO.class), perfisDto);
    }

    public void criarUsuarioAdminPadrao() {
        if (Objects.isNull(usuarioRepository.findByNomeUser("admin"))) {
            UsuarioORM admin = new UsuarioORM();
            admin.setNomeCompleto("Administrador");
            admin.setNomeAmigavel("Administrador");
            admin.setNomeUser("admin");
            admin.setSenhaUser(PasswordUtil.hashPassword("123456"));

            UsuarioORM usuarioAdmin = usuarioRepository.save(admin);

            PerfilORM perfilAdmin = perfilRepository.findByMnemonico("admin-sistema");

            UsuarioPerfilORM usuarioPerfilAdmin = new UsuarioPerfilORM();
            usuarioPerfilAdmin.setUsuario(usuarioAdmin);
            usuarioPerfilAdmin.setPerfil(perfilAdmin);

            usuarioPerfilRepository.save(usuarioPerfilAdmin);
        }
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
