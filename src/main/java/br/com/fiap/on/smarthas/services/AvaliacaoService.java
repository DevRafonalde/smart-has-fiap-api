package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.model.entities.dto.TentativaAvaliacaoDTO;
import org.springframework.stereotype.Service;

@Service
public class AvaliacaoService {
    public TentativaAvaliacaoDTO submeterAvaliacao(TentativaAvaliacaoDTO tentativaAvaliacaoDTO) {
        // Concluir módulo se aprovado
        // Se reprovado pela 3 vez, obrigatório tutoria e reinicio do módulo
        return null;
    }
}
