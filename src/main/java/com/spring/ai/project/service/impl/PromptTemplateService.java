package com.spring.ai.project.service.impl;

import org.springframework.stereotype.Service;

@Service
public class PromptTemplateService {

    public String buildJavaProgramPrompt(String question) {
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

        Rules:
        - Language must be Java
        - Code must be complete and runnable
        - Use Scanner if input required

        Question:
        %s
        """.formatted(question);
    }
}
