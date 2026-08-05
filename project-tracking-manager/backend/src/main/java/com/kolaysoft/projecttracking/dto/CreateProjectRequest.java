package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.ProjectStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CreateProjectRequest {

    @NotBlank(
            message = "Proje adı zorunludur."
    )
    @Size(
            max = 200,
            message = "Proje adı en fazla 200 karakter olabilir."
    )
    private String name;

    @Size(
            max = 2000,
            message = "Açıklama en fazla 2000 karakter olabilir."
    )
    private String description;

    @NotNull(
            message = "Başlangıç tarihi zorunludur."
    )
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(
            message = "Proje durumu zorunludur."
    )
    private ProjectStatus status;
}