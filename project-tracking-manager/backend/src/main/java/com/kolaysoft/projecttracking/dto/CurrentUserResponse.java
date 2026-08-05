package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CurrentUserResponse {

    private Long id;

    private String username;

    private String fullName;

    private UserRole role;
}
