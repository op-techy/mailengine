package com.mailengine.mailengine.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateRecipientRequest {

    @NotBlank
    @Email(message = "Please enter a valid email address")
    private String email;

    private String firstName;
    private String lastName;
    private String company;
}