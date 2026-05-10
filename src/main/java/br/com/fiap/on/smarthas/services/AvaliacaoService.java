package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.exceptions.CondicaoInsatisfeitaException;
import br.com.fiap.on.smarthas.exceptions.ElementoNaoEncontradoException;
import br.com.fiap.on.smarthas.model.entities.dto.*;
import br.com.fiap.on.smarthas.model.entities.orm.*;
import br.com.fiap.on.smarthas.model.repositories.*;
import br.com.fiap.on.smarthas.utils.StatusMatricula;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvaliacaoService {
    private final AvaliacaoRepository avaliacaoRepository;
    private final TentativaAvaliacaoRepository tentativaAvaliacaoRepository;
    private final ProgressoModuloRepository progressoModuloRepository;
    private final MatriculaRepository matriculaRepository;
    private final MedalhaRepository medalhaRepository;
    private final MedalhasUsuarioRepository medalhasUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AulaAssistidaRepository aulaAssistidaRepository;
    private final AulaRepository aulaRepository;
    private final QuestaoRepository questaoRepository;
    private final OpcaoQuestaoRepository opcaoQuestaoRepository;
    private final ModelMapper modelMapper;

    // Mnemonico da medalha de conclusão de módulo — deve existir no catálogo (t_medalhas)
    private static final String MEDALHA_MODULO_CONCLUIDO = "modulo_concluido";

    @Transactional
    public TentativaAvaliacaoResponseDTO submeterAvaliacao(TentativaAvaliacaoRequestDTO tentativaAvaliacaoRequestDTO) {
        // 1. Carrega e valida entidades base

        AvaliacaoORM avaliacao = avaliacaoRepository
                .findById(tentativaAvaliacaoRequestDTO.getIdAvaliacao())
                .orElseThrow(() -> new ElementoNaoEncontradoException("Avaliação não encontrada"));

        UsuarioORM aluno = usuarioRepository
                .findById(tentativaAvaliacaoRequestDTO.getIdAluno())
                .orElseThrow(() -> new ElementoNaoEncontradoException("Usuário não encontrado"));

        ModuloORM modulo = avaliacao.getModulo();

        // 2. Verifica se o aluno ainda tem tentativas disponíveis

        List<TentativaAvaliacaoORM> tentativasAnteriores = tentativaAvaliacaoRepository
                .findByAvaliacaoAndAluno(avaliacao, aluno);

        // Bloqueia nova tentativa se já atingiu o máximo E ainda não foi aprovado
        boolean jaAprovado = tentativasAnteriores.stream().anyMatch(TentativaAvaliacaoORM::isAprovado);

        if (jaAprovado) {
            throw new CondicaoInsatisfeitaException("Você já foi aprovado nesta avaliação e não pode submetê-la novamente");
        }

        int tentativasUsadas = tentativasAnteriores.size();
        if (tentativasUsadas >= avaliacao.getMaxTentativas()) {
            throw new CondicaoInsatisfeitaException(String.format(
                    "Número máximo de tentativas atingido (%d/%d). Solicite uma sessão de tutoria para reiniciar",
                    tentativasUsadas, avaliacao.getMaxTentativas()
            ));
        }

        // ── 3. Calcula aprovação com base na nota mínima (RN03) ──────────────

        Integer nota = tentativaAvaliacaoRequestDTO.getNota();
        boolean aprovado = nota >= avaliacao.getPontuacaoMinima();

        // ── 4. Persiste a tentativa ───────────────────────────────────────────

        TentativaAvaliacaoORM novaTentativa = new TentativaAvaliacaoORM();
        novaTentativa.setAvaliacao(avaliacao);
        novaTentativa.setAluno(aluno);
        novaTentativa.setNota(nota);
        novaTentativa.setAprovado(aprovado);
        TentativaAvaliacaoORM tentativaSalva = tentativaAvaliacaoRepository.save(novaTentativa);

        int tentativaAtual = tentativasUsadas + 1;
        log.debug("Tentativa {}/{} registrada para usuário id={}, avaliação id={}, nota={}, aprovado={}",
                tentativaAtual, avaliacao.getMaxTentativas(),
                aluno.getId(), avaliacao.getId(), nota, aprovado);

        // ── 5. Pós-processamento por resultado ────────────────────────────────

        if (aprovado) {
            processarAprovacao(aluno, modulo, tentativaSalva);
        } else {
            processarReprovacao(aluno, modulo, avaliacao, tentativaAtual);
        }

        // ── 6. Monta e retorna o DTO de resposta ─────────────────────────────

        TentativaAvaliacaoResponseDTO resposta = modelMapper.map(tentativaSalva, TentativaAvaliacaoResponseDTO.class);
        resposta.setTentativaAtual(tentativaAtual);
        resposta.setMaxTentativas(avaliacao.getMaxTentativas());
        resposta.setTentativasRestantes(aprovado ? 0 : avaliacao.getMaxTentativas() - tentativaAtual);
        resposta.setTutoriaObrigatoria(
                !aprovado && tentativaAtual >= avaliacao.getMaxTentativas()
        );

        return resposta;
    }

    // ── Aprovação: marca progresso, verifica conclusão do curso e emite medalha ──

    private void processarAprovacao(
            UsuarioORM aluno,
            ModuloORM modulo,
            TentativaAvaliacaoORM tentativa
    ) {
        // Marca o módulo como concluído (RN04: só após aprovação)
        ProgressoModuloORM progresso = progressoModuloRepository
                .findByAlunoAndModulo(aluno, modulo)
                .orElseGet(() -> {
                    // Cria o registro de progresso se ainda não existir
                    ProgressoModuloORM novo = new ProgressoModuloORM();
                    novo.setAluno(aluno);
                    novo.setModulo(modulo);
                    return novo;
                });

        progresso.setConcluido(true);
        progresso.setDataConclusao(LocalDateTime.now());
        progressoModuloRepository.save(progresso);

        log.debug("Módulo id={} marcado como concluído para usuário id={}", modulo.getId(), aluno.getId());

        // Verifica se todos os módulos do curso foram concluídos
        // para atualizar o status da matrícula
        verificarConclusaoCurso(aluno, modulo);

        // Emite a medalha de conclusão de módulo (RN04)
        emitirMedalha(aluno, tentativa);
    }

    // ── Verifica se o curso inteiro foi concluído e atualiza a matrícula ─────

    private void verificarConclusaoCurso(UsuarioORM aluno, ModuloORM modulo) {
        CursoORM curso = modulo.getCurso();

        MatriculaORM matricula = matriculaRepository
                .findByAluno_IdAndCurso_Id(aluno.getId(), curso.getId())
                .orElseThrow(() -> new ElementoNaoEncontradoException("Aluno não tem matrícula no curso " + curso.getTitulo()));

        // Conta quantos módulos ativos o curso tem
        long totalModulos = progressoModuloRepository.countByModuloCursoAndModuloAtivo(curso, true);

        // Conta quantos foram concluídos pelo aluno
        long modulosConcluidos = progressoModuloRepository
                .countByAlunoAndModuloCursoAndConcluidoTrue(aluno, curso);

        if (modulosConcluidos >= totalModulos && totalModulos > 0) {
            matricula.setStatus(StatusMatricula.CONCLUIDA);
            matricula.setDataConclusao(LocalDateTime.now());
            matriculaRepository.save(matricula);
            log.debug("Curso id={} concluído pelo usuário id={}", curso.getId(), aluno.getId());
        }
    }

    // ── Reprovação: verifica se atingiu o limite e obriga tutoria ────────────

    private void processarReprovacao(
            UsuarioORM aluno,
            ModuloORM modulo,
            AvaliacaoORM avaliacao,
            int tentativaAtual
    ) {
        // Última tentativa esgotada (RN03): reinicia o progresso do módulo
        if (tentativaAtual >= avaliacao.getMaxTentativas()) {
            reiniciarProgressoModulo(aluno, modulo);
            log.debug("Tentativas esgotadas para usuário id={} no módulo id={}. Tutoria obrigatória.",
                    aluno.getId(), modulo.getId());
        }
    }

    // ── Reinicia o progresso do módulo após esgotar as tentativas ────────────

    private void reiniciarProgressoModulo(UsuarioORM aluno, ModuloORM modulo) {
        progressoModuloRepository
                .findByAlunoAndModulo(aluno, modulo)
                .ifPresent(progresso -> {
                    progresso.setConcluido(false);
                    progresso.setDataConclusao(null);
                    progressoModuloRepository.save(progresso);
                });
    }

    // ── Emite a medalha de conclusão de módulo (RN04) ─────────────────────────

    private void emitirMedalha(UsuarioORM aluno, TentativaAvaliacaoORM tentativa) {
        medalhaRepository.findByMnemonico(MEDALHA_MODULO_CONCLUIDO)
                .ifPresentOrElse(
                        medalha -> {
                            MedalhasUsuarioORM medalhaUsuario = new MedalhasUsuarioORM();
                            medalhaUsuario.setAluno(aluno);
                            medalhaUsuario.setMedalha(medalha);
                            medalhaUsuario.setTentativaAvaliacao(tentativa);
                            medalhasUsuarioRepository.save(medalhaUsuario);
                            log.debug("Medalha '{}' emitida para usuário id={}", MEDALHA_MODULO_CONCLUIDO, aluno.getId());
                        },
                        () -> log.warn("Medalha com mnemonico '{}' não encontrada no catálogo. " +
                                "Verifique o seeder de medalhas.", MEDALHA_MODULO_CONCLUIDO)
                );
    }

    public AvaliacaoCompletaDTO buscarAvaliacao(Long idModulo, Long idAluno, boolean isAdmin) {
        if (isAdmin) {
            return montarAvaliacao(idModulo);
        }

        long qtdAulasAssistidas = aulaAssistidaRepository.countAulaAssistidaORMByAluno_IdAndAula_Modulo_Id(idAluno, idModulo);
        long qtdAulasTotal = aulaRepository.countByModulo_Id(idModulo);

        if (qtdAulasAssistidas != qtdAulasTotal) {
            throw new CondicaoInsatisfeitaException("Todas as aulas devem ter sido assistidas para que a avaliação possa ser feita");
        }

        return montarAvaliacao(idModulo);
    }

    private AvaliacaoCompletaDTO montarAvaliacao(Long idModulo) {
        AvaliacaoORM avaliacao = avaliacaoRepository.findByModulo_Id(idModulo);
        List<QuestaoORM> questoes = questaoRepository.findByAvaliacao_Id(avaliacao.getId());
        List<OpcaoQuestaoORM> alternativas = opcaoQuestaoRepository.findByQuestao_IdIn(questoes.stream().map(QuestaoORM::getId).toList());

        Map<Long, List<OpcaoQuestaoORM>> alternativasPorQuestao =
                alternativas.stream()
                        .collect(Collectors.groupingBy(o -> o.getQuestao().getId()));

        AvaliacaoCompletaDTO dto = new AvaliacaoCompletaDTO();

        dto.setAvaliacao(new AvaliacaoDTO(
                avaliacao.getId(),
                avaliacao.getMaxTentativas(),
                avaliacao.getPontuacaoMinima()
        ));

        List<QuestaoDTO> questoesDTO = questoes.stream().map(q -> {
            List<OpcaoQuestaoDTO> opcoesDTO =
                    alternativasPorQuestao
                            .getOrDefault(q.getId(), List.of())
                            .stream()
                            .map(o -> new OpcaoQuestaoDTO(
                                    o.getId(),
                                    o.getTexto(),
                                    o.getLinkFoto(),
                                    o.isCorreta()
                            ))
                            .toList();

            return new QuestaoDTO(
                    q.getId(),
                    q.getEnunciado(),
                    q.getLinkFoto(),
                    q.getValor(),
                    opcoesDTO
            );

        }).toList();

        dto.setQuestoes(questoesDTO);

        return dto;
    }
}
