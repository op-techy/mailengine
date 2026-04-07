package com.mailengine.mailengine.dto.response;

import com.mailengine.mailengine.entity.enums.RecipientStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class RecipientResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String company;
    private RecipientStatus status;
    private Instant createdAt;
}
