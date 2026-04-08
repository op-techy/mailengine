package com.mailengine.mailengine.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class CreateTemplateRequest {

    @NotBlank
    private String name;

    private String category;

    @NotBlank
    private String htmlContent;

    /** Unlayer JSON design — required to restore the drag-and-drop editor state. */
    private Map<String, Object> jsonDesign;
}
