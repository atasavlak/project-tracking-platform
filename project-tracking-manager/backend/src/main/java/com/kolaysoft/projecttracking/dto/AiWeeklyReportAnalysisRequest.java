package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.WeeklyReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AiWeeklyReportAnalysisRequest {

    @NotNull(message = "Proje ID değeri zorunludur.")
    private Long projectId;

    private WeeklyReportStatus currentStatus;

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

    public AiWeeklyReportAnalysisRequest() {
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public WeeklyReportStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(WeeklyReportStatus currentStatus) {
        this.currentStatus = currentStatus;
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
