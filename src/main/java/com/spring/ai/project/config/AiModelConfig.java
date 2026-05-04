package com.spring.ai.project.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

;

@Configuration
public class AiModelConfig {

    @Bean(name = "primaryClient")
    public ChatClient primaryClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("You are a strict Java code generator")
                .build(); // uses Ollama (default)
    }

    @Bean(name = "backupClient")
    public ChatClient backupClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("You are a strict Java code generator")
                .build(); // will override via config (DeepSeek)
    }
}
