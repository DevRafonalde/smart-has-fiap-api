package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.model.entities.dto.AulaDTO;
import br.com.fiap.on.smarthas.model.entities.dto.ModuloCompletoDTO;
import br.com.fiap.on.smarthas.model.entities.dto.ModuloDTO;
import br.com.fiap.on.smarthas.model.repositories.AulaRepository;
import br.com.fiap.on.smarthas.model.repositories.ModuloRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuloService {
    private final ModuloRepository moduloRepository;
    private final AulaRepository aulaRepository;
    private final ModelMapper modelMapper;

    public List<ModuloDTO> buscarModulosPorCurso(Long id) {
        return moduloRepository.findByCurso_Id(id).stream()
                .map(moduloORM -> modelMapper.map(moduloORM, ModuloDTO.class))
                .toList();
    }

    public ModuloCompletoDTO buscarModuloPorId(Long id) {
        return new ModuloCompletoDTO(
                modelMapper.map(moduloRepository.findById(id), ModuloDTO.class),
                aulaRepository.findByModulo_Id(id).stream()
                        .map(aulaORM -> modelMapper.map(aulaORM, AulaDTO.class))
                        .toList()
        );
    }
}
