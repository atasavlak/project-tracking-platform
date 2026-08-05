package com.kolaysoft.projecttracking.controller;

import com.kolaysoft.projecttracking.dto.ActivateAccountRequest;
import com.kolaysoft.projecttracking.dto.ForgotPasswordRequest;
import com.kolaysoft.projecttracking.dto.CurrentUserResponse;
import com.kolaysoft.projecttracking.dto.MessageResponse;
import com.kolaysoft.projecttracking.dto.ResetPasswordRequest;
import com.kolaysoft.projecttracking.service.AccountActivationService;
import com.kolaysoft.projecttracking.service.AuthenticationService;
import com.kolaysoft.projecttracking.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Kimlik Doğrulama",
        description = "Hesap aktivasyonu ve şifre işlemleri"
)
public class AuthController {

    private final AccountActivationService
            accountActivationService;

    private final PasswordResetService
            passwordResetService;

    private final AuthenticationService
            authenticationService;

    @Operation(
            summary = "Giriş yapan kullanıcının bilgilerini getirir"
    )
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser() {
        return ResponseEntity.ok(
                authenticationService.getCurrentUser()
        );
    }

    @Operation(
            summary = "Kullanıcı hesabını aktive eder ve ilk şifreyi belirler"
    )
    @PostMapping("/activate")
    public ResponseEntity<MessageResponse> activateAccount(
            @Valid
            @RequestBody
            ActivateAccountRequest request
    ) {
        accountActivationService
                .activateAccount(request);

        return ResponseEntity.ok(
                new MessageResponse(
                        "Hesabınız başarıyla aktive edildi. Sisteme giriş yapabilirsiniz."
                )
        );
    }

    @Operation(
            summary = "Şifre sıfırlama bağlantısı gönderir"
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid
            @RequestBody
            ForgotPasswordRequest request
    ) {
        passwordResetService
                .requestPasswordReset(
                        request.getEmail()
                );

        return ResponseEntity.ok(
                new MessageResponse(
                        "E-posta adresi sistemde kayıtlıysa şifre sıfırlama bağlantısı gönderilmiştir."
                )
        );
    }

    @Operation(
            summary = "Şifre sıfırlama tokenı ile yeni şifre belirler"
    )
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid
            @RequestBody
            ResetPasswordRequest request
    ) {
        passwordResetService
                .resetPassword(request);

        return ResponseEntity.ok(
                new MessageResponse(
                        "Şifreniz başarıyla değiştirildi. Yeni şifrenizle giriş yapabilirsiniz."
                )
        );
    }
}