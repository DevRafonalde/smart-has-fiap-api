package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.exceptions.CondicaoInsatisfeitaException;
import br.com.fiap.on.smarthas.model.entities.dto.LoginRequestDTO;
import br.com.fiap.on.smarthas.model.entities.dto.PerfilDTO;
import br.com.fiap.on.smarthas.model.entities.dto.UsuarioDTO;
import br.com.fiap.on.smarthas.model.entities.dto.UsuarioPerfilDTO;
import br.com.fiap.on.smarthas.model.entities.orm.PerfilORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import br.com.fiap.on.smarthas.model.entities.orm.UsuarioPerfilORM;
import br.com.fiap.on.smarthas.model.repositories.PerfilRepository;
import br.com.fiap.on.smarthas.model.repositories.UsuarioPerfilRepository;
import br.com.fiap.on.smarthas.model.repositories.UsuarioRepository;
import br.com.fiap.on.smarthas.config.PasswordUtil;
import br.com.fiap.on.smarthas.exceptions.AcessoNaoAutorizadoException;
import br.com.fiap.on.smarthas.exceptions.AtributoJaUtilizadoException;
import br.com.fiap.on.smarthas.exceptions.ElementoNaoEncontradoException;
import br.com.fiap.on.smarthas.utils.FormatarNomeMaiusculo;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
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
    private final VerificacaoEmailService verificacaoEmailService;

    @Value("${smarthas.security.perfil-padrao}")
    private String perfilPadrao;

    public List<UsuarioDTO> listarTodos(Pageable pageable) {
        Page<UsuarioORM> usuarios = usuarioRepository.findAll(pageable);

        return usuarios.stream()
                .filter(usuario -> usuario.getNomeAmigavel() != null && usuario.getAtivo())
                .map(usuario -> mapper.map(usuario, UsuarioDTO.class))
                .toList();
    }

    public UsuarioPerfilDTO listarEspecifico(Long id) {
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

    public UsuarioPerfilDTO clonar(Long id) {
        UsuarioPerfilDTO usuarioExistente = listarEspecifico(id);
        usuarioExistente.setUsuario(null);
        return usuarioExistente;
    }

    public void deletar(Long id) {
        UsuarioORM usuarioDelete = usuarioRepository.findById(id)
                .orElseThrow(() -> new ElementoNaoEncontradoException("Usuário não encontrado no banco de dados"));

        List<UsuarioPerfilORM> vinculosUsuario = usuarioPerfilRepository.findByUsuario(usuarioDelete);
        if (!vinculosUsuario.isEmpty()) {
            usuarioPerfilRepository.deleteAll(vinculosUsuario);
        }

        usuarioRepository.delete(usuarioDelete);
    }

    public UsuarioPerfilDTO novoUsuario(UsuarioPerfilDTO usuarioPerfilDTO) {
        if (usuarioPerfilDTO.getUsuario().getNomeAmigavel().contains("Administrador") || usuarioPerfilDTO.getUsuario().getNomeCompleto().contains("Administrador")) {
            throw new CondicaoInsatisfeitaException("Nome não pode conter \"Administrador\"");
        }

        UsuarioORM usuarioExistente = usuarioRepository.findByCpf(usuarioPerfilDTO.getUsuario().getCpf());
        if (Objects.nonNull(usuarioExistente)) {
            throw new AtributoJaUtilizadoException("CPF já está cadastrado");
        }

        UsuarioORM usuarioExistente2 = usuarioRepository.findByEmail(usuarioPerfilDTO.getUsuario().getEmail());
        if (Objects.nonNull(usuarioExistente2)) {
            throw new AtributoJaUtilizadoException("E-mail já está cadastrado");
        }

        UsuarioORM usuarioRecebido = mapper.map(usuarioPerfilDTO.getUsuario(), UsuarioORM.class);
        usuarioRecebido.setSenhaUser(PasswordUtil.hashPassword(usuarioRecebido.getSenhaUser()));
        usuarioRecebido.setNomeAmigavel(FormatarNomeMaiusculo.formatar(usuarioRecebido.getNomeAmigavel()));
        usuarioRecebido.setNomeCompleto(FormatarNomeMaiusculo.formatar(usuarioRecebido.getNomeCompleto()));

        UsuarioORM usuarioCadastrado = usuarioRepository.save(usuarioRecebido);

        verificacaoEmailService.enviarCodigoVerificacao(usuarioCadastrado.getId());

        return vincularPerfisAoUsuario(usuarioPerfilDTO, usuarioCadastrado, usuarioCadastrado);
    }

    public UsuarioPerfilDTO editar(UsuarioPerfilDTO usuarioPerfilDTO) {
        UsuarioORM usuarioRecebido = mapper.map(usuarioPerfilDTO.getUsuario(), UsuarioORM.class);

        UsuarioORM usuarioBanco = usuarioRepository.findById(usuarioRecebido.getId())
                .orElseThrow(() -> new ElementoNaoEncontradoException("Usuário não encontrado no banco de dados"));

        if (!usuarioRecebido.getEmail().equalsIgnoreCase(usuarioBanco.getEmail())) {
            UsuarioORM usuarioExistente = usuarioRepository.findByEmail(usuarioRecebido.getEmail());
            if (Objects.nonNull(usuarioExistente)) {
                throw new AtributoJaUtilizadoException("E-mail já está sendo utilizado");
            }
        }

        usuarioRecebido.setSenhaUser(usuarioBanco.getSenhaUser());
        usuarioRecebido.setNomeAmigavel(FormatarNomeMaiusculo.formatar(usuarioRecebido.getNomeAmigavel()));
        usuarioRecebido.setNomeCompleto(FormatarNomeMaiusculo.formatar(usuarioRecebido.getNomeCompleto()));

        UsuarioORM usuarioSalvo = usuarioRepository.save(usuarioRecebido);

        List<UsuarioPerfilORM> registrosExistentes = usuarioPerfilRepository.findByUsuario(usuarioRecebido);
        usuarioPerfilRepository.deleteAll(registrosExistentes);

        return vincularPerfisAoUsuario(usuarioPerfilDTO, usuarioRecebido, usuarioSalvo);
    }

    @NonNull
    private UsuarioPerfilDTO vincularPerfisAoUsuario(
            UsuarioPerfilDTO modeloCadastroUsuarioPerfil,
            UsuarioORM usuarioRecebido,
            UsuarioORM usuarioSalvo) {

        List<PerfilORM> perfis;

        if (modeloCadastroUsuarioPerfil.getPerfisUsuario() == null
                || modeloCadastroUsuarioPerfil.getPerfisUsuario().isEmpty()) {

            PerfilORM perfilDefault = perfilRepository.findByMnemonico(perfilPadrao)
                    .orElseThrow(() -> new RuntimeException("Perfil padrão não encontrado"));

            perfis = List.of(perfilDefault);

        } else {

            perfis = modeloCadastroUsuarioPerfil.getPerfisUsuario()
                    .stream()
                    .map(this::resolverPerfil)
                    .toList();
        }

        List<PerfilDTO> perfisDto = new ArrayList<>();

        for (PerfilORM perfil : perfis) {
            UsuarioPerfilORM usuarioPerfil = new UsuarioPerfilORM();
            usuarioPerfil.setUsuario(usuarioRecebido);
            usuarioPerfil.setDataHora(LocalDateTime.now());
            usuarioPerfil.setPerfil(perfil);

            usuarioPerfilRepository.save(usuarioPerfil);

            perfisDto.add(mapper.map(perfil, PerfilDTO.class));
        }

        return new UsuarioPerfilDTO(
                mapper.map(usuarioSalvo, UsuarioDTO.class),
                perfisDto
        );
    }

    public UsuarioORM autenticar(LoginRequestDTO loginRequest) {
        UsuarioORM usuarioEncontrado;
        if (!loginRequest.getCpf().isEmpty()) {
            usuarioEncontrado = usuarioRepository.findByCpf(loginRequest.getCpf());
        } else if (!loginRequest.getEmail().isEmpty()) {
            usuarioEncontrado = usuarioRepository.findByEmail(loginRequest.getEmail());
        } else {
            throw new AcessoNaoAutorizadoException("Faça o login inserindo um e-mail ou um CPF");
        }

        if (PasswordUtil.verificarSenha(loginRequest.getSenha(), usuarioEncontrado.getSenhaUser())) {
            return usuarioEncontrado;
        }
        throw new ElementoNaoEncontradoException("Usuário ou senha inválidos");
    }

    public void loginFeito(UsuarioORM usuario) {
        // Lógica opcional pós-login, como atualização de status ou auditoria
        // Exemplo: atualizar data de último acesso, etc.
    }

    public UsuarioORM buscarOrmPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ElementoNaoEncontradoException("Usuário não encontrado"));
    }

    private PerfilORM resolverPerfil(PerfilDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("Perfil deve possuir ID");
        }

        return perfilRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado: " + dto.getId()));
    }
}
