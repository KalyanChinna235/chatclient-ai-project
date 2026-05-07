package com.spring.ai.project.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.ai.project.dto.RespnseStructure;
import com.spring.ai.project.entity.AiResponseStore;
import com.spring.ai.project.repo.AiResponseRepository;
import com.spring.ai.project.service.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RagServiceImpl implements RagService {

    private final AiResponseRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EmbeddingModel embeddingModel;

    public RagServiceImpl(AiResponseRepository repository,
                          EmbeddingModel embeddingModel) {
        this.repository = repository;
        this.embeddingModel = embeddingModel;
    }

    // 🔥 VECTOR SEARCH
    @Override
    public Optional<RespnseStructure> find(String question, String type) {

        try {
            float[] queryVector = embeddingModel.embed(question);

            List<AiResponseStore> all = repository.findAll();

            List<AiResponseStore> candidates = new ArrayList<>();

            for (AiResponseStore entity : all) {

                if (!entity.getType().equals(type)) continue;
                if (entity.getEmbedding() == null) continue;

                float[] dbVector = objectMapper.readValue(
                        entity.getEmbedding(),
                        float[].class
                );

                double score = cosineSimilarity(queryVector, dbVector);

                // ✅ Step 1: collect candidates
                if (score > 0.75) { // lower threshold for wider net
                    entity.setScore(score); // transient field
                    candidates.add(entity);
                }
            }

            // ✅ Step 2: sort by score DESC
            candidates.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

            if (!candidates.isEmpty()) {

                AiResponseStore best = candidates.get(0);

                log.info("TOP MATCH SCORE: {}", best.getScore());

                // ✅ Step 3: final strict check
                if (best.getScore() > 0.85) {

                    log.info("VECTOR RAG HIT");

                    return Optional.of(
                            objectMapper.readValue(
                                    best.getAnswerJson(),
                                    RespnseStructure.class
                            )
                    );
                }
            }

        } catch (Exception e) {
            log.error("RAG error", e);
        }

        return Optional.empty();
    }

    // 🔥 SAVE EMBEDDING
    @Override
    public void save(String question, String type, RespnseStructure response) {

        try {
            // ✅ FIX: float[]
            float[] vector = embeddingModel.embed(question);

            AiResponseStore entity = AiResponseStore.builder()
                    .question(question)
                    .type(type)
                    .answerJson(objectMapper.writeValueAsString(response))
                    .embedding(objectMapper.writeValueAsString(vector)) // store JSON
                    .build();

            repository.save(entity);

            log.info("Saved to VECTOR DB");

        } catch (Exception e) {
            log.error("Save failed", e);
        }
    }

    // 🔥 COSINE SIMILARITY (float[] version)
    private double cosineSimilarity(float[] v1, float[] v2) {

        if (v1.length != v2.length) return 0.0;

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}