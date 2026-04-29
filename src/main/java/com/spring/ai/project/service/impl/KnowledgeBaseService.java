package com.spring.ai.project.service.impl;

import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseService {

    public String getContext(String question) {

        if (question.toLowerCase().contains("java")) {
            return """
            Follow Java best practices:
            - Use Scanner for input
            - Use proper class structure
            - Follow naming conventions
            """;
        }

        return "";
    }
}