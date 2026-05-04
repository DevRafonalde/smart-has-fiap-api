package br.com.fiap.on.smarthas.controllers;

import br.com.fiap.on.smarthas.annotations.Permissao;
import br.com.fiap.on.smarthas.model.entities.dto.PerfilDTO;
import br.com.fiap.on.smarthas.model.entities.dto.PerfilPermissaoDTO;
import br.com.fiap.on.smarthas.model.entities.dto.PerfilUsuarioDTO;
import br.com.fiap.on.smarthas.services.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/perfis")
@RequiredArgsConstructor
public class PerfilController {
    private PerfilService perfilService;

    @PostMapping("/cadastrar")
    @Permissao(rota = "cadastrarperfil")
    public ResponseEntity<PerfilPermissaoDTO> cadastrarPerfil(@RequestBody PerfilPermissaoDTO perfilPermissaoDTO) {
        PerfilPermissaoDTO perfilCadastrado = perfilService.novoPerfil(perfilPermissaoDTO);

        return ResponseEntity.ok(perfilCadastrado);
    }

    @GetMapping("/listar")
    @Permissao(rota = "listartodosperfis")
    public ResponseEntity<List<PerfilDTO>> listarTodosPerfis(Pageable pageable) {
        List<PerfilDTO> perfis = perfilService.listarTodos(pageable);

        return ResponseEntity.ok(perfis);
    }

    @GetMapping("/listar-usuarios-vinculados/{id}")
    @Permissao(rota = "listarusuariosvinculados")
    public ResponseEntity<PerfilUsuarioDTO> listarUsuariosVinculados(@PathVariable Long id) {
        PerfilUsuarioDTO perfilUsuarioDTO = perfilService.listarUsuariosVinculados(id);

        return ResponseEntity.ok(perfilUsuarioDTO);
    }

//    @GetMapping("/vincular-usuarios-em-lote/{id}")
//    public String vincularUsuariosEmLote(@PathVariable Long id, ModelMap modelMap) {
//        PerfilUsuarioDTO modeloCadastroPerfilUsuario = perfilService.listarUsuariosVinculados(id);
//        modelMap.addAttribute("modeloCadastroPerfilUsuario", modeloCadastroPerfilUsuario);
//        return "perfis/usuarios-em-lote";
//    }

    @GetMapping("/listar-especifico/{id}")
    @Permissao(rota = "listarperfilespecifico")
    public ResponseEntity<PerfilPermissaoDTO> listarPerfilEspecifico(@PathVariable Long id) {
        PerfilPermissaoDTO perfilPermissao = perfilService.listarEspecifico(id);

        return ResponseEntity.ok(perfilPermissao);
    }

    @GetMapping("/clonar/{id}")
    @Permissao(rota = "clonarperfil")
    public ResponseEntity<PerfilPermissaoDTO> clonarPerfil(@PathVariable Long id) {
        PerfilPermissaoDTO perfilPermissaoDTO = perfilService.clonar(id);

        return ResponseEntity.ok(perfilPermissaoDTO);
    }

    @DeleteMapping("/deletar/{id}")
    @Permissao(rota = "deletarperfil")
    public ResponseEntity<Boolean> deletarPerfil(@PathVariable Long id) {
        perfilService.deletar(id);

        return ResponseEntity.ok(true);
    }

    @PutMapping("/editar")
    @Permissao(rota = "editarperfil")
    public ResponseEntity<PerfilPermissaoDTO> editarPerfil(@RequestBody @Valid PerfilPermissaoDTO perfilPermissaoRecebido) {
        PerfilPermissaoDTO perfilPermissaoAtualizado = perfilService.editar(perfilPermissaoRecebido);

        return ResponseEntity.ok(perfilPermissaoAtualizado);
    }

//    @PostMapping("/vincular-usuarios-em-lote")
//    public ResponseEntity<Integer> vincularUsuariosEmLotePost(@RequestBody ModeloCadastroPerfilUsuarioId modeloCadastroPerfilUsuarioId, RedirectAttributes attributes) {
//        ModeloCadastroPerfilUsuarioId modeloRetorno = perfilService.vincularUsuariosEmLote(modeloCadastroPerfilUsuarioId);
//        attributes.addFlashAttribute("sucesso", "Usuários vinculados com sucesso");
//        return ResponseEntity.ok(modeloRetorno.getPerfil().getId());
//    }
}
