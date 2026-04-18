package br.com.fiap.on.smarthas.auth.api.controllers;

import br.com.fiap.on.smarthas.auth.internal.models.entities.dto.PerfilDTO;
import br.com.fiap.on.smarthas.auth.internal.models.entities.dto.PerfilPermissaoDTO;
import br.com.fiap.on.smarthas.auth.internal.models.entities.dto.PerfilUsuarioDTO;
import br.com.fiap.on.smarthas.auth.internal.services.PerfilService;
import br.com.fiap.on.smarthas.shared.annotations.Permissao;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/perfis")
public class PerfilController {

    @Autowired
    private PerfilService perfilService;

    @PostMapping("/cadastrar")
    @Permissao(rota = "cadastrarperfil")
    public ResponseEntity<PerfilPermissaoDTO> cadastrarPerfil(@RequestBody PerfilPermissaoDTO perfilPermissaoDTO) {
        PerfilPermissaoDTO perfilCadastrado = perfilService.novoPerfil(perfilPermissaoDTO);

        return new ResponseEntity<>(perfilCadastrado, HttpStatus.OK);
    }

    @GetMapping("/listar")
    @Permissao(rota = "listartodosperfis")
    public ResponseEntity<List<PerfilDTO>> listarTodosPerfis(Pageable pageable) {
        List<PerfilDTO> perfis = perfilService.listarTodos(pageable);

        return new ResponseEntity<>(perfis, HttpStatus.OK);
    }

    @GetMapping("/listar-usuarios-vinculados/{id}")
    @Permissao(rota = "listarusuariosvinculados")
    public ResponseEntity<PerfilUsuarioDTO> listarUsuariosVinculados(@PathVariable Integer id) {
        PerfilUsuarioDTO perfilUsuarioDTO = perfilService.listarUsuariosVinculados(id);

        return new ResponseEntity<>(perfilUsuarioDTO, HttpStatus.OK);
    }

//    @GetMapping("/vincular-usuarios-em-lote/{id}")
//    public String vincularUsuariosEmLote(@PathVariable Integer id, ModelMap modelMap) {
//        PerfilUsuarioDTO modeloCadastroPerfilUsuario = perfilService.listarUsuariosVinculados(id);
//        modelMap.addAttribute("modeloCadastroPerfilUsuario", modeloCadastroPerfilUsuario);
//        return "perfis/usuarios-em-lote";
//    }

    @GetMapping("/listar-especifico/{id}")
    @Permissao(rota = "listarperfilespecifico")
    public ResponseEntity<PerfilPermissaoDTO> listarPerfilEspecifico(@PathVariable Integer id) {
        PerfilPermissaoDTO perfilPermissao = perfilService.listarEspecifico(id);

        return new ResponseEntity<>(perfilPermissao, HttpStatus.OK);
    }

    @GetMapping("/clonar/{id}")
    @Permissao(rota = "clonarperfil")
    public ResponseEntity<PerfilPermissaoDTO> clonarPerfil(@PathVariable Integer id) {
        PerfilPermissaoDTO perfilPermissaoDTO = perfilService.clonar(id);

        return new ResponseEntity<>(perfilPermissaoDTO, HttpStatus.OK);
    }

    @DeleteMapping("/deletar/{id}")
    @Permissao(rota = "deletarperfil")
    public ResponseEntity<Boolean> deletarPerfil(@PathVariable Integer id) {
        perfilService.deletar(id);

        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PutMapping("/editar")
    @Permissao(rota = "editarperfil")
    public ResponseEntity<PerfilPermissaoDTO> editarPerfil(@RequestBody @Valid PerfilPermissaoDTO perfilPermissaoRecebido) {
        PerfilPermissaoDTO perfilPermissaoAtualizado = perfilService.editar(perfilPermissaoRecebido);

        return new ResponseEntity<>(perfilPermissaoAtualizado, HttpStatus.OK);
    }

//    @PostMapping("/vincular-usuarios-em-lote")
//    public ResponseEntity<Integer> vincularUsuariosEmLotePost(@RequestBody ModeloCadastroPerfilUsuarioId modeloCadastroPerfilUsuarioId, RedirectAttributes attributes) {
//        ModeloCadastroPerfilUsuarioId modeloRetorno = perfilService.vincularUsuariosEmLote(modeloCadastroPerfilUsuarioId);
//        attributes.addFlashAttribute("sucesso", "Usuários vinculados com sucesso");
//        return new ResponseEntity<>(modeloRetorno.getPerfil().getId(), HttpStatus.OK);
//    }
}
