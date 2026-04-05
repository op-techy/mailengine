package com.mailengine.mailengine.dto.request;

import com.mailengine.mailengine.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InviteUserRequest {

    @NotBlank
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotNull
    private Role role;
}
