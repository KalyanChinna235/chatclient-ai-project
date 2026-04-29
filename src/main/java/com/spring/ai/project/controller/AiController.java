package com.spring.ai.project.controller;

import com.spring.ai.project.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/ask")
    public ResponseEntity<?> askToAi(@RequestParam String question) {
        return ResponseEntity.ok(aiService.askToAi(question));
    }
}