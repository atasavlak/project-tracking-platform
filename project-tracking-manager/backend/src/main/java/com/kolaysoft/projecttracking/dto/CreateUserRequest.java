package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(
            message = "Kullanıcı adı zorunludur."
    )
    @Size(
            min = 3,
            max = 100,
            message = "Kullanıcı adı 3 ile 100 karakter arasında olmalıdır."
    )
    private String username;

    @NotBlank(
            message = "Ad soyad zorunludur."
    )
    @Size(
            min = 2,
            max = 150,
            message = "Ad soyad 2 ile 150 karakter arasında olmalıdır."
    )
    private String fullName;

    @NotBlank(
            message = "E-posta adresi zorunludur."
    )
    @Email(
            message = "Geçerli bir e-posta adresi giriniz."
    )
    @Size(
            max = 150,
            message = "E-posta adresi en fazla 150 karakter olabilir."
    )
    private String email;

    @Size(
            max = 20,
            message = "Telefon numarası en fazla 20 karakter olabilir."
    )
    private String phoneNumber;

    @NotNull(
            message = "Kullanıcı rolü zorunludur."
    )
    private UserRole role;
}