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
    private Long userId;
    private String name;
    private String email;
    private Role role;
    /** True when the user must change password before using the app (invited users). */
    private Boolean mustChangePwd;
}
