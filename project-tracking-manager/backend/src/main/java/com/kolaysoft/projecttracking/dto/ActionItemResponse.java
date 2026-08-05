package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.ActionItemPriority;
import com.kolaysoft.projecttracking.entity.ActionItemStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ActionItemResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private Long weeklyReportId;
    private Long responsibleUserId;
    private String responsibleUsername;
    private String responsibleFullName;
    private String title;
    private String description;
    private ActionItemPriority priority;
    private ActionItemStatus status;
    private LocalDate targetDate;
    private LocalDate completionDate;
    private String note;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ActionItemResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getWeeklyReportId() {
        return weeklyReportId;
    }

    public void setWeeklyReportId(Long weeklyReportId) {
        this.weeklyReportId = weeklyReportId;
    }

    public Long getResponsibleUserId() {
        return responsibleUserId;
    }

    public void setResponsibleUserId(Long responsibleUserId) {
        this.responsibleUserId = responsibleUserId;
    }

    public String getResponsibleUsername() {
        return responsibleUsername;
    }

    public void setResponsibleUsername(String responsibleUsername) {
        this.responsibleUsername = responsibleUsername;
    }

    public String getResponsibleFullName() {
        return responsibleFullName;
    }

    public void setResponsibleFullName(String responsibleFullName) {
        this.responsibleFullName = responsibleFullName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActionItemPriority getPriority() {
        return priority;
    }

    public void setPriority(ActionItemPriority priority) {
        this.priority = priority;
    }

    public ActionItemStatus getStatus() {
        return status;
    }

    public void setStatus(ActionItemStatus status) {
        this.status = status;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
