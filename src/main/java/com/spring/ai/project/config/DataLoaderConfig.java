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

            String template = """
                    You are a STRICT backend API.

                    You MUST return ONLY valid JSON.

                    DO NOT:
                    - Add markdown
                    - Add explanations
                    - Add multiple responses
                    - Return any language other than Java

                    STRICT OUTPUT:
                    - Must start with {
                    - Must end with }
                    - No extra text

                    FORMAT:
                    {
                      "title": "string",
                      "content": "string",
                      "description": "string",
                      "createdYear": "YYYY"
                    }

                    CONTENT RULES (VERY IMPORTANT):
                    - ONLY Java code
                    - NO explanations
                    - NO comments
                    - Code must END at last }

                    JAVA RULES:
                    - Must compile
                    - Must include:
                      import java.util.Scanner;
                      public class
                      public static void main(String[] args)
                      Scanner usage
                      scanner.close()

                    OUTPUT RULES:
                    - Use \\n for every new line
                    - DO NOT return single-line code
                    - MUST be properly formatted
                    - Use System.out.println ONLY

                    STRICTLY FORBIDDEN:
                    - system.out.println
                    - markdown
                    - explanations
                    - multiple outputs

                    ONLY RETURN JSON.

                    QUESTION:
                    {question}
                    """;

            PromptTemplate entity = repository.findByName("JAVA_PROGRAM")
                    .orElse(new PromptTemplate());

            entity.setName("JAVA_PROGRAM");
            entity.setTemplate(template);

            repository.save(entity);
        };
    }
}