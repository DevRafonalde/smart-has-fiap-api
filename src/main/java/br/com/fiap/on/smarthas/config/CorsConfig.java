package br.com.fiap.on.smarthas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // libera todas as rotas
                        .allowedOriginPatterns("*") // aceita qualquer origem
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // libera métodos
                        .allowedHeaders("*") // libera todos os headers
                        .allowCredentials(true); // permite credenciais (cookies, Authorization header, etc.)
            }
        };
    }
}
