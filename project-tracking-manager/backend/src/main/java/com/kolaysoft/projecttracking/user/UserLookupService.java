package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.AssignableUserResponse;
import com.kolaysoft.projecttracking.repository.AppUserRepository;
import com.kolaysoft.projecttracking.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLookupService {

    private final AppUserRepository appUserRepository;

    public List<AssignableUserResponse> getAssignableUsers() {
        return appUserRepository
                .findByActiveTrueAndActivationCompletedTrueOrderByFullNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AssignableUserResponse toResponse(
            AppUser appUser
    ) {
        AssignableUserResponse response =
                new AssignableUserResponse();

        response.setId(
                appUser.getId()
        );

        response.setUsername(
                appUser.getUsername()
        );

        response.setFullName(
                appUser.getFullName()
        );

        response.setRole(
                appUser.getRole()
        );

        return response;
    }
}
