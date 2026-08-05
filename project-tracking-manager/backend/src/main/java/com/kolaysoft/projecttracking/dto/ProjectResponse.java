package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private Long projectManagerId;
    private String projectManagerFullName;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }

    public Long getProjectManagerId() {
        return projectManagerId;
    }

    public void setProjectManagerId(
            Long projectManagerId
    ) {
        this.projectManagerId =
                projectManagerId;
    }

    public String getProjectManagerFullName() {
        return projectManagerFullName;
    }

    public void setProjectManagerFullName(
            String projectManagerFullName
    ) {
        this.projectManagerFullName =
                projectManagerFullName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(
            LocalDate startDate
    ) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(
            LocalDate endDate
    ) {
        this.endDate = endDate;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(
            ProjectStatus status
    ) {
        this.status = status;
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