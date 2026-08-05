package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.RiskIssueSeverity;
import com.kolaysoft.projecttracking.entity.RiskIssueStatus;
import com.kolaysoft.projecttracking.entity.RiskIssueType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RiskIssueResponse {

    private Long id;

    private Long weeklyReportId;

    private Long projectId;

    private String projectName;

    private Long responsibleUserId;

    private String responsibleUsername;

    private String responsibleFullName;

    private RiskIssueType type;

    private String title;

    private String description;

    private RiskIssueSeverity severity;

    private RiskIssueStatus status;

    private LocalDate followUpDate;

    private String resolutionNote;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public RiskIssueResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id
    ) {
        this.id =
                id;
    }

    public Long getWeeklyReportId() {
        return weeklyReportId;
    }

    public void setWeeklyReportId(
            Long weeklyReportId
    ) {
        this.weeklyReportId =
                weeklyReportId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(
            Long projectId
    ) {
        this.projectId =
                projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(
            String projectName
    ) {
        this.projectName =
                projectName;
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

    public String getResponsibleUsername() {
        return responsibleUsername;
    }

    public void setResponsibleUsername(
            String responsibleUsername
    ) {
        this.responsibleUsername =
                responsibleUsername;
    }

    public String getResponsibleFullName() {
        return responsibleFullName;
    }

    public void setResponsibleFullName(
            String responsibleFullName
    ) {
        this.responsibleFullName =
                responsibleFullName;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(
            boolean active
    ) {
        this.active =
                active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt =
                createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt =
                updatedAt;
    }
}
