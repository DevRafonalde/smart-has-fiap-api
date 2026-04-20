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
import br.com.fiap.on.smarthas.shared.exceptions.CPFInvalidoException;
import br.com.fiap.on.smarthas.shared.exceptions.ElementoNaoEncontradoException;
import br.com.fiap.on.smarthas.shared.utils.FormatarNomeMaiusculo;
import br.com.fiap.on.smarthas.shared.utils.ValidarCPF;
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

    @Value("${smarthas.security.perfil-padrao}")
    private String perfilPadrao;

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

        if (ValidarCPF.validar(usuarioPerfilDTO.getUsuario().getCpf())) {
            throw new CPFInvalidoException("O CPF informado não é válido");
        }

        UsuarioORM usuarioRecebido = mapper.map(usuarioPerfilDTO.getUsuario(), UsuarioORM.class);
        usuarioRecebido.setSenhaUser(PasswordUtil.hashPassword(usuarioRecebido.getSenhaUser()));
        usuarioRecebido.setNomeAmigavel(FormatarNomeMaiusculo.formatar(usuarioRecebido.getNomeAmigavel()));
        usuarioRecebido.setNomeCompleto(FormatarNomeMaiusculo.formatar(usuarioRecebido.getNomeCompleto()));

        UsuarioORM usuarioCadastrado = usuarioRepository.save(usuarioRecebido);

        return vincularPerfisAoUsuario(usuarioPerfilDTO, usuarioCadastrado, usuarioCadastrado);
    }

    public UsuarioPerfilDTO editar(UsuarioPerfilDTO usuarioPerfilDTO) {
        UsuarioORM usuarioRecebido = mapper.map(usuarioPerfilDTO.getUsuario(), UsuarioORM.class);

        if (ValidarCPF.validar(usuarioRecebido.getCpf())) {
            throw new CPFInvalidoException("O CPF informado não é válido");
        }

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

        return vincularPerfisAoUsuario(usuarioPerfilDTO, usuarioRecebido, usuarioSalvo);
    }

    // TODO Arrumar o readme pra colocar os JSON certos

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

    public UsuarioORM buscarOrmPorId(Integer id) {
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
