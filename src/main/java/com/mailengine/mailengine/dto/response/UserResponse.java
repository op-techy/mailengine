package com.mailengine.mailengine.dto.response;

import com.mailengine.mailengine.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {

    private long id;

    private String name;

    private String email;

    private Role role;

    private boolean emailVerified;

    private boolean mustChangePwd;

    private Instant createdAt;
}
