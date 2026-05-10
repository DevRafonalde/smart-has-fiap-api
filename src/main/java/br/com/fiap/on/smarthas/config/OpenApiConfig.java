package br.com.fiap.on.smarthas.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SmartHAS — Digital 360 API",
                version = "v1",
                description = "API REST da Plataforma de Inclusão Digital Comunitária. " +
                        "Endpoints marcados com 🔒 exigem autenticação via JWT Bearer Token. " +
                        "Use o botão **Authorize** para informar o token obtido em `/auth/usuarios/login`.",
                contact = @Contact(
                        name = "Equipe SmartHAS — FIAP",
                        url = "https://github.com/DevRafonalde/smart-has-fiap-api"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080/api", description = "Desenvolvimento local"),
                @Server(url = "https://api.smarthas.com.br/api", description = "Produção")
        }
)
// Define o esquema de segurança "bearerAuth" referenciado em todos os @SecurityRequirement
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtido em POST /auth/usuarios/login. " +
                "Informe apenas o token — o prefixo 'Bearer ' é adicionado automaticamente."
)
public class OpenApiConfig {
    // Configuração via annotations — nenhum bean adicional necessário
}