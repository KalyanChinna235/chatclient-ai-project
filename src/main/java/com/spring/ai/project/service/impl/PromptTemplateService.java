package com.spring.ai.project.service.impl;

import com.spring.ai.project.entity.PromptTemplate;
import com.spring.ai.project.repo.PromptTemplateRepository;
import org.springframework.stereotype.Service;

@Service
public class PromptTemplateService {
    private final PromptTemplateRepository repository;

    public PromptTemplateService(PromptTemplateRepository repository) {
        this.repository = repository;
    }

    public String getTemplate(String name, String question) {

        PromptTemplate template = repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Template not found: " + name));

        return template.getTemplate()
                .replace("{question}", question.trim()); // ✅ safe replace
    }
}
