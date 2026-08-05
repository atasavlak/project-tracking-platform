package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.WeeklyReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateWeeklyReportRequest {

    @NotNull(message = "Proje ID değeri zorunludur.")
    private Long projectId;

    @NotNull(message = "Hafta başlangıç tarihi zorunludur.")
    private LocalDate weekStartDate;

    @NotNull(message = "Hafta bitiş tarihi zorunludur.")
    private LocalDate weekEndDate;

    private WeeklyReportStatus status;

    @NotBlank(message = "Haftalık rapor özeti boş bırakılamaz.")
    @Size(
            max = 2000,
            message = "Haftalık rapor özeti en fazla 2000 karakter olabilir."
    )
    private String summary;

    @Size(
            max = 2000,
            message = "Tamamlanan işler en fazla 2000 karakter olabilir."
    )
    private String completedWork;

    @Size(
            max = 2000,
            message = "Gelecek hafta planı en fazla 2000 karakter olabilir."
    )
    private String nextWeekPlan;

    @Size(
            max = 2000,
            message = "Risk açıklaması en fazla 2000 karakter olabilir."
    )
    private String risks;

    public CreateWeeklyReportRequest() {
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public LocalDate getWeekEndDate() {
        return weekEndDate;
    }

    public void setWeekEndDate(LocalDate weekEndDate) {
        this.weekEndDate = weekEndDate;
    }

    public WeeklyReportStatus getStatus() {
        return status;
    }

    public void setStatus(WeeklyReportStatus status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCompletedWork() {
        return completedWork;
    }

    public void setCompletedWork(String completedWork) {
        this.completedWork = completedWork;
    }

    public String getNextWeekPlan() {
        return nextWeekPlan;
    }

    public void setNextWeekPlan(String nextWeekPlan) {
        this.nextWeekPlan = nextWeekPlan;
    }

    public String getRisks() {
        return risks;
    }

    public void setRisks(String risks) {
        this.risks = risks;
    }
}