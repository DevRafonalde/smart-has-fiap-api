package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.exceptions.CondicaoInsatisfeitaException;
import br.com.fiap.on.smarthas.model.entities.dto.*;
import br.com.fiap.on.smarthas.model.entities.orm.AvaliacaoORM;
import br.com.fiap.on.smarthas.model.entities.orm.OpcaoQuestaoORM;
import br.com.fiap.on.smarthas.model.entities.orm.QuestaoORM;
import br.com.fiap.on.smarthas.model.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {
    private final AvaliacaoRepository avaliacaoRepository;
    private final QuestaoRepository questaoRepository;
    private final OpcaoQuestaoRepository opcaoQuestaoRepository;
    private final AulaAssistidaRepository aulaAssistidaRepository;
    private final AulaRepository aulaRepository;

    public TentativaAvaliacaoDTO submeterAvaliacao(TentativaAvaliacaoDTO tentativaAvaliacaoDTO) {
        // Concluir módulo se aprovado
        // Se reprovado pela 3 vez, obrigatório tutoria e reinicio do módulo
        return null;
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
