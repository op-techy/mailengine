package com.mailengine.mailengine.dto.response;

import com.mailengine.mailengine.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {

    private String token;

    private long userId;

    private String email;

    private Role role;
}
