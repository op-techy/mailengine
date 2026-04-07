package com.mailengine.mailengine.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateRecipientListRequest {

    @NotBlank
    private String name;

    private String description;
}
