package com.kolaysoft.projecttracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "Şifre sıfırlama tokenı zorunludur.")
    private String token;

    @NotBlank(message = "Yeni şifre zorunludur.")
    @Size(
            min = 8,
            max = 100,
            message = "Yeni şifre 8 ile 100 karakter arasında olmalıdır."
    )
    private String newPassword;

    @NotBlank(message = "Yeni şifre tekrarı zorunludur.")
    private String newPasswordConfirmation;
}