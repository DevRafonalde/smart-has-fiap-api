package br.com.fiap.on.smarthas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync  // habilita o @Async em toda a aplicação
public class AsyncConfig {

    // Pool dedicado para envio de e-mails
    // Evita que falhas ou lentidão no SMTP afetem o pool principal do Tomcat
    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);       // threads sempre ativas
        executor.setMaxPoolSize(5);        // máximo em picos
        executor.setQueueCapacity(50);     // fila antes de rejeitar
        executor.setThreadNamePrefix("email-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
