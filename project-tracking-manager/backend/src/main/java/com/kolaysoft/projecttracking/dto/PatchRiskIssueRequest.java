package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.RiskIssueSeverity;
import com.kolaysoft.projecttracking.entity.RiskIssueStatus;
import com.kolaysoft.projecttracking.entity.RiskIssueType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PatchRiskIssueRequest {

    @Positive(message = "Sorumlu kullanıcı ID değeri pozitif olmalıdır.")
    private Long responsibleUserId;

    private RiskIssueType type;

    @Size(
            max = 200,
            message = "Risk veya engel başlığı en fazla 200 karakter olabilir."
    )
    private String title;

    @Size(
            max = 2000,
            message = "Risk veya engel açıklaması en fazla 2000 karakter olabilir."
    )
    private String description;

    private RiskIssueSeverity severity;

    private RiskIssueStatus status;

    private LocalDate followUpDate;

    @Size(
            max = 2000,
            message = "Çözüm notu en fazla 2000 karakter olabilir."
    )
    private String resolutionNote;

    public PatchRiskIssueRequest() {
    }

    public Long getResponsibleUserId() {
        return responsibleUserId;
    }

    public void setResponsibleUserId(
            Long responsibleUserId
    ) {
        this.responsibleUserId =
                responsibleUserId;
    }

    public RiskIssueType getType() {
        return type;
    }

    public void setType(
            RiskIssueType type
    ) {
        this.type =
                type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title
    ) {
        this.title =
                title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description =
                description;
    }

    public RiskIssueSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(
            RiskIssueSeverity severity
    ) {
        this.severity =
                severity;
    }

    public RiskIssueStatus getStatus() {
        return status;
    }

    public void setStatus(
            RiskIssueStatus status
    ) {
        this.status =
                status;
    }

    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(
            LocalDate followUpDate
    ) {
        this.followUpDate =
                followUpDate;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(
            String resolutionNote
    ) {
        this.resolutionNote =
                resolutionNote;
    }
}
