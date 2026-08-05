package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String username;

    private String fullName;

    private String email;

    private String phoneNumber;

    private UserRole role;

    private boolean active;

    private boolean emailVerified;

    private boolean activationCompleted;

    private LocalDateTime passwordChangedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}