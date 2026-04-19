package br.com.fiap.on.smarthas.config;

import br.com.fiap.on.smarthas.auth.internal.services.InicializacaoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupRunner {
    @Bean
    public CommandLineRunner init(InicializacaoService inicializacaoService) {
        return args -> {
            inicializacaoService.startSeeder();
        };
    }
}
