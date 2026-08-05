package com.kolaysoft.projecttracking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank(message = "E-posta adresi zorunludur.")
    @Email(message = "Geçerli bir e-posta adresi giriniz.")
    private String email;
}