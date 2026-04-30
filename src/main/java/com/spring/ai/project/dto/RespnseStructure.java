package com.spring.ai.project.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class RespnseStructure {

    private String title;
    private String content;
    private String description;
    private String createdYear;

}
