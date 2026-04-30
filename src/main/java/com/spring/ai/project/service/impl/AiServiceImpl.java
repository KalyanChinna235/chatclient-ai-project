package com.spring.ai.project.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.ai.project.dto.RespnseStructure;
import com.spring.ai.project.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;
    private final PromptTemplateService templateService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiServiceImpl(ChatClient.Builder builder,
                         PromptTemplateService templateService) {
        this.chatClient = builder.build();
        this.templateService = templateService;
    }

    @Override
    public RespnseStructure askToAi(String question) {
        String basePrompt = templateService.getTemplate("JAVA_PROGRAM", question);
        return executeWithRetry(basePrompt);
    }

    private RespnseStructure executeWithRetry(String basePrompt) {

        String prompt = basePrompt;

        for (int i = 0; i < 3; i++) {
            try {

                String raw = chatClient
                        .prompt()
                        .user(prompt)
                        .call()
                        .content();

                log.info("AI RAW RESPONSE:\n{}", raw);

                // Step 1: Clean
                raw = cleanResponse(raw);

                // Step 2: Extract JSON if present
                if (raw.trim().startsWith("{")) {

                    String json = extractJson(raw);

                    RespnseStructure res =
                            objectMapper.readValue(json, RespnseStructure.class);

                    // Step 3: Auto-format code
                    String formatted = formatJavaCode(res.getContent());
                    res.setContent(formatted);

                    // Step 4: Validate
                    validate(res);

                    return res;
                }

                // Step 5: Raw Java fallback
                if (raw.contains("class") && raw.contains("main")) {

                    log.warn("Using fallback (raw Java)");

                    String formatted = formatJavaCode(raw);

                    RespnseStructure res = RespnseStructure.builder()
                            .title("Java Program")
                            .content(formatted)
                            .description("Generated Java program")
                            .createdYear(String.valueOf(Year.now().getValue()))
                            .build();

                    validate(res);

                    return res;
                }

                throw new RuntimeException("Invalid AI response format");

            } catch (Exception e) {

                log.error("Retrying... Attempt: {}", (i + 1));

                prompt = basePrompt + """

                        ERROR:
                        Previous response was INVALID.

                        FIX STRICTLY:
                        - Return ONLY valid JSON
                        - No markdown
                        - No explanations
                        - Code must compile
                        - Use \\n for new lines
                        - Ensure all braces are closed
                        - Use System.out.println()
                        - Include import java.util.Scanner

                        RETURN ONLY JSON
                        """;
            }
        }

        // FINAL FALLBACK
        return RespnseStructure.builder()
                .title("Error")
                .content("AI failed to generate valid response")
                .description("Fallback response after retries")
                .createdYear(String.valueOf(Year.now().getValue()))
                .build();
    }

    // Extract JSON safely
    private String extractJson(String raw) {
        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");

        if (start == -1 || end == -1) {
            throw new RuntimeException("No JSON found");
        }

        return raw.substring(start, end + 1);
    }

    // Clean AI junk
    private String cleanResponse(String raw) {
        return raw
                .replace("```java", "")
                .replace("```", "")
                .replace("system.out", "System.out") // auto-fix
                .trim();
    }

    // Auto-format Java code
    private String formatJavaCode(String code) {
        return code
                .replace("{", "{\n")
                .replace("}", "\n}\n")
                .replace(";", ";\n")
                .replaceAll("\\n\\s*\\n", "\n")
                .replaceAll("\\n+", "\n")
                .trim();
    }

    // Strong validation
    private void validate(RespnseStructure res) {

        if (res == null) throw new RuntimeException("Response null");

        if (res.getTitle() == null || res.getTitle().isEmpty()) {
            throw new RuntimeException("Title missing");
        }

        String content = res.getContent();

        if (content == null || content.isEmpty()) {
            throw new RuntimeException("Content missing");
        }

        // Format check
        if (!content.contains("\n")) {
            throw new RuntimeException("Code not formatted");
        }

        // Required elements
        if (!content.contains("class")) throw new RuntimeException("No class");
        if (!content.contains("main")) throw new RuntimeException("No main method");
        if (!content.contains("Scanner")) throw new RuntimeException("No Scanner");
        if (!content.contains("import java.util.Scanner")) throw new RuntimeException("Missing import");

        // System.out checks
        if (!content.contains("System.out")) throw new RuntimeException("System.out missing");
        if (content.contains("system.out")) throw new RuntimeException("Wrong system.out");

        // Close scanner
        if (!content.contains("close()")) {
            throw new RuntimeException("Scanner not closed");
        }

        // No explanation allowed
        if (content.contains("This program") || content.contains("Explanation")) {
            throw new RuntimeException("Explanation detected");
        }

        // No markdown
        if (content.contains("```")) {
            throw new RuntimeException("Markdown not allowed");
        }

        // Braces validation
        long open = content.chars().filter(ch -> ch == '{').count();
        long close = content.chars().filter(ch -> ch == '}').count();

        if (open != close) {
            throw new RuntimeException("Braces mismatch");
        }

        // Year check
        if (res.getCreatedYear() == null ||
                !res.getCreatedYear().matches("\\d{4}")) {
            throw new RuntimeException("Invalid year");
        }
    }
}