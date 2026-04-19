package br.com.fiap.on.smarthas.auth.api.middlewares;

import br.com.fiap.on.smarthas.auth.internal.models.entities.dto.UsuarioPerfilDTO;
import br.com.fiap.on.smarthas.auth.internal.services.JwtService;
import br.com.fiap.on.smarthas.auth.internal.services.PerfilService;
import br.com.fiap.on.smarthas.auth.internal.services.UsuarioService;
import br.com.fiap.on.smarthas.shared.annotations.Permissao;
import br.com.fiap.on.smarthas.shared.exceptions.AcessoNaoAutorizadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@AllArgsConstructor
@RequiredArgsConstructor
public class PermissaoMiddleware implements HandlerInterceptor {
    private JwtService jwtService;
    private UsuarioService usuarioService;
    private PerfilService perfilService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Só intercepta se for método de controller
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // Verifica se o método tem a annotation @Permissao
        Permissao permissaoAnnotation = handlerMethod.getMethodAnnotation(Permissao.class);
        if (permissaoAnnotation == null) {
            System.out.println("Rota pública");
            // Não tem annotation → rota pública
            return true;
        }

        // Caso tenha annotation, extrai a rota exigida
        String rotaNecessaria = permissaoAnnotation.rota();

        // Valida o token JWT
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            System.out.println("Sem Token");
            throw new AcessoNaoAutorizadoException("Não foi enviado o Token de Autorização");
        }

        token = token.substring(7);

        int idUsuario;
        try {
            idUsuario = jwtService.validarTokenERetornarId(token);
        } catch (Exception e) {
            throw new AcessoNaoAutorizadoException("Token inválido, Exceção: " + e.getMessage());
        }

        // Carrega o usuário e perfis
        UsuarioPerfilDTO usuario = usuarioService.listarEspecifico(idUsuario);
        if (usuario == null) {
            throw new AcessoNaoAutorizadoException("Usuário do token não encontrado");
        }

        // Verifica se o usuário possui a permissão exigida
        boolean autorizado = usuario.getPerfisUsuario().stream()
                .anyMatch(perfilDTO -> perfilService.listarEspecifico(perfilDTO.getId())
                        .getPermissoes()
                        .stream()
                        .anyMatch(permissao -> permissao.getNome().equalsIgnoreCase(rotaNecessaria))
                );

        if (!autorizado) {
            throw new AcessoNaoAutorizadoException("Usuário não possui a permissão necessária para esta função");
        }

        System.out.println("Passou pelo Middleware");
        return true;
    }
}
