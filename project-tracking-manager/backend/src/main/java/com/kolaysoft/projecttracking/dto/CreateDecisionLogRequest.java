package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.DecisionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateDecisionLogRequest {

    @NotNull(message = "Proje ID değeri zorunludur.")
    @Positive(message = "Proje ID değeri pozitif olmalıdır.")
    private Long projectId;

    @Positive(message = "Haftalık rapor ID değeri pozitif olmalıdır.")
    private Long weeklyReportId;

    @Positive(message = "Karar sahibi kullanıcı ID değeri pozitif olmalıdır.")
    private Long decisionOwnerId;

    @NotBlank(message = "Karar başlığı boş bırakılamaz.")
    @Size(
            max = 200,
            message = "Karar başlığı en fazla 200 karakter olabilir."
    )
    private String title;

    @NotBlank(message = "Karar açıklaması boş bırakılamaz.")
    @Size(
            max = 2000,
            message = "Karar açıklaması en fazla 2000 karakter olabilir."
    )
    private String description;

    @NotNull(message = "Karar tarihi zorunludur.")
    private LocalDate decisionDate;

    private DecisionStatus status;

    @Size(
            max = 2000,
            message = "Karar notu en fazla 2000 karakter olabilir."
    )
    private String note;

    public CreateDecisionLogRequest() {
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(
            Long projectId
    ) {
        this.projectId = projectId;
    }

    public Long getWeeklyReportId() {
        return weeklyReportId;
    }

    public void setWeeklyReportId(
            Long weeklyReportId
    ) {
        this.weeklyReportId = weeklyReportId;
    }

    public Long getDecisionOwnerId() {
        return decisionOwnerId;
    }

    public void setDecisionOwnerId(
            Long decisionOwnerId
    ) {
        this.decisionOwnerId = decisionOwnerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title
    ) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(
            LocalDate decisionDate
    ) {
        this.decisionDate = decisionDate;
    }

    public DecisionStatus getStatus() {
        return status;
    }

    public void setStatus(
            DecisionStatus status
    ) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(
            String note
    ) {
        this.note = note;
    }
}
