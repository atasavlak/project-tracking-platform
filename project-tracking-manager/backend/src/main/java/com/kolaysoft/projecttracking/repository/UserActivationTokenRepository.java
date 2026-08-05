package com.kolaysoft.projecttracking.repository;

import com.kolaysoft.projecttracking.entity.UserActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserActivationTokenRepository
        extends JpaRepository<UserActivationToken, Long> {

    Optional<UserActivationToken>
    findByTokenHashAndUsedAtIsNull(
            String tokenHash
    );

    void deleteByAppUser_IdAndUsedAtIsNull(
            Long userId
    );
}