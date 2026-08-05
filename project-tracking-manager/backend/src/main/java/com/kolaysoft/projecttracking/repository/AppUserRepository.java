package com.kolaysoft.projecttracking.repository;

import com.kolaysoft.projecttracking.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository
        extends JpaRepository<AppUser, Long> {

    Optional<AppUser>
    findByUsernameIgnoreCaseAndActiveTrue(
            String username
    );

    Optional<AppUser>
    findByEmailIgnoreCase(
            String email
    );

    Optional<AppUser>
    findByEmailIgnoreCaseAndActiveTrue(
            String email
    );

    Optional<AppUser>
    findByIdAndActiveTrueAndActivationCompletedTrue(
            Long id
    );

    boolean existsByUsernameIgnoreCase(
            String username
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    List<AppUser>
    findAllByOrderByCreatedAtDesc();

    List<AppUser>
    findByActiveTrueAndActivationCompletedTrueOrderByFullNameAsc();
}
