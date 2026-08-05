package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.ProjectStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AiDashboardSummaryResponse {

    private String provider;
    private LocalDateTime analyzedAt;
    private ProjectHealthStatus overallStatus;
    private String executiveSummary;
    private List<String> highlights = new ArrayList<>();
    private List<AiDashboardProjectInsightResponse> attentionProjects =
            new ArrayList<>();
    private List<String> weeklyReportInsights = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();
    private long analyzedProjectCount;
    private long analyzedWeeklyReportCount;
    private long healthyProjectCount;
    private long needsAttentionProjectCount;
    private long criticalProjectCount;
    private long projectsWithoutReportCount;
    private long openRiskIssueCount;
    private long criticalRiskIssueCount;
    private long overdueActionItemCount;
    private long criticalWorkItemCount;
    private ProjectStatus appliedProjectStatus;
    private ProjectHealthStatus appliedHealthStatus;
}
