package com.spring.ai.project.service;

import com.spring.ai.project.dto.RespnseStructure;

public interface AiService {
    RespnseStructure askToAi(String prompt);
}