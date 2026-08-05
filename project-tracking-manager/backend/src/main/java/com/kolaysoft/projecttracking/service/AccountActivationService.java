package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.ActivateAccountRequest;
import com.kolaysoft.projecttracking.entity.UserActivationToken;
import com.kolaysoft.projecttracking.exception.BusinessRuleException;
import com.kolaysoft.projecttracking.repository.AppUserRepository;
import com.kolaysoft.projecttracking.repository.UserActivationTokenRepository;
import com.kolaysoft.projecttracking.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountActivationService {

    private final UserActivationTokenRepository
            userActivationTokenRepository;

    private final AppUserRepository appUserRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    @Value(
            "${app.activation-token-expiration-hours:24}"
    )
    private long activationTokenExpirationHours;

    @Transactional
    public void issueActivation(
            AppUser appUser
    ) {
        userActivationTokenRepository
                .deleteByAppUser_IdAndUsedAtIsNull(
                        appUser.getId()
                );

        String rawToken =
                generateRawToken();

        String tokenHash =
                hashToken(rawToken);

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plusHours(
                                activationTokenExpirationHours
                        );

        UserActivationToken activationToken =
                new UserActivationToken(
                        appUser,
                        tokenHash,
                        expiresAt
                );

        userActivationTokenRepository.save(
                activationToken
        );

        emailService.sendActivationEmail(
                appUser.getEmail(),
                appUser.getFullName(),
                rawToken,
                expiresAt
        );
    }

    @Transactional
    public void activateAccount(
            ActivateAccountRequest request
    ) {
        if (!request
                .getPassword()
                .equals(
                        request.getPasswordConfirmation()
                )) {

            throw new BusinessRuleException(
                    "Şifre ve şifre tekrarı eşleşmiyor."
            );
        }

        String tokenHash =
                hashToken(
                        request.getToken()
                );

        UserActivationToken activationToken =
                userActivationTokenRepository
                        .findByTokenHashAndUsedAtIsNull(
                                tokenHash
                        )
                        .orElseThrow(() ->
                                new BusinessRuleException(
                                        "Aktivasyon bağlantısı geçersiz veya daha önce kullanılmıştır."
                                )
                        );

        if (activationToken
                .getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new BusinessRuleException(
                    "Aktivasyon bağlantısının süresi dolmuştur."
            );
        }

        AppUser appUser =
                activationToken.getAppUser();

        if (appUser.isActivationCompleted()) {
            throw new BusinessRuleException(
                    "Kullanıcı hesabı daha önce aktive edilmiştir."
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        appUser.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );
        appUser.setActive(true);
        appUser.setEmailVerified(true);
        appUser.setActivationCompleted(true);
        appUser.setPasswordChangedAt(now);

        activationToken.setUsedAt(now);

        appUserRepository.save(appUser);

        userActivationTokenRepository.save(
                activationToken
        );
    }

    private String generateRawToken() {
        byte[] tokenBytes =
                new byte[32];

        SecureRandom secureRandom =
                new SecureRandom();

        secureRandom.nextBytes(
                tokenBytes
        );

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        tokenBytes
                );
    }

    private String hashToken(
            String rawToken
    ) {
        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    messageDigest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat
                    .of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Token hash algoritması oluşturulamadı.",
                    exception
            );
        }
    }
}