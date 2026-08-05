package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.CurrentUserResponse;
import com.kolaysoft.projecttracking.exception.UserNotFoundException;
import com.kolaysoft.projecttracking.repository.AppUserRepository;
import com.kolaysoft.projecttracking.security.AppUserPrincipal;
import com.kolaysoft.projecttracking.security.CurrentUserService;
import com.kolaysoft.projecttracking.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationService {

    private final CurrentUserService currentUserService;
    private final AppUserRepository appUserRepository;

    public CurrentUserResponse getCurrentUser() {
        AppUserPrincipal principal =
                currentUserService.getCurrentUser();

        AppUser user =
                appUserRepository
                        .findById(principal.getId())
                        .filter(AppUser::isActive)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        principal.getId()
                                )
                        );

        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole()
        );
    }
}
