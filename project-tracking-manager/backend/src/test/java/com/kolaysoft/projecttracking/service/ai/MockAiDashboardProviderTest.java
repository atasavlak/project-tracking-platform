package com.kolaysoft.projecttracking.service.ai;

import com.kolaysoft.projecttracking.dto.AiDashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.DashboardProjectResponse;
import com.kolaysoft.projecttracking.dto.DashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.ProjectHealthStatus;
import com.kolaysoft.projecttracking.entity.ProjectStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockAiDashboardProviderTest {

    private final MockAiProvider provider =
            new MockAiProvider();

    @Test
    void criticalPortfolioProducesManagementSummary() {
        DashboardSummaryResponse summary =
                new DashboardSummaryResponse();
        summary.setTotalActiveProjects(3);
        summary.setTotalActiveWeeklyReports(3);
        summary.setHealthyProjects(1);
        summary.setNeedsAttentionProjects(1);
        summary.setCriticalProjects(1);
        summary.setTotalActiveWorkItems(12);
        summary.setCompletedWorkItems(6);
        summary.setRiskyWorkItems(1);
        summary.setBlockedWorkItems(1);
        summary.setDelayedWorkItems(1);
        summary.setOpenRiskIssues(3);
        summary.setCriticalRiskIssues(1);
        summary.setOverdueActionItems(2);

        DashboardProjectResponse criticalProject =
                new DashboardProjectResponse();
        criticalProject.setProjectId(10L);
        criticalProject.setProjectName("Mobil Bankacılık");
        criticalProject.setProjectStatus(ProjectStatus.AT_RISK);
        criticalProject.setHealthStatus(ProjectHealthStatus.CRITICAL);
        criticalProject.setHealthScore(35);
        criticalProject.setCriticalRiskIssues(1);
        criticalProject.setOverdueActionItems(2);
        criticalProject.setBlockedWorkItems(1);
        criticalProject.setLatestWeeklyReportId(20L);
        criticalProject.setLatestReportWeekStartDate(
                LocalDate.of(2026, 8, 3)
        );
        criticalProject.setLatestReportRisks(
                "Test cihazı eksikliği canlıya geçişi riske atıyor."
        );

        AiDashboardSummaryContext context =
                new AiDashboardSummaryContext(
                        null,
                        null,
                        summary,
                        List.of(criticalProject),
                        List.of(),
                        List.of(),
                        List.of()
                );

        AiDashboardSummaryResponse response =
                provider.summarizeDashboard(context);

        assertEquals(
                ProjectHealthStatus.CRITICAL,
                response.getOverallStatus()
        );
        assertEquals(3, response.getAnalyzedProjectCount());
        assertFalse(response.getHighlights().isEmpty());
        assertFalse(response.getRecommendations().isEmpty());
        assertEquals(1, response.getAttentionProjects().size());
        assertTrue(
                response.getExecutiveSummary()
                        .contains("3 aktif projenin")
        );
        assertTrue(
                response.getWeeklyReportInsights()
                        .getFirst()
                        .contains("Mobil Bankacılık")
        );
    }

    @Test
    void emptyPortfolioProducesNoReportStatus() {
        DashboardSummaryResponse summary =
                new DashboardSummaryResponse();

        AiDashboardSummaryContext context =
                new AiDashboardSummaryContext(
                        ProjectStatus.COMPLETED,
                        ProjectHealthStatus.NO_REPORT,
                        summary,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of()
                );

        AiDashboardSummaryResponse response =
                provider.summarizeDashboard(context);

        assertEquals(
                ProjectHealthStatus.NO_REPORT,
                response.getOverallStatus()
        );
        assertEquals(0, response.getAnalyzedProjectCount());
        assertTrue(response.getAttentionProjects().isEmpty());
    }
}
