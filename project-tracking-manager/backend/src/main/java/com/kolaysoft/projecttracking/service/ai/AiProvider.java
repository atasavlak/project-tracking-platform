package com.kolaysoft.projecttracking.service.ai;

import com.kolaysoft.projecttracking.dto.AiDashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.AiWeeklyReportAnalysisResponse;

public interface AiProvider {

    String getProviderName();

    AiWeeklyReportAnalysisResponse analyze(
            AiWeeklyReportAnalysisContext context
    );

    AiDashboardSummaryResponse summarizeDashboard(
            AiDashboardSummaryContext context
    );
}
