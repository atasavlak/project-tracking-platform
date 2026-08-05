package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.ProjectStatus;
import com.kolaysoft.projecttracking.entity.WeeklyReportStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DashboardProjectResponse {

    private Long projectId;
    private String projectName;
    private Long projectManagerId;
    private String projectManagerFullName;
    private ProjectStatus projectStatus;

    private ProjectHealthStatus healthStatus;
    private int healthScore;
    private int completionRate;

    private Long latestWeeklyReportId;
    private LocalDate latestReportWeekStartDate;
    private WeeklyReportStatus latestReportStatus;
    private String latestReportSummary;
    private String latestReportRisks;

    private long totalWorkItems;
    private long completedWorkItems;
    private long inProgressWorkItems;
    private long riskyWorkItems;
    private long blockedWorkItems;
    private long delayedWorkItems;

    private long openRiskIssues;
    private long criticalRiskIssues;
    private long decisionCount;
    private long approvedDecisionCount;
    private long openActionItems;
    private long overdueActionItems;
    private long completedActionItems;
}
