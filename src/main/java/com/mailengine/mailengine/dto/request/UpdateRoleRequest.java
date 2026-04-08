package com.mailengine.mailengine.dto.request;

import com.mailengine.mailengine.entity.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateRoleRequest {

    @NotNull
    private Role role;
}
