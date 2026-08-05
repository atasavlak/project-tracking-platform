package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.AiDashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.AiWeeklyReportAnalysisRequest;
import com.kolaysoft.projecttracking.dto.AiWeeklyReportAnalysisResponse;
import com.kolaysoft.projecttracking.dto.DashboardCriticalRiskResponse;
import com.kolaysoft.projecttracking.dto.DashboardOverdueActionResponse;
import com.kolaysoft.projecttracking.dto.DashboardProjectResponse;
import com.kolaysoft.projecttracking.dto.DashboardRiskyWorkItemResponse;
import com.kolaysoft.projecttracking.dto.DashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.ProjectHealthStatus;
import com.kolaysoft.projecttracking.entity.ActionItem;
import com.kolaysoft.projecttracking.entity.ActionItemStatus;
import com.kolaysoft.projecttracking.entity.Project;
import com.kolaysoft.projecttracking.entity.ProjectStatus;
import com.kolaysoft.projecttracking.entity.RiskIssue;
import com.kolaysoft.projecttracking.entity.RiskIssueSeverity;
import com.kolaysoft.projecttracking.entity.RiskIssueStatus;
import com.kolaysoft.projecttracking.entity.WorkItem;
import com.kolaysoft.projecttracking.entity.WorkItemStatus;
import com.kolaysoft.projecttracking.exception.BusinessRuleException;
import com.kolaysoft.projecttracking.exception.ProjectNotFoundException;
import com.kolaysoft.projecttracking.repository.ActionItemRepository;
import com.kolaysoft.projecttracking.repository.ProjectRepository;
import com.kolaysoft.projecttracking.repository.RiskIssueRepository;
import com.kolaysoft.projecttracking.repository.WorkItemRepository;
import com.kolaysoft.projecttracking.security.AppUserPrincipal;
import com.kolaysoft.projecttracking.security.CurrentUserService;
import com.kolaysoft.projecttracking.service.ai.AiDashboardSummaryContext;
import com.kolaysoft.projecttracking.service.ai.AiProvider;
import com.kolaysoft.projecttracking.service.ai.AiWeeklyReportAnalysisContext;
import com.kolaysoft.projecttracking.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiAnalysisService {

    private static final List<RiskIssueStatus> OPEN_RISK_STATUSES =
            List.of(
                    RiskIssueStatus.OPEN,
                    RiskIssueStatus.IN_PROGRESS
            );

    private final ProjectRepository projectRepository;
    private final WorkItemRepository workItemRepository;
    private final RiskIssueRepository riskIssueRepository;
    private final ActionItemRepository actionItemRepository;
    private final CurrentUserService currentUserService;
    private final DashboardService dashboardService;
    private final AiProvider aiProvider;

    public AiWeeklyReportAnalysisResponse analyzeWeeklyReport(
            AiWeeklyReportAnalysisRequest request
    ) {
        Project project = projectRepository
                .findById(request.getProjectId())
                .filter(Project::isActive)
                .orElseThrow(
                        () -> new ProjectNotFoundException(
                                request.getProjectId()
                        )
                );

        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        ensureCanAnalyzeProject(
                project,
                currentUser
        );
        validateInput(request);

        Long projectManagerFilter =
                currentUser.getRole()
                        == UserRole.PROJECT_MANAGER
                        ? currentUser.getId()
                        : null;

        List<WorkItem> workItems =
                workItemRepository.searchVisibleWorkItems(
                        project.getId(),
                        null,
                        null,
                        null,
                        projectManagerFilter
                );

        List<RiskIssue> riskIssues =
                riskIssueRepository.searchVisibleRiskIssues(
                        project.getId(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        projectManagerFilter
                );

        List<ActionItem> actionItems =
                actionItemRepository.searchVisibleActionItems(
                        project.getId(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        projectManagerFilter
                );

        AiWeeklyReportAnalysisContext context =
                buildContext(
                        request,
                        project,
                        workItems,
                        riskIssues,
                        actionItems
                );

        AiWeeklyReportAnalysisResponse response =
                aiProvider.analyze(context);

        response.setProvider(
                aiProvider.getProviderName()
        );
        response.setAnalyzedAt(
                LocalDateTime.now()
        );

        return response;
    }

    public AiDashboardSummaryResponse summarizeDashboard(
            ProjectStatus projectStatus,
            ProjectHealthStatus healthStatus
    ) {
        List<DashboardProjectResponse> projects =
                dashboardService.getProjects(
                        projectStatus,
                        healthStatus
                );

        Set<Long> projectIds = projects.stream()
                .map(DashboardProjectResponse::getProjectId)
                .collect(Collectors.toUnmodifiableSet());

        DashboardSummaryResponse summary =
                dashboardService.getSummaryForProjects(projects);

        List<DashboardCriticalRiskResponse> criticalRisks =
                dashboardService.getCriticalRisksForProjects(
                        projectIds
                );

        List<DashboardOverdueActionResponse> overdueActions =
                dashboardService.getOverdueActionsForProjects(
                        projectIds
                );

        List<DashboardRiskyWorkItemResponse> riskyWorkItems =
                dashboardService.getRiskyWorkItemsForProjects(
                        projectIds
                );

        AiDashboardSummaryContext context =
                new AiDashboardSummaryContext(
                        projectStatus,
                        healthStatus,
                        summary,
                        projects,
                        criticalRisks,
                        overdueActions,
                        riskyWorkItems
                );

        AiDashboardSummaryResponse response =
                aiProvider.summarizeDashboard(context);

        response.setProvider(
                aiProvider.getProviderName()
        );
        response.setAnalyzedAt(
                LocalDateTime.now()
        );

        return response;
    }

    private AiWeeklyReportAnalysisContext buildContext(
            AiWeeklyReportAnalysisRequest request,
            Project project,
            List<WorkItem> workItems,
            List<RiskIssue> riskIssues,
            List<ActionItem> actionItems
    ) {
        int completedWorkItems = countWorkItems(
                workItems,
                WorkItemStatus.COMPLETED
        );
        int inProgressWorkItems = countWorkItems(
                workItems,
                WorkItemStatus.IN_PROGRESS
        );
        int atRiskWorkItems = countWorkItems(
                workItems,
                WorkItemStatus.AT_RISK
        );
        int blockedWorkItems = countWorkItems(
                workItems,
                WorkItemStatus.BLOCKED
        );
        int delayedWorkItems = countWorkItems(
                workItems,
                WorkItemStatus.DELAYED
        );

        List<RiskIssue> openRiskIssues = riskIssues
                .stream()
                .filter(riskIssue ->
                        OPEN_RISK_STATUSES.contains(
                                riskIssue.getStatus()
                        )
                )
                .toList();

        int openHighRiskIssues = Math.toIntExact(
                openRiskIssues
                        .stream()
                        .filter(riskIssue ->
                                riskIssue.getSeverity()
                                        == RiskIssueSeverity.HIGH
                        )
                        .count()
        );

        int openCriticalRiskIssues = Math.toIntExact(
                openRiskIssues
                        .stream()
                        .filter(riskIssue ->
                                riskIssue.getSeverity()
                                        == RiskIssueSeverity.CRITICAL
                        )
                        .count()
        );

        List<ActionItem> overdueActions = actionItems
                .stream()
                .filter(this::isOverdueAction)
                .toList();

        List<String> openRiskTitles = openRiskIssues
                .stream()
                .filter(riskIssue ->
                        riskIssue.getSeverity()
                                == RiskIssueSeverity.HIGH
                                || riskIssue.getSeverity()
                                == RiskIssueSeverity.CRITICAL
                )
                .map(RiskIssue::getTitle)
                .distinct()
                .limit(5)
                .toList();

        List<String> overdueActionTitles = overdueActions
                .stream()
                .map(ActionItem::getTitle)
                .distinct()
                .limit(5)
                .toList();

        return new AiWeeklyReportAnalysisContext(
                project.getId(),
                project.getName(),
                request.getCurrentStatus(),
                normalizeOptionalText(request.getSummary()),
                normalizeOptionalText(
                        request.getCompletedWork()
                ),
                normalizeOptionalText(
                        request.getNextWeekPlan()
                ),
                normalizeOptionalText(request.getRisks()),
                workItems.size(),
                completedWorkItems,
                inProgressWorkItems,
                atRiskWorkItems,
                blockedWorkItems,
                delayedWorkItems,
                openHighRiskIssues,
                openCriticalRiskIssues,
                overdueActions.size(),
                openRiskTitles,
                overdueActionTitles
        );
    }

    private int countWorkItems(
            List<WorkItem> workItems,
            WorkItemStatus status
    ) {
        return Math.toIntExact(
                workItems
                        .stream()
                        .filter(workItem ->
                                workItem.getStatus() == status
                        )
                        .count()
        );
    }

    private boolean isOverdueAction(
            ActionItem actionItem
    ) {
        if (actionItem.getStatus()
                == ActionItemStatus.OVERDUE) {

            return true;
        }

        return (actionItem.getStatus()
                == ActionItemStatus.OPEN
                || actionItem.getStatus()
                == ActionItemStatus.IN_PROGRESS)
                && actionItem.getTargetDate()
                .isBefore(LocalDate.now());
    }

    private void ensureCanAnalyzeProject(
            Project project,
            AppUserPrincipal currentUser
    ) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        if (currentUser.getRole()
                == UserRole.PROJECT_MANAGER
                && project
                .getProjectManager()
                .getId()
                .equals(currentUser.getId())) {

            return;
        }

        throw new AccessDeniedException(
                "Yalnızca sorumlu olduğunuz proje için rapor analizi oluşturabilirsiniz."
        );
    }

    private void validateInput(
            AiWeeklyReportAnalysisRequest request
    ) {
        if (!hasText(request.getSummary())
                && !hasText(request.getCompletedWork())
                && !hasText(request.getNextWeekPlan())
                && !hasText(request.getRisks())) {

            throw new BusinessRuleException(
                    "Analiz için özet, tamamlanan işler, gelecek hafta planı veya risk alanlarından en az biri doldurulmalıdır."
            );
        }
    }

    private String normalizeOptionalText(String value) {
        if (!hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private boolean hasText(String value) {
        return value != null
                && !value.isBlank();
    }
}
