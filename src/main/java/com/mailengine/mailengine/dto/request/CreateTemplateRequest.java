package com.mailengine.mailengine.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateTemplateRequest {
    @NotBlank
    private String name;

    private String category;

    @NotBlank
    private String htmlContent;
}
