package com.kolaysoft.projecttracking.security;

import com.kolaysoft.projecttracking.repository.AppUserRepository;
import com.kolaysoft.projecttracking.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(
            String usernameOrEmail
    ) throws UsernameNotFoundException {

        String normalizedValue =
                usernameOrEmail == null
                        ? ""
                        : usernameOrEmail.trim();

        if (normalizedValue.isBlank()) {
            throw new UsernameNotFoundException(
                    "Kullanıcı adı, e-posta veya şifre hatalıdır."
            );
        }

        AppUser user =
                appUserRepository
                        .findByUsernameIgnoreCaseAndActiveTrue(
                                normalizedValue
                        )
                        .or(() ->
                                appUserRepository
                                        .findByEmailIgnoreCaseAndActiveTrue(
                                                normalizedValue
                                        )
                        )
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "Kullanıcı adı, e-posta veya şifre hatalıdır."
                                )
                        );

        return new AppUserPrincipal(
                user
        );
    }
}