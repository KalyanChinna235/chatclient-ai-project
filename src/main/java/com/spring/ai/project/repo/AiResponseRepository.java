package com.spring.ai.project.repo;

import com.spring.ai.project.entity.AiResponseStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiResponseRepository extends JpaRepository<AiResponseStore, Long> {

    Optional<AiResponseStore> findTopByQuestionAndType(String question, String type);
    List<AiResponseStore> findAll();
}
