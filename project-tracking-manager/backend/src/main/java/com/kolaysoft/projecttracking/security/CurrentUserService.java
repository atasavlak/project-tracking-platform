package com.kolaysoft.projecttracking.security;

import com.kolaysoft.projecttracking.user.UserRole;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public AppUserPrincipal getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new AuthenticationCredentialsNotFoundException(
                    "Bu işlem için kullanıcı girişi gereklidir."
            );
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof AppUserPrincipal userPrincipal)) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Oturum kullanıcısı belirlenemedi."
            );
        }

        return userPrincipal;
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public UserRole getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    public boolean hasRole(UserRole role) {
        return getCurrentUserRole() == role;
    }

    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }
}