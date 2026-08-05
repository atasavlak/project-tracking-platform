package com.kolaysoft.projecttracking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DashboardSummaryResponse {

    private long totalActiveProjects;
    private long onTrackProjects;
    private long atRiskProjects;
    private long delayedProjects;

    private long healthyProjects;
    private long needsAttentionProjects;
    private long criticalProjects;
    private long projectsWithoutReport;

    private long totalActiveWeeklyReports;

    private long totalActiveWorkItems;
    private long plannedWorkItems;
    private long inProgressWorkItems;
    private long completedWorkItems;
    private long riskyWorkItems;
    private long blockedWorkItems;
    private long delayedWorkItems;

    private long totalActiveRiskIssues;
    private long openRiskIssues;
    private long criticalRiskIssues;

    private long totalActiveDecisions;
    private long approvedDecisions;
    private long implementedDecisions;

    private long totalActiveActionItems;
    private long openActionItems;
    private long overdueActionItems;
    private long completedActionItems;
}
