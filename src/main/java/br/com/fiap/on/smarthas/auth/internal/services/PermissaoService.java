package br.com.fiap.on.smarthas.auth.internal.services;

import br.com.fiap.on.smarthas.auth.internal.models.entities.dto.PermissaoDTO;
import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.PerfilPermissaoORM;
import br.com.fiap.on.smarthas.auth.internal.models.entities.orm.PermissaoORM;
import br.com.fiap.on.smarthas.auth.internal.models.repositories.PerfilPermissaoRepository;
import br.com.fiap.on.smarthas.auth.internal.models.repositories.PermissaoRepository;
import br.com.fiap.on.smarthas.shared.exceptions.ElementoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissaoService {
    private PermissaoRepository permissaoRepository;
    private PerfilPermissaoRepository perfilPermissaoRepository;
    private ModelMapper mapper;

    public List<PermissaoDTO> listarTodas(Pageable pageable) {
        Page<PermissaoORM> permissoes = permissaoRepository.findAll(pageable);
        return permissoes.stream()
                .map(permissao -> mapper.map(permissao, PermissaoDTO.class))
                .toList();
    }

    public PermissaoDTO listarPorId(Integer id) {
        PermissaoORM permissaoORM = permissaoRepository.findById(id).orElseThrow(() -> new ElementoNaoEncontradoException("Não foi encontrada nenhuma permissão com os parâmetros enviados"));

        return mapper.map(permissaoORM, PermissaoDTO.class);
    }

    public void deletar(int id) {
        PermissaoORM permissaoDelete = permissaoRepository.findById(id).orElseThrow(() -> new ElementoNaoEncontradoException("Permissão não encontrada no banco de dados"));

        List<PerfilPermissaoORM> usosPermissao = perfilPermissaoRepository.findByPermissao(permissaoDelete);
        if (!usosPermissao.isEmpty()) {
            perfilPermissaoRepository.deleteAll(usosPermissao);
        }

        permissaoRepository.delete(permissaoDelete);
    }

    public PermissaoDTO novaPermissao(PermissaoDTO permissao) {
        PermissaoORM permissaoRecebida = mapper.map(permissao, PermissaoORM.class);
        PermissaoORM permissaoCriada = permissaoRepository.save(permissaoRecebida);

        return mapper.map(permissaoCriada, PermissaoDTO.class);
    }

    public PermissaoDTO editar(PermissaoDTO permissao) {
        permissaoRepository.findById(permissao.getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Permissão não encontrada no banco de dados"));

        PermissaoORM permissaoRecebida = mapper.map(permissao, PermissaoORM.class);
        PermissaoORM permissaoAtualizada = permissaoRepository.save(permissaoRecebida);

        return mapper.map(permissaoAtualizada, PermissaoDTO.class);
    }
}
