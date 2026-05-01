package br.com.fiap.on.smarthas.services;

import br.com.fiap.on.smarthas.model.entities.orm.UsuarioORM;
import br.com.fiap.on.smarthas.model.entities.orm.VerificacaoEmailORM;
import br.com.fiap.on.smarthas.model.repositories.UsuarioRepository;
import br.com.fiap.on.smarthas.model.repositories.VerificacaoEmailRepository;
import br.com.fiap.on.smarthas.model.template.EmailVerificacaoTemplate;
import br.com.fiap.on.smarthas.exceptions.AcessoNaoAutorizadoException;
import br.com.fiap.on.smarthas.exceptions.ElementoNaoEncontradoException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificacaoEmailService {
    private final VerificacaoEmailRepository verificacaoEmailRepository;
    private final UsuarioRepository usuarioRepository;
    private final JavaMailSender mailSender;

    // Lido do application.yml — remetente configurado no SMTP
    @Value("${spring.mail.username}")
    private String remetente;

    // Expiração do código em minutos — configurável pelo application.yml
    @Value("${smarthas.email.verificacao.expiracao-minutos:15}")
    private int expiracaoMinutos;

    // ─── Geração e envio ──────────────────────────────────────────────────────

    // Gera um código, invalida os anteriores e envia o e-mail
    // @Async garante que o envio não bloqueie a thread da requisição de registro
    @Async
    @Transactional
    public void enviarCodigoVerificacao(Integer idUsuario) {
        UsuarioORM usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ElementoNaoEncontradoException("Usuário não encontrado"));

        // Invalida todos os códigos anteriores antes de gerar um novo
        invalidarCodigosAnteriores(usuario);

        String codigo = gerarCodigo();
        LocalDateTime expiracao = LocalDateTime.now().plusMinutes(expiracaoMinutos);

        VerificacaoEmailORM verificacao = new VerificacaoEmailORM(usuario, codigo, expiracao);
        verificacaoEmailRepository.save(verificacao);

        enviarEmail(usuario, codigo);

        log.debug("Código de verificação enviado para usuário id={}", idUsuario);
    }

    // ─── Verificação do código ─────────────────────────────────────────────────

    @Transactional
    public void verificar(Integer idUsuario, String codigoRecebido) {
        UsuarioORM usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ElementoNaoEncontradoException("Usuário não encontrado"));

        VerificacaoEmailORM verificacao = verificacaoEmailRepository
                .findFirstByUsuarioAndUtilizadoFalseOrderByCriadoEmDesc(usuario)
                .orElseThrow(() -> new AcessoNaoAutorizadoException("Nenhum código de verificação pendente para este usuário"));

        if (verificacao.estaExpirado()) {
            // Marca como utilizado para manter histórico e forçar nova solicitação
            verificacao.setUtilizado(true);
            verificacaoEmailRepository.save(verificacao);
            throw new AcessoNaoAutorizadoException("Código de verificação expirado. Solicite um novo código");
        }

        if (!verificacao.getCodigo().equals(codigoRecebido)) {
            throw new AcessoNaoAutorizadoException("Código de verificação incorreto");
        }

        // Código válido — marca como utilizado e ativa o e-mail do usuário
        verificacao.setUtilizado(true);
        verificacaoEmailRepository.save(verificacao);

        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        log.debug("E-mail verificado com sucesso para usuário id={}", idUsuario);
    }

    // ─── Reenvio ──────────────────────────────────────────────────────────────

    // Permite ao usuário solicitar um novo código caso o anterior tenha expirado
    @Async
    @Transactional
    public void reenviarCodigo(Integer idUsuario) {
        enviarCodigoVerificacao(idUsuario);
        log.debug("Código de verificação reenviado para usuário id={}", idUsuario);
    }

    // ─── Métodos privados ──────────────────────────────────────────────────────

    private String gerarCodigo() {
        // Gera um número aleatório entre 100000 e 999999 (sempre 6 dígitos)
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private void invalidarCodigosAnteriores(UsuarioORM usuario) {
        List<VerificacaoEmailORM> anteriores = verificacaoEmailRepository.findByUsuario(usuario);
        anteriores.forEach(v -> v.setUtilizado(true));
        verificacaoEmailRepository.saveAll(anteriores);
    }

    private void enviarEmail(UsuarioORM usuario, String codigo) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

            helper.setFrom(remetente, "SmartHAS · Digital 360");
            helper.setTo(usuario.getEmail());
            helper.setSubject("Seu código de verificação — SmartHAS Digital 360");

            // HTML gerado pelo template estático
            String htmlContent = EmailVerificacaoTemplate.gerar(usuario.getNomeAmigavel(), codigo);
            helper.setText(htmlContent, true); // true = isHtml

            mailSender.send(mensagem);

            log.debug("E-mail de verificação enviado para {}", usuario.getEmail());

        } catch (MessagingException e) {
            log.error("Falha ao enviar e-mail de verificação para usuário id={}: {}", usuario.getId(), e.getMessage());
            // Não lança exceção para não bloquear o fluxo de registro —
            // o usuário pode solicitar reenvio depois
        } catch (Exception e) {
            log.error("Erro inesperado ao enviar e-mail para usuário id={}: {}", usuario.getId(), e.getMessage());
        }
    }
}