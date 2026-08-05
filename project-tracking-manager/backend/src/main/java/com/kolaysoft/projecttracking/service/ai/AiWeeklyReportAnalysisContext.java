package com.kolaysoft.projecttracking.service.ai;

import com.kolaysoft.projecttracking.entity.WeeklyReportStatus;

import java.util.List;

public record AiWeeklyReportAnalysisContext(
        Long projectId,
        String projectName,
        WeeklyReportStatus currentStatus,
        String summary,
        String completedWork,
        String nextWeekPlan,
        String risks,
        int totalWorkItems,
        int completedWorkItems,
        int inProgressWorkItems,
        int atRiskWorkItems,
        int blockedWorkItems,
        int delayedWorkItems,
        int openHighRiskIssues,
        int openCriticalRiskIssues,
        int overdueActionItems,
        List<String> openRiskTitles,
        List<String> overdueActionTitles
) {
}
