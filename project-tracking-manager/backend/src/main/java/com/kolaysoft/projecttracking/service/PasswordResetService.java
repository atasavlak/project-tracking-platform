package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.ResetPasswordRequest;
import com.kolaysoft.projecttracking.entity.PasswordResetToken;
import com.kolaysoft.projecttracking.exception.BusinessRuleException;
import com.kolaysoft.projecttracking.repository.AppUserRepository;
import com.kolaysoft.projecttracking.repository.PasswordResetTokenRepository;
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
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private final AppUserRepository appUserRepository;

    private final PasswordResetTokenRepository
            passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    @Value(
            "${app.password-reset-token-expiration-minutes:15}"
    )
    private long passwordResetTokenExpirationMinutes;

    @Transactional
    public void requestPasswordReset(
            String email
    ) {
        if (email == null || email.isBlank()) {
            return;
        }

        String normalizedEmail =
                email.trim()
                        .toLowerCase(Locale.ROOT);

        AppUser appUser =
                appUserRepository
                        .findByEmailIgnoreCaseAndActiveTrue(
                                normalizedEmail
                        )
                        .orElse(null);

        if (appUser == null) {
            return;
        }

        if (!appUser.isActivationCompleted()
                || !appUser.isEmailVerified()
                || appUser.getPassword() == null) {

            return;
        }

        passwordResetTokenRepository
                .deleteByAppUser_IdAndUsedAtIsNull(
                        appUser.getId()
                );

        String rawToken =
                generateRawToken();

        String tokenHash =
                hashToken(rawToken);

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plusMinutes(
                                passwordResetTokenExpirationMinutes
                        );

        PasswordResetToken passwordResetToken =
                new PasswordResetToken(
                        appUser,
                        tokenHash,
                        expiresAt
                );

        passwordResetTokenRepository.save(
                passwordResetToken
        );

        emailService.sendPasswordResetEmail(
                appUser.getEmail(),
                appUser.getFullName(),
                rawToken,
                expiresAt
        );
    }

    @Transactional
    public void resetPassword(
            ResetPasswordRequest request
    ) {
        if (!request
                .getNewPassword()
                .equals(
                        request.getNewPasswordConfirmation()
                )) {

            throw new BusinessRuleException(
                    "Yeni şifre ve şifre tekrarı eşleşmiyor."
            );
        }

        String tokenHash =
                hashToken(
                        request.getToken()
                );

        PasswordResetToken passwordResetToken =
                passwordResetTokenRepository
                        .findByTokenHashAndUsedAtIsNull(
                                tokenHash
                        )
                        .orElseThrow(() ->
                                new BusinessRuleException(
                                        "Şifre sıfırlama bağlantısı geçersiz veya daha önce kullanılmıştır."
                                )
                        );

        LocalDateTime now =
                LocalDateTime.now();

        if (passwordResetToken
                .getExpiresAt()
                .isBefore(now)) {

            throw new BusinessRuleException(
                    "Şifre sıfırlama bağlantısının süresi dolmuştur."
            );
        }

        AppUser appUser =
                passwordResetToken.getAppUser();

        if (!appUser.isActive()) {
            throw new BusinessRuleException(
                    "Pasif kullanıcı hesabının şifresi sıfırlanamaz."
            );
        }

        if (!appUser.isActivationCompleted()
                || !appUser.isEmailVerified()) {

            throw new BusinessRuleException(
                    "Aktivasyonu tamamlanmamış kullanıcı hesabının şifresi sıfırlanamaz."
            );
        }

        if (appUser.getPassword() != null
                && passwordEncoder.matches(
                request.getNewPassword(),
                appUser.getPassword()
        )) {

            throw new BusinessRuleException(
                    "Yeni şifre mevcut şifreyle aynı olamaz."
            );
        }

        appUser.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        appUser.setPasswordChangedAt(now);

        passwordResetToken.setUsedAt(now);

        appUserRepository.save(appUser);

        passwordResetTokenRepository.save(
                passwordResetToken
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
        if (rawToken == null
                || rawToken.isBlank()) {

            throw new BusinessRuleException(
                    "Şifre sıfırlama tokenı zorunludur."
            );
        }

        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    messageDigest.digest(
                            rawToken.trim()
                                    .getBytes(
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