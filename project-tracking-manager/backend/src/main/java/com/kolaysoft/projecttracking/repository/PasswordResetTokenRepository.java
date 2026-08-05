package com.kolaysoft.projecttracking.repository;

import com.kolaysoft.projecttracking.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken>
    findByTokenHashAndUsedAtIsNull(
            String tokenHash
    );

    void deleteByAppUser_IdAndUsedAtIsNull(
            Long userId
    );
}