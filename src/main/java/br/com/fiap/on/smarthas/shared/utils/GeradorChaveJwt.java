package br.com.fiap.on.smarthas.shared.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class GeradorChaveJwt {
    // Executa ao iniciar a aplicação
    @PostConstruct
    public void aoIniciar() {
        try {
            gerarChaveSecreta();
        } catch (IOException e) {
            System.err.println("Erro ao gerar chave na inicialização: " + e.getMessage());
        }
    }

    // Executa uma vez por dia (à meia-noite, por exemplo)
    @Scheduled(cron = "0 0 0 * * *") // formato: segundo minuto hora dia mes dia-da-semana
    public void agendadoDiariamente() {
        try {
            gerarChaveSecreta();
            System.out.println("Chave regenerada automaticamente.");
        } catch (IOException e) {
            System.err.println("Erro ao regenerar chave: " + e.getMessage());
        }
    }

    public void gerarChaveSecreta() throws IOException {
        byte[] chave = new byte[32];
        new SecureRandom().nextBytes(chave);
        String chaveCodificada = Base64.getEncoder().encodeToString(chave);

        String caminhoPasta = "./jwt";
        String nomeArquivo = "jwt_secret_key.txt";

        File pasta = new File(caminhoPasta);
        if (!pasta.exists() && !pasta.mkdirs()) {
            throw new IOException("Falha ao criar diretório: " + caminhoPasta);
        }

        File arquivo = new File(pasta, nomeArquivo);
        try (FileOutputStream fos = new FileOutputStream(arquivo)) {
            fos.write(chaveCodificada.getBytes());
        }

        System.out.println("Chave gerada com sucesso em: " + arquivo.getAbsolutePath());
    }
}

