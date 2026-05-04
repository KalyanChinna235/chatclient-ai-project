package com.spring.ai.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient deepseekClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:11435") // DeepSeek / Ollama second instance
                .build();
    }
}