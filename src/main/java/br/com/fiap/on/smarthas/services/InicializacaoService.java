package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.controllers.PerfilController;
import br.com.fiap.on.smarthas.controllers.PermissaoController;
import br.com.fiap.on.smarthas.controllers.UsuarioController;
import br.com.fiap.on.smarthas.model.entities.orm.*;
import br.com.fiap.on.smarthas.model.repositories.*;
import br.com.fiap.on.smarthas.config.PasswordUtil;
import br.com.fiap.on.smarthas.annotations.Permissao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class InicializacaoService {
    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final PermissaoRepository permissaoRepository;
    private final PerfilPermissaoRepository perfilPermissaoRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;

    // Controllers cujos métodos anotados com @Permissao serão escaneados
    private static final List<Class<?>> CONTROLLERS = List.of(
            PerfilController.class,
            PermissaoController.class,
            UsuarioController.class
    );

    @Transactional
    public void startSeeder() {
        // Idempotência: verifica se as permissões já foram criadas
        // Usar a contagem de permissões é mais robusto do que checar entidades
        // individuais — se as permissões existem, o seeder já rodou com sucesso
        long totalPermissoesEsperadas = contarPermissoesEsperadas();
        long totalPermissoesBanco = permissaoRepository.count();

        if (totalPermissoesBanco >= totalPermissoesEsperadas) {
            log.info("Seeder já executado. Pulando inicialização.");
            return;
        }

        log.info("Iniciando seeder de autenticação...");

        List<PermissaoORM> todasPermissoes = criarPermissoes();
        configurarAdmin(todasPermissoes);
        configurarPerfilPadrao(todasPermissoes);

        log.info("Seeder concluído. {} permissões criadas.", todasPermissoes.size());
    }

    // ─── Criação de permissões ─────────────────────────────────────────────────

    // Lê a annotation @Permissao de cada método dos controllers registrados
    // Garante que o nome da permissão é sempre o valor de rota="..." da annotation
    private List<PermissaoORM> criarPermissoes() {
        List<String> rotasUnicas = extrairRotasDosControllers();

        List<PermissaoORM> permissoesCriadas = new ArrayList<>();
        for (String rota : rotasUnicas) {
            // Evita duplicar permissões caso o seeder seja chamado mais de uma vez
            // em um cenário de migração parcial
            boolean jaExiste = permissaoRepository.findByNome(rota).isPresent();
            if (jaExiste) {
                log.debug("Permissão '{}' já existe, pulando.", rota);
                permissoesCriadas.add(permissaoRepository.findByNome(rota).get());
                continue;
            }

            PermissaoORM permissao = new PermissaoORM();
            permissao.setNome(rota);
            permissao.setDescricao("Permissão gerada automaticamente pelo seeder");
            permissao.setAtivo(true);

            permissoesCriadas.add(permissaoRepository.save(permissao));
            log.debug("Permissão criada: '{}'", rota);
        }

        return permissoesCriadas;
    }

    // Escaneia os controllers e extrai o valor de rota="..." de cada @Permissao
    // Já filtra duplicatas com distinct()
    private List<String> extrairRotasDosControllers() {
        return CONTROLLERS.stream()
                .flatMap(controller -> Arrays.stream(controller.getDeclaredMethods()))
                .map(method -> method.getAnnotation(Permissao.class))
                .filter(Objects::nonNull)                      // ignora métodos sem @Permissao
                .map(Permissao::rota)
                .map(String::toLowerCase)
                .distinct()                                    // remove duplicatas entre controllers
                .sorted()                                      // ordem alfabética para log legível
                .toList();
    }

    // Conta quantas permissões únicas são esperadas — usado na verificação de idempotência
    private long contarPermissoesEsperadas() {
        return extrairRotasDosControllers().size();
    }

    // ─── Configuração do admin ─────────────────────────────────────────────────

    private void configurarAdmin(List<PermissaoORM> todasPermissoes) {
        if (Objects.nonNull(usuarioRepository.findByNomeUser("admin"))) {
            log.info("Usuário admin já existe. Pulando.");
            return;
        }

        UsuarioORM admin = criarUsuarioAdmin();
        PerfilORM perfilAdmin = criarPerfil("Administrador de Sistemas", "admin",
                "Perfil com acesso total ao sistema");

        // Admin recebe TODAS as permissões
        vincularPermissoesAoPerfil(perfilAdmin, todasPermissoes);
        vincularUsuarioAoPerfil(admin, perfilAdmin);

        log.info("Usuário admin criado e configurado com {} permissões.", todasPermissoes.size());
    }

    // ─── Controllers acessíveis pelo perfil padrão ────────────────────────────
    // Para dar acesso a um novo controller ao perfil padrão, basta adicioná-lo aqui.
    // Permissões cujo nome começa com "admin" são sempre excluídas,
    // independente do controller listado.
    private static final List<Class<?>> CONTROLLERS_PADRAO = List.of(
            PerfilController.class,
            PermissaoController.class,
            UsuarioController.class
    );

    // ─── Configuração do perfil padrão ─────────────────────────────────────────

    private void configurarPerfilPadrao(List<PermissaoORM> todasPermissoes) {
        if (Objects.nonNull(perfilRepository.findByMnemonico("padrao"))) {
            log.info("Perfil padrão já existe. Pulando.");
            return;
        }

        PerfilORM perfilPadrao = criarPerfil("Usuário Padrão", "padrao",
                "Perfil com permissões padrões do sistema");

        // Extrai as rotas permitidas a partir dos controllers definidos em CONTROLLERS_PADRAO,
        // excluindo qualquer permissão cujo nome comece com "admin"
        List<String> rotasPermitidas = CONTROLLERS_PADRAO.stream()
                .flatMap(controller -> Arrays.stream(controller.getDeclaredMethods()))
                .map(method -> method.getAnnotation(Permissao.class))
                .filter(Objects::nonNull)
                .map(Permissao::rota)
                .map(String::toLowerCase)
                .filter(rota -> !rota.startsWith("admin"))
                .distinct()
                .toList();

        // Cruza com as permissões já persistidas no banco para pegar os ORM corretos
        List<PermissaoORM> permissoesPadrao = todasPermissoes.stream()
                .filter(p -> rotasPermitidas.contains(p.getNome()))
                .toList();

        vincularPermissoesAoPerfil(perfilPadrao, permissoesPadrao);

        log.info("Perfil padrão criado com {} permissões.", permissoesPadrao.size());
    }

    // ─── Métodos auxiliares ────────────────────────────────────────────────────

    private UsuarioORM criarUsuarioAdmin() {
        UsuarioORM admin = new UsuarioORM();
        admin.setNomeCompleto("Administrador");
        admin.setNomeAmigavel("Administrador");
        admin.setNomeUser("admin");
        admin.setSenhaUser(PasswordUtil.hashPassword("123456"));
        admin.setSenhaAtualizada(true);
        admin.setAtivo(true);
        return usuarioRepository.save(admin);
    }

    private PerfilORM criarPerfil(String nome, String mnemonico, String descricao) {
        PerfilORM perfil = new PerfilORM();
        perfil.setNome(nome);
        perfil.setMnemonico(mnemonico);
        perfil.setDescricao(descricao);
        perfil.setAtivo(true);
        return perfilRepository.save(perfil);
    }

    private void vincularPermissoesAoPerfil(PerfilORM perfil, List<PermissaoORM> permissoes) {
        for (PermissaoORM permissao : permissoes) {
            PerfilPermissaoORM vinculo = new PerfilPermissaoORM();
            vinculo.setPerfil(perfil);
            vinculo.setPermissao(permissao);
            vinculo.setDataHora(LocalDateTime.now());
            perfilPermissaoRepository.save(vinculo);
        }
    }

    private void vincularUsuarioAoPerfil(UsuarioORM usuario, PerfilORM perfil) {
        UsuarioPerfilORM vinculo = new UsuarioPerfilORM();
        vinculo.setUsuario(usuario);
        vinculo.setPerfil(perfil);
        vinculo.setDataHora(LocalDateTime.now());
        usuarioPerfilRepository.save(vinculo);
    }
}