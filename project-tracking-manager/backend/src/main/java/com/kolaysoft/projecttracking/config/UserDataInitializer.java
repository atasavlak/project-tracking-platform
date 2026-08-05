package com.kolaysoft.projecttracking.config;

import com.kolaysoft.projecttracking.repository.AppUserRepository;
import com.kolaysoft.projecttracking.user.AppUser;
import com.kolaysoft.projecttracking.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(1)
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.sample-data.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class UserDataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserIfMissing(
                "manager",
                "Manager123!",
                "Örnek Proje Yöneticisi",
                "manager@projecttracking.local",
                "+905550000001",
                UserRole.PROJECT_MANAGER
        );

        createUserIfMissing(
                "manager2",
                "Manager2123!",
                "İkinci Proje Yöneticisi",
                "manager2@projecttracking.local",
                "+905550000002",
                UserRole.PROJECT_MANAGER
        );

        createUserIfMissing(
                "cto",
                "Cto123!",
                "Örnek CTO",
                "cto@projecttracking.local",
                "+905550000003",
                UserRole.CTO
        );

        createUserIfMissing(
                "teamlead",
                "TeamLead123!",
                "Örnek Ekip Lideri",
                "teamlead@projecttracking.local",
                "+905550000004",
                UserRole.TEAM_LEAD
        );

        createUserIfMissing(
                "admin",
                "Admin123!",
                "Sistem Yöneticisi",
                "admin@projecttracking.local",
                "+905550000005",
                UserRole.ADMIN
        );
    }

    private void createUserIfMissing(
            String username,
            String rawPassword,
            String fullName,
            String email,
            String phoneNumber,
            UserRole role
    ) {
        boolean userExists =
                appUserRepository.existsByUsernameIgnoreCase(
                        username
                );

        if (userExists) {
            return;
        }

        AppUser user = new AppUser();

        user.setUsername(username);
        user.setPassword(
                passwordEncoder.encode(rawPassword)
        );
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setRole(role);
        user.setActive(true);
        user.setEmailVerified(true);
        user.setActivationCompleted(true);
        user.setPasswordChangedAt(
                LocalDateTime.now()
        );

        appUserRepository.save(user);
    }
}