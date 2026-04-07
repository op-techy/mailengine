package com.mailengine.mailengine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class RecipientListResponse {

    private Long id;
    private String name;
    private String description;
    private Integer recipientCount;
    private Instant createdAt;
}
