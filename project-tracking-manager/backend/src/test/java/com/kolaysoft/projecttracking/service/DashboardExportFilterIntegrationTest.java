package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.DashboardProjectResponse;
import com.kolaysoft.projecttracking.dto.DashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.ProjectHealthStatus;
import com.kolaysoft.projecttracking.entity.WorkItem;
import com.kolaysoft.projecttracking.repository.WorkItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DashboardExportFilterIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private WorkItemRepository workItemRepository;

    @Test
    void filteredReportDataUsesOnlyMatchingProjects() {
        List<DashboardProjectResponse> projects =
                dashboardService.getProjects(
                        null,
                        ProjectHealthStatus.CRITICAL
                );

        Set<Long> projectIds = projects.stream()
                .map(DashboardProjectResponse::getProjectId)
                .collect(Collectors.toSet());

        DashboardSummaryResponse summary =
                dashboardService.getSummaryForProjects(projects);

        long expectedWorkItemCount = workItemRepository
                .findVisibleActiveWorkItemsOrderByCreatedAtDesc()
                .stream()
                .map(WorkItem::getWeeklyReport)
                .filter(report ->
                        projectIds.contains(report.getProject().getId())
                )
                .count();

        assertEquals(projects.size(), summary.getTotalActiveProjects());
        assertEquals(projects.size(), summary.getCriticalProjects());
        assertEquals(0, summary.getHealthyProjects());
        assertEquals(0, summary.getNeedsAttentionProjects());
        assertEquals(0, summary.getProjectsWithoutReport());
        assertEquals(expectedWorkItemCount, summary.getTotalActiveWorkItems());

        assertTrue(
                dashboardService
                        .getCriticalRisksForProjects(projectIds)
                        .stream()
                        .allMatch(risk ->
                                projectIds.contains(risk.getProjectId())
                        )
        );
        assertTrue(
                dashboardService
                        .getOverdueActionsForProjects(projectIds)
                        .stream()
                        .allMatch(action ->
                                projectIds.contains(action.getProjectId())
                        )
        );
        assertTrue(
                dashboardService
                        .getRiskyWorkItemsForProjects(projectIds)
                        .stream()
                        .allMatch(workItem ->
                                projectIds.contains(workItem.getProjectId())
                        )
        );
    }

    @Test
    void emptyProjectFilterProducesEmptyReportData() {
        DashboardSummaryResponse summary =
                dashboardService.getSummaryForProjects(List.of());

        assertEquals(0, summary.getTotalActiveProjects());
        assertEquals(0, summary.getTotalActiveWeeklyReports());
        assertEquals(0, summary.getTotalActiveWorkItems());
        assertEquals(0, summary.getTotalActiveRiskIssues());
        assertEquals(0, summary.getTotalActiveDecisions());
        assertEquals(0, summary.getTotalActiveActionItems());
        assertTrue(
                dashboardService
                        .getCriticalRisksForProjects(Set.of())
                        .isEmpty()
        );
        assertTrue(
                dashboardService
                        .getOverdueActionsForProjects(Set.of())
                        .isEmpty()
        );
        assertTrue(
                dashboardService
                        .getRiskyWorkItemsForProjects(Set.of())
                        .isEmpty()
        );
    }
}
