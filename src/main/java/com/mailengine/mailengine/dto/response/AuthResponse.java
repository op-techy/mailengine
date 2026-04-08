package com.mailengine.mailengine.dto.response;

import com.mailengine.mailengine.entity.enums.Role;

public record AuthResponse(
        String token,
        UserDto user,
        long expiresIn // Pass the token expiration (e.g., in seconds or millis)
) {
    public record UserDto(
            Long id,
            String name,
            String email,
            Role role
    ) {}
}
