package com.spring.ai.project.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.ai.project.dto.RespnseStructure;
import com.spring.ai.project.service.AiService;
import com.spring.ai.project.service.CodeValidatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Year;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient; // PRIMARY (Ollama)
    private final WebClient deepseekClient; // BACKUP (DeepSeek)
    private final PromptTemplateService templateService;
    private final CodeValidatorService codeValidatorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiServiceImpl(ChatClient.Builder builder,
                         WebClient deepseekClient,
                         PromptTemplateService templateService,
                         CodeValidatorService codeValidatorService) {

        this.chatClient = builder.build();
        this.deepseekClient = deepseekClient;
        this.templateService = templateService;
        this.codeValidatorService = codeValidatorService;
    }

    @Override
    public RespnseStructure askToAi(String question) {

        String type = detectType(question); 
        String basePrompt = templateService.getTemplate(type, question);

        return executeWithRetry(basePrompt, type);
    }

    //  Detect question type
    private String detectType(String question) {

        String q = question.toLowerCase();

        if (q.contains("pattern") || q.contains("singleton") || q.contains("design")) {
            return "JAVA_THEORY";
        }

        return "JAVA_PROGRAM";
    }

    private RespnseStructure executeWithRetry(String basePrompt, String type) {

        String prompt = basePrompt;

        for (int i = 0; i < 3; i++) {
            try {

                String raw;

                // PRIMARY
                try {
                    raw = chatClient
                            .prompt()
                            .user(prompt)
                            .call()
                            .content();
                } catch (Exception e) {
                    log.warn("Primary failed → switching to DeepSeek");
                    raw = callDeepSeek(prompt);
                }

                log.info("AI RAW RESPONSE:\n{}", raw);

                raw = cleanResponse(raw);

                // JSON CASE
                if (raw.trim().startsWith("{")) {

                    String json = extractJson(raw);

                    RespnseStructure res =
                            objectMapper.readValue(json, RespnseStructure.class);

                    String code = extractJavaCode(res.getContent());
                    res.setContent(code);

                    validate(res, type);

                    return res;
                }

                // RAW JAVA FALLBACK
                if (raw.contains("class")) {

                    log.warn("Using raw Java fallback");

                    String code = extractJavaCode(raw);

                    RespnseStructure res = RespnseStructure.builder()
                            .title("Java Program")
                            .content(code)
                            .description("Generated Java program")
                            .createdYear(String.valueOf(Year.now().getValue()))
                            .build();

                    validate(res, type);

                    return res;
                }

                throw new RuntimeException("Invalid response");

            } catch (Exception e) {

                log.error("Retrying... Attempt {}", (i + 1));

                prompt = basePrompt + """

                        ERROR:
                        Previous response was INVALID.

                        STRICT FIX:
                        - Return ONLY JSON
                        - content MUST be pure Java code
                        - DO NOT include explanations
                        - DO NOT mix Java + JSON
                        - DO NOT use markdown
                        - Code must compile

                        TRY AGAIN.
                        """;
            }
        }

        // FINAL FALLBACK
        return RespnseStructure.builder()
                .title("Error")
                .content("AI failed to generate valid response")
                .description("Fallback response")
                .createdYear(String.valueOf(Year.now().getValue()))
                .build();
    }

    // BACKUP CALL
    private String callDeepSeek(String prompt) {

        return deepseekClient.post()
                .uri("/api/generate")
                .bodyValue(prompt)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String extractJson(String raw) {
        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");
        if (start == -1 || end == -1) {
            throw new RuntimeException("No JSON found");
        }
        return raw.substring(start, end + 1);
    }

    private String extractJavaCode(String raw) {

        int start = raw.indexOf("import");
        int end = raw.lastIndexOf("}");

        if (start != -1 && end != -1) {
            return raw.substring(start, end + 1).trim();
        }

        return raw.trim();
    }

    private String cleanResponse(String raw) {
        return raw
                .replace("```java", "")
                .replace("```", "")
                .replace("system.out", "System.out")
                .trim();
    }

    //  SMART VALIDATION
    private void validate(RespnseStructure res, String type) {

        String content = res.getContent();

        if (content == null || content.isEmpty()) {
            throw new RuntimeException("Content missing");
        }

        if (!content.contains("class")) {
            throw new RuntimeException("No class found");
        }

        //  PROGRAM TYPE
        if ("JAVA_PROGRAM".equals(type)) {

            if (!content.contains("main")) {
                throw new RuntimeException("Main missing");
            }

            if (!content.contains("Scanner")) {
                throw new RuntimeException("Scanner missing");
            }

            if (!content.contains("close()")) {
                throw new RuntimeException("Scanner not closed");
            }
        }

        //  THEORY TYPE
        if ("JAVA_THEORY".equals(type)) {

            if (content.contains("Scanner")) {
                throw new RuntimeException("Scanner not allowed");
            }
        }

        if (!content.contains("System.out")) {
            log.warn("System.out not present (allowed)");
        }

        if (content.contains("system.out")) {
            throw new RuntimeException("Wrong system.out");
        }

        if (!codeValidatorService.isValidJavaCode(content)) {
            throw new RuntimeException("Compilation failed");
        }
    }
}