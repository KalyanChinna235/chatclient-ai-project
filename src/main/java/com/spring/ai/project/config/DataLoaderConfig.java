package com.spring.ai.project.config;

import com.spring.ai.project.entity.PromptTemplate;
import com.spring.ai.project.repo.PromptTemplateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoaderConfig {

    @Bean
    CommandLineRunner loadTemplates(PromptTemplateRepository repository) {
        return args -> {

            // ================================
            // JAVA PROGRAM TEMPLATE
            // ================================
            String programTemplate = """
                    You are a STRICT backend API.

                    You MUST return ONLY valid JSON.
                    NO markdown.
                    NO explanations.
                    NO multiple outputs.

                    STRICT JSON FORMAT:
                    {
                      "title": "string",
                      "content": "string",
                      "description": "string",
                      "createdYear": "YYYY"
                    }

                    CRITICAL RULES:
                    - "content" MUST contain ONLY Java code
                    - DO NOT write English sentences inside "content"
                    - DO NOT include comments
                    - DO NOT include text after closing brace

                    JAVA RULES (MANDATORY):
                    - Must compile successfully
                    - Must include:
                      import java.util.Scanner;
                      public class
                      public static void main(String[] args)
                      Scanner usage
                      scanner.close()

                    FORBIDDEN:
                    - "Write a program..."
                    - "This program..."
                    - Markdown (```)
                    - Multiple responses
                    - Mixing Java + JSON

                    QUESTION:
                    {question}
                    """;

            // ================================
            // JAVA THEORY TEMPLATE (NEW)
            // ================================
            String theoryTemplate = """
                    You are a STRICT backend API.

                    You MUST return ONLY valid JSON.
                    NO markdown.
                    NO explanations outside JSON.

                    STRICT JSON FORMAT:
                    {
                      "title": "string",
                      "content": "string",
                      "description": "string",
                      "createdYear": "YYYY"
                    }

                    CRITICAL RULES:
                    - "content" MUST contain ONLY Java code
                    - DO NOT include explanation inside code
                    - DO NOT include Scanner
                    - DO NOT require user input
                    - Focus on class/design implementation

                    JAVA RULES:
                    - Must compile
                    - Must include:
                      public class
                    - main method is OPTIONAL

                    FORBIDDEN:
                    - Markdown (```)
                    - Multiple responses
                    - Mixing Java + JSON

                    QUESTION:
                    {question}
                    """;

            // ================================
            // SAVE / UPDATE PROGRAM TEMPLATE
            // ================================
            PromptTemplate program = repository.findByName("JAVA_PROGRAM")
                    .orElse(new PromptTemplate());

            program.setName("JAVA_PROGRAM");
            program.setTemplate(programTemplate);
            repository.save(program);

            // ================================
            // SAVE / UPDATE THEORY TEMPLATE
            // ================================
            PromptTemplate theory = repository.findByName("JAVA_THEORY")
                    .orElse(new PromptTemplate());

            theory.setName("JAVA_THEORY");
            theory.setTemplate(theoryTemplate);
            repository.save(theory);
        };
    }
}