package br.com.fiap.on.smarthas.config;

import br.com.fiap.on.smarthas.auth.api.middlewares.PermissaoMiddleware;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {
    private PermissaoMiddleware permissaoMiddleware;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissaoMiddleware);
    }
}

