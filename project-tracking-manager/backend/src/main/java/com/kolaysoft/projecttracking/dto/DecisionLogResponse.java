package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.DecisionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DecisionLogResponse {

    private Long id;

    private Long projectId;

    private String projectName;

    private Long weeklyReportId;

    private Long decisionOwnerId;

    private String decisionOwnerUsername;

    private String decisionOwnerFullName;

    private String title;

    private String description;

    private LocalDate decisionDate;

    private DecisionStatus status;

    private String note;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public DecisionLogResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id
    ) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(
            Long projectId
    ) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(
            String projectName
    ) {
        this.projectName = projectName;
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

    public String getDecisionOwnerUsername() {
        return decisionOwnerUsername;
    }

    public void setDecisionOwnerUsername(
            String decisionOwnerUsername
    ) {
        this.decisionOwnerUsername = decisionOwnerUsername;
    }

    public String getDecisionOwnerFullName() {
        return decisionOwnerFullName;
    }

    public void setDecisionOwnerFullName(
            String decisionOwnerFullName
    ) {
        this.decisionOwnerFullName = decisionOwnerFullName;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(
            boolean active
    ) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt = updatedAt;
    }
}
