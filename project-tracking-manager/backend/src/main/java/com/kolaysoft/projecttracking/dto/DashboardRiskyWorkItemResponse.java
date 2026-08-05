package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.ProjectStatus;
import com.kolaysoft.projecttracking.entity.WorkItemStatus;

import java.time.LocalDate;

public class DashboardRiskyWorkItemResponse {

    private Long workItemId;
    private String title;
    private WorkItemStatus status;
    private String responsiblePerson;
    private String description;

    private Long weeklyReportId;
    private LocalDate reportWeekStartDate;

    private Long projectId;
    private String projectName;
    private ProjectStatus projectStatus;

    public DashboardRiskyWorkItemResponse() {
    }

    public Long getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(Long workItemId) {
        this.workItemId = workItemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public WorkItemStatus getStatus() {
        return status;
    }

    public void setStatus(WorkItemStatus status) {
        this.status = status;
    }

    public String getResponsiblePerson() {
        return responsiblePerson;
    }

    public void setResponsiblePerson(
            String responsiblePerson
    ) {
        this.responsiblePerson =
                responsiblePerson;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getWeeklyReportId() {
        return weeklyReportId;
    }

    public void setWeeklyReportId(Long weeklyReportId) {
        this.weeklyReportId = weeklyReportId;
    }

    public LocalDate getReportWeekStartDate() {
        return reportWeekStartDate;
    }

    public void setReportWeekStartDate(
            LocalDate reportWeekStartDate
    ) {
        this.reportWeekStartDate =
                reportWeekStartDate;
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

    public ProjectStatus getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(
            ProjectStatus projectStatus
    ) {
        this.projectStatus = projectStatus;
    }
}