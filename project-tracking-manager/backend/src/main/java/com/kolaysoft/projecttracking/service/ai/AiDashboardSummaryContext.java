package com.kolaysoft.projecttracking.service.ai;

import com.kolaysoft.projecttracking.dto.DashboardCriticalRiskResponse;
import com.kolaysoft.projecttracking.dto.DashboardOverdueActionResponse;
import com.kolaysoft.projecttracking.dto.DashboardProjectResponse;
import com.kolaysoft.projecttracking.dto.DashboardRiskyWorkItemResponse;
import com.kolaysoft.projecttracking.dto.DashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.ProjectHealthStatus;
import com.kolaysoft.projecttracking.entity.ProjectStatus;

import java.util.List;

public record AiDashboardSummaryContext(
        ProjectStatus appliedProjectStatus,
        ProjectHealthStatus appliedHealthStatus,
        DashboardSummaryResponse summary,
        List<DashboardProjectResponse> projects,
        List<DashboardCriticalRiskResponse> criticalRisks,
        List<DashboardOverdueActionResponse> overdueActions,
        List<DashboardRiskyWorkItemResponse> riskyWorkItems
) {
}
