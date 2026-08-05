package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.user.UserRole;

public class AssignableUserResponse {

    private Long id;

    private String username;

    private String fullName;

    private UserRole role;

    public AssignableUserResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id
    ) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(
            String username
    ) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(
            String fullName
    ) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(
            UserRole role
    ) {
        this.role = role;
    }
}
