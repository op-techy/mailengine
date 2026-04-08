package com.mailengine.mailengine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class TemplateResponse {

    private Long id;
    private String name;
    private String category;
    private String htmlContent;
    private Map<String, Object> jsonDesign;
    private String thumbnailUrl;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;
}
