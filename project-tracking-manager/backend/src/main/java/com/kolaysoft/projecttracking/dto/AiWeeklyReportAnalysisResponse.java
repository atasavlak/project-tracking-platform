package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.WeeklyReportStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AiWeeklyReportAnalysisResponse {

    private String provider;
    private LocalDateTime analyzedAt;
    private WeeklyReportStatus suggestedStatus;
    private String executiveSummary;
    private List<String> detectedRisks = new ArrayList<>();
    private List<String> suggestedActions = new ArrayList<>();
    private List<String> indicators = new ArrayList<>();
    private int totalWorkItems;
    private int completedWorkItems;
    private int atRiskWorkItems;
    private int blockedWorkItems;
    private int delayedWorkItems;
    private int openHighRiskIssues;
    private int openCriticalRiskIssues;
    private int overdueActionItems;

    public AiWeeklyReportAnalysisResponse() {
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public WeeklyReportStatus getSuggestedStatus() {
        return suggestedStatus;
    }

    public void setSuggestedStatus(WeeklyReportStatus suggestedStatus) {
        this.suggestedStatus = suggestedStatus;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public List<String> getDetectedRisks() {
        return detectedRisks;
    }

    public void setDetectedRisks(List<String> detectedRisks) {
        this.detectedRisks = detectedRisks;
    }

    public List<String> getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(List<String> suggestedActions) {
        this.suggestedActions = suggestedActions;
    }

    public List<String> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<String> indicators) {
        this.indicators = indicators;
    }

    public int getTotalWorkItems() {
        return totalWorkItems;
    }

    public void setTotalWorkItems(int totalWorkItems) {
        this.totalWorkItems = totalWorkItems;
    }

    public int getCompletedWorkItems() {
        return completedWorkItems;
    }

    public void setCompletedWorkItems(int completedWorkItems) {
        this.completedWorkItems = completedWorkItems;
    }

    public int getAtRiskWorkItems() {
        return atRiskWorkItems;
    }

    public void setAtRiskWorkItems(int atRiskWorkItems) {
        this.atRiskWorkItems = atRiskWorkItems;
    }

    public int getBlockedWorkItems() {
        return blockedWorkItems;
    }

    public void setBlockedWorkItems(int blockedWorkItems) {
        this.blockedWorkItems = blockedWorkItems;
    }

    public int getDelayedWorkItems() {
        return delayedWorkItems;
    }

    public void setDelayedWorkItems(int delayedWorkItems) {
        this.delayedWorkItems = delayedWorkItems;
    }

    public int getOpenHighRiskIssues() {
        return openHighRiskIssues;
    }

    public void setOpenHighRiskIssues(int openHighRiskIssues) {
        this.openHighRiskIssues = openHighRiskIssues;
    }

    public int getOpenCriticalRiskIssues() {
        return openCriticalRiskIssues;
    }

    public void setOpenCriticalRiskIssues(int openCriticalRiskIssues) {
        this.openCriticalRiskIssues = openCriticalRiskIssues;
    }

    public int getOverdueActionItems() {
        return overdueActionItems;
    }

    public void setOverdueActionItems(int overdueActionItems) {
        this.overdueActionItems = overdueActionItems;
    }
}
