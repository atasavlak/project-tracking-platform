package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.user.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {

    @NotNull(message = "Kullanıcı rolü zorunludur.")
    private UserRole role;
}