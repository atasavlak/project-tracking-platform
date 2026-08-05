package com.kolaysoft.projecttracking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest {

    @NotNull(message = "Kullanıcı durumu zorunludur.")
    private Boolean active;
}