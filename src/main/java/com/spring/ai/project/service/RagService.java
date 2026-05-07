package com.spring.ai.project.service;

import com.spring.ai.project.dto.RespnseStructure;

import java.util.Optional;

public interface RagService {

    Optional<RespnseStructure> find(String question, String type);

    void save(String question, String type, RespnseStructure response);
}