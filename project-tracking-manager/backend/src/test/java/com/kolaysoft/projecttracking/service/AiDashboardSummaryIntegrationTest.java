package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.AiDashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.DashboardProjectResponse;
import com.kolaysoft.projecttracking.dto.ProjectHealthStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AiDashboardSummaryIntegrationTest {

    @Autowired
    private AiAnalysisService aiAnalysisService;

    @Autowired
    private DashboardService dashboardService;

    @Test
    void dashboardSummaryUsesFilteredProjects() {
        List<DashboardProjectResponse> criticalProjects =
                dashboardService.getProjects(
                        null,
                        ProjectHealthStatus.CRITICAL
                );

        AiDashboardSummaryResponse response =
                aiAnalysisService.summarizeDashboard(
                        null,
                        ProjectHealthStatus.CRITICAL
                );

        assertEquals(
                criticalProjects.size(),
                response.getAnalyzedProjectCount()
        );
        assertEquals(
                ProjectHealthStatus.CRITICAL,
                response.getAppliedHealthStatus()
        );
        assertNotNull(response.getProvider());
        assertNotNull(response.getAnalyzedAt());
        assertNotNull(response.getExecutiveSummary());
        assertTrue(
                response.getAttentionProjects()
                        .stream()
                        .allMatch(project ->
                                project.getHealthStatus()
                                        == ProjectHealthStatus.CRITICAL
                        )
        );
    }
}
