package com.spring.ai.project.repo;

import com.spring.ai.project.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    Optional<PromptTemplate> findByName(String name);
}