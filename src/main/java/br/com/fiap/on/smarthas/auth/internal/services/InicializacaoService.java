package br.com.fiap.on.smarthas.auth.internal.services;

import br.com.fiap.on.smarthas.auth.api.controllers.PerfilController;
import br.com.fiap.on.smarthas.auth.api.controllers.PermissaoController;
import br.com.fiap.on.smarthas.auth.api.controllers.UsuarioController;
import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.*;
import br.com.fiap.on.smarthas.auth.internal.models.repositories.*;
import br.com.fiap.on.smarthas.config.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InicializacaoService {
    private PerfilRepository perfilRepository;
    private UsuarioRepository usuarioRepository;
    private PermissaoRepository permissaoRepository;
    private PerfilPermissaoRepository perfilPermissaoRepository;
    private UsuarioPerfilRepository usuarioPerfilRepository;

    public void startSeeder() {
        if (Objects.nonNull(usuarioRepository.findByNomeUser("admin"))) {
            return;
        }

        List<PermissaoORM> permissoesCriadas = criarPermissoes();

        configurarUsuarioAdmin(permissoesCriadas);
        configurarPerfilPadrao(permissoesCriadas);
    }

    private void configurarUsuarioAdmin(List<PermissaoORM> permissoesCriadas) {
        UsuarioORM usuarioCriado = criarUsuarioAdmin();
        PerfilORM perfilAdmin = criarPerfilAdmin();
        vincularPerfisPermissoes(perfilAdmin, permissoesCriadas);
        vincularUsuariosPerfis(usuarioCriado, perfilAdmin);
    }

    private void configurarPerfilPadrao(List<PermissaoORM> permissoesCriadas) {
        PerfilORM perfilPadrao = criarPerfilPadrao();

        // TODO Trocar o Controller abaixo pelos controllers da aplicação mesmo
        List<String> permissoesPerfil = Arrays.stream(PerfilController.class.getDeclaredMethods())
                .map(Method::getName)
                .map(String::toLowerCase)
                .toList();

        List<String> permissoesNecessarias = new ArrayList<>();
        permissoesNecessarias.addAll(permissoesPerfil);

        List<PermissaoORM> permissoesPadrao = new ArrayList<>();
        for (PermissaoORM permissao : permissoesCriadas) {
            if (permissoesNecessarias.contains(permissao.getNome())) {
                permissoesPadrao.add(permissao);
            }
        }

        vincularPerfisPermissoes(perfilPadrao, permissoesPadrao);
    }

    private UsuarioORM criarUsuarioAdmin() {
        UsuarioORM admin = new UsuarioORM();
        admin.setNomeCompleto("Administrador");
        admin.setNomeAmigavel("Administrador");
        admin.setNomeUser("admin");
        admin.setSenhaUser(PasswordUtil.hashPassword("123456"));
        admin.setSenhaAtualizada(true);

        return usuarioRepository.save(admin);
    }

    private PerfilORM criarPerfilAdmin() {
        PerfilORM admin = new PerfilORM();
        admin.setNome("Administrador de Sistemas");
        admin.setMnemonico("admin");
        admin.setDescricao("Perfil com todas as permissões do sistema");

        return perfilRepository.save(admin);
    }

    private PerfilORM criarPerfilPadrao() {
        PerfilORM padrao = new PerfilORM();
        padrao.setNome("Perfil de Usuário Padrão");
        padrao.setMnemonico("padrao");
        padrao.setDescricao("Perfil com todas as permissões padrões do sistema");

        return perfilRepository.save(padrao);
    }

    // Esse método precisa criar TODAS as permissões da aplicação
    private List<PermissaoORM> criarPermissoes() {
        List<PermissaoORM> permissoesCriadas = new ArrayList<>();
        List<String> permissoesPerfil = Arrays.stream(PerfilController.class.getDeclaredMethods())
                .map(Method::getName)
                .map(String::toLowerCase)
                .toList();

        List<String> permissoesUsuarios = Arrays.stream(UsuarioController.class.getDeclaredMethods())
                .map(Method::getName)
                .map(String::toLowerCase)
                .toList();

        List<String> permissoesPermissoes = Arrays.stream(PermissaoController.class.getDeclaredMethods())
                .map(Method::getName)
                .map(String::toLowerCase)
                .toList();

        List<String> todasPermissoes = new ArrayList<>();
        todasPermissoes.addAll(permissoesPerfil);
        todasPermissoes.addAll(permissoesUsuarios);
        todasPermissoes.addAll(permissoesPermissoes);

        int contadorId = 1;
        for (String nomePermissao : todasPermissoes) {
            PermissaoORM novaPermissao = new PermissaoORM();
            novaPermissao.setId(contadorId++);
            novaPermissao.setNome(nomePermissao);

            PermissaoORM permissaoCriada = permissaoRepository.save(novaPermissao);

            permissoesCriadas.add(permissaoCriada);
        }

        return permissoesCriadas;
    }

    private void vincularPerfisPermissoes(PerfilORM perfilCriado, List<PermissaoORM> permissoesCriadas) {
        for (PermissaoORM permissao : permissoesCriadas) {
            PerfilPermissaoORM perfilPermissao = new PerfilPermissaoORM();
            perfilPermissao.setPerfil(perfilCriado);
            perfilPermissao.setPermissao(permissao);
            perfilPermissao.setDataHora(LocalDateTime.now());

            perfilPermissaoRepository.save(perfilPermissao);
        }
    }

    private void vincularUsuariosPerfis(UsuarioORM usuarioCriado, PerfilORM perfilCriado) {
        UsuarioPerfilORM usuarioPerfil = new UsuarioPerfilORM();
        usuarioPerfil.setUsuario(usuarioCriado);
        usuarioPerfil.setPerfil(perfilCriado);
        usuarioPerfil.setDataHora(LocalDateTime.now());

        usuarioPerfilRepository.save(usuarioPerfil);
    }
}
