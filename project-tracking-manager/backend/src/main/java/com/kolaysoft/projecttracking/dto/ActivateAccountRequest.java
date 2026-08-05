package com.kolaysoft.projecttracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivateAccountRequest {

    @NotBlank(message = "Aktivasyon tokenı zorunludur.")
    private String token;

    @NotBlank(message = "Şifre zorunludur.")
    @Size(
            min = 8,
            max = 100,
            message = "Şifre 8 ile 100 karakter arasında olmalıdır."
    )
    private String password;

    @NotBlank(message = "Şifre tekrarı zorunludur.")
    private String passwordConfirmation;
}