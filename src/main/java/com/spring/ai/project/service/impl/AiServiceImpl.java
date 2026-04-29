package com.spring.ai.project.service.impl;

import com.spring.ai.project.dto.RespnseStructure;
import com.spring.ai.project.service.AiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;

    public AiServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public RespnseStructure askToAi(String question) {
        String prompt = buildPrompt(question);
        return executeWithRetry(prompt);
    }

    // ✅ Prompt Builder
    private String buildPrompt(String question) {
        return """
        You are a backend API.

        Return ONLY valid JSON.

        Format:
        {
          "title": "...",
          "content": "...",
          "description": "...",
          "createdYear": "YYYY"
        }

        STRICT RULES:
        - Language MUST be Java
        - Class name must match title (no spaces)
        - Code must be complete and runnable
        - Must include main method
        - Use Scanner for input
        - Close Scanner
        - Handle case-insensitive input if needed
        - No explanation outside JSON

        Question:
        """ + question;
    }

    // Retry + Auto-Fix Layer
    private RespnseStructure executeWithRetry(String prompt) {

        for (int i = 0; i < 3; i++) {
            try {

                RespnseStructure response = chatClient
                        .prompt()
                        .user(prompt)
                        .call()
                        .entity(new ParameterizedTypeReference<RespnseStructure>() {});

                validate(response);
                return response;

            } catch (Exception e) {

                // Auto-fix prompt (important)
                prompt += """
                        
                        IMPORTANT:
                        - Previous response was invalid
                        - STRICTLY follow JSON format
                        - Ensure valid Java code
                        - Do not miss any fields
                        """;
            }
        }

        throw new RuntimeException("AI failed after 3 retries");
    }

    // Validation Layer
    private void validate(RespnseStructure res) {

        if (res == null) {
            throw new RuntimeException("Response is null");
        }

        if (res.getContent() == null || !res.getContent().contains("class")) {
            throw new RuntimeException("Invalid Java code");
        }

        if (!res.getContent().contains("main")) {
            throw new RuntimeException("Main method missing");
        }

        if (!res.getContent().contains("Scanner")) {
            throw new RuntimeException("Scanner not used");
        }

        if (!res.getContent().contains("close")) {
            throw new RuntimeException("Scanner not closed");
        }

        if (res.getTitle() == null || res.getTitle().isEmpty()) {
            throw new RuntimeException("Title missing");
        }
    }
}