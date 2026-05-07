package com.spring.ai.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponseStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000)
    private String question;

    @Column(columnDefinition = "TEXT")
    private String answerJson;

    private String type; // JAVA_PROGRAM / JAVA_THEORY

    @Column(columnDefinition = "TEXT")
    private String embedding;

    @Transient
    private double score;
}
