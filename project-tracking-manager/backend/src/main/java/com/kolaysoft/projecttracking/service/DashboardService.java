package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.DashboardCriticalRiskResponse;
import com.kolaysoft.projecttracking.dto.DashboardOverdueActionResponse;
import com.kolaysoft.projecttracking.dto.DashboardProjectResponse;
import com.kolaysoft.projecttracking.dto.DashboardRiskyWorkItemResponse;
import com.kolaysoft.projecttracking.dto.DashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.ProjectHealthStatus;
import com.kolaysoft.projecttracking.entity.ActionItem;
import com.kolaysoft.projecttracking.entity.ActionItemStatus;
import com.kolaysoft.projecttracking.entity.DecisionLog;
import com.kolaysoft.projecttracking.entity.DecisionStatus;
import com.kolaysoft.projecttracking.entity.Project;
import com.kolaysoft.projecttracking.entity.ProjectStatus;
import com.kolaysoft.projecttracking.entity.RiskIssue;
import com.kolaysoft.projecttracking.entity.RiskIssueSeverity;
import com.kolaysoft.projecttracking.entity.RiskIssueStatus;
import com.kolaysoft.projecttracking.entity.WeeklyReport;
import com.kolaysoft.projecttracking.entity.WeeklyReportStatus;
import com.kolaysoft.projecttracking.entity.WorkItem;
import com.kolaysoft.projecttracking.entity.WorkItemStatus;
import com.kolaysoft.projecttracking.repository.ActionItemRepository;
import com.kolaysoft.projecttracking.repository.DecisionLogRepository;
import com.kolaysoft.projecttracking.repository.ProjectRepository;
import com.kolaysoft.projecttracking.repository.RiskIssueRepository;
import com.kolaysoft.projecttracking.repository.WeeklyReportRepository;
import com.kolaysoft.projecttracking.repository.WorkItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final List<WorkItemStatus> RISKY_STATUSES =
            List.of(
                    WorkItemStatus.AT_RISK,
                    WorkItemStatus.BLOCKED,
                    WorkItemStatus.DELAYED
            );

    private static final List<RiskIssueStatus> OPEN_RISK_STATUSES =
            List.of(
                    RiskIssueStatus.OPEN,
                    RiskIssueStatus.IN_PROGRESS
            );

    private final ProjectRepository projectRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final WorkItemRepository workItemRepository;
    private final RiskIssueRepository riskIssueRepository;
    private final DecisionLogRepository decisionLogRepository;
    private final ActionItemRepository actionItemRepository;

    public DashboardService(
            ProjectRepository projectRepository,
            WeeklyReportRepository weeklyReportRepository,
            WorkItemRepository workItemRepository,
            RiskIssueRepository riskIssueRepository,
            DecisionLogRepository decisionLogRepository,
            ActionItemRepository actionItemRepository
    ) {
        this.projectRepository = projectRepository;
        this.weeklyReportRepository = weeklyReportRepository;
        this.workItemRepository = workItemRepository;
        this.riskIssueRepository = riskIssueRepository;
        this.decisionLogRepository = decisionLogRepository;
        this.actionItemRepository = actionItemRepository;
    }

    public DashboardSummaryResponse getSummary() {
        return getSummary(null, null);
    }

    public DashboardSummaryResponse getSummary(
            ProjectStatus status,
            ProjectHealthStatus healthStatus
    ) {
        return getSummaryForProjects(
                getProjects(status, healthStatus)
        );
    }

    public DashboardSummaryResponse getSummaryForProjects(
            List<DashboardProjectResponse> projectResponses
    ) {
        Set<Long> projectIds = projectResponses.stream()
                .map(DashboardProjectResponse::getProjectId)
                .collect(Collectors.toUnmodifiableSet());

        List<WeeklyReport> weeklyReports = weeklyReportRepository
                .findVisibleActiveReportsOrderByWeekStartDateDesc()
                .stream()
                .filter(report ->
                        projectIds.contains(report.getProject().getId())
                )
                .toList();

        List<WorkItem> workItems = workItemRepository
                .findVisibleActiveWorkItemsOrderByCreatedAtDesc()
                .stream()
                .filter(workItem ->
                        projectIds.contains(
                                workItem.getWeeklyReport()
                                        .getProject()
                                        .getId()
                        )
                )
                .toList();

        List<RiskIssue> riskIssues = getAllRiskIssues().stream()
                .filter(riskIssue ->
                        projectIds.contains(
                                riskIssue.getWeeklyReport()
                                        .getProject()
                                        .getId()
                        )
                )
                .toList();

        List<DecisionLog> decisions = getAllDecisions().stream()
                .filter(decision ->
                        projectIds.contains(
                                decision.getProject().getId()
                        )
                )
                .toList();

        List<ActionItem> actionItems = getAllActionItems().stream()
                .filter(actionItem ->
                        projectIds.contains(
                                actionItem.getProject().getId()
                        )
                )
                .toList();

        DashboardSummaryResponse response =
                new DashboardSummaryResponse();

        response.setTotalActiveProjects(projectResponses.size());
        response.setOnTrackProjects(
                projectResponses.stream()
                        .filter(project ->
                                project.getProjectStatus()
                                        == ProjectStatus.ON_TRACK
                        )
                        .count()
        );
        response.setAtRiskProjects(
                projectResponses.stream()
                        .filter(project ->
                                project.getProjectStatus()
                                        == ProjectStatus.AT_RISK
                        )
                        .count()
        );
        response.setDelayedProjects(
                projectResponses.stream()
                        .filter(project ->
                                project.getProjectStatus()
                                        == ProjectStatus.DELAYED
                        )
                        .count()
        );

        response.setHealthyProjects(
                countProjectsByHealth(
                        projectResponses,
                        ProjectHealthStatus.HEALTHY
                )
        );
        response.setNeedsAttentionProjects(
                countProjectsByHealth(
                        projectResponses,
                        ProjectHealthStatus.NEEDS_ATTENTION
                )
        );
        response.setCriticalProjects(
                countProjectsByHealth(
                        projectResponses,
                        ProjectHealthStatus.CRITICAL
                )
        );
        response.setProjectsWithoutReport(
                countProjectsByHealth(
                        projectResponses,
                        ProjectHealthStatus.NO_REPORT
                )
        );

        response.setTotalActiveWeeklyReports(weeklyReports.size());

        response.setTotalActiveWorkItems(workItems.size());
        response.setPlannedWorkItems(
                countByStatus(workItems, WorkItemStatus.PLANNED)
        );
        response.setInProgressWorkItems(
                countByStatus(workItems, WorkItemStatus.IN_PROGRESS)
        );
        response.setCompletedWorkItems(
                countByStatus(workItems, WorkItemStatus.COMPLETED)
        );
        response.setRiskyWorkItems(
                countByStatus(workItems, WorkItemStatus.AT_RISK)
        );
        response.setBlockedWorkItems(
                countByStatus(workItems, WorkItemStatus.BLOCKED)
        );
        response.setDelayedWorkItems(
                countByStatus(workItems, WorkItemStatus.DELAYED)
        );

        response.setTotalActiveRiskIssues(riskIssues.size());
        response.setOpenRiskIssues(
                riskIssues.stream()
                        .filter(this::isOpenRisk)
                        .count()
        );
        response.setCriticalRiskIssues(
                riskIssues.stream()
                        .filter(this::isOpenRisk)
                        .filter(riskIssue ->
                                riskIssue.getSeverity()
                                        == RiskIssueSeverity.CRITICAL
                        )
                        .count()
        );

        response.setTotalActiveDecisions(decisions.size());
        response.setApprovedDecisions(
                decisions.stream()
                        .filter(decision ->
                                decision.getStatus()
                                        == DecisionStatus.APPROVED
                        )
                        .count()
        );
        response.setImplementedDecisions(
                decisions.stream()
                        .filter(decision ->
                                decision.getStatus()
                                        == DecisionStatus.IMPLEMENTED
                        )
                        .count()
        );

        response.setTotalActiveActionItems(actionItems.size());
        response.setOpenActionItems(
                actionItems.stream()
                        .filter(actionItem -> {
                            ActionItemStatus status =
                                    getEffectiveActionStatus(actionItem);
                            return status == ActionItemStatus.OPEN
                                    || status
                                    == ActionItemStatus.IN_PROGRESS;
                        })
                        .count()
        );
        response.setOverdueActionItems(
                actionItems.stream()
                        .filter(actionItem ->
                                getEffectiveActionStatus(actionItem)
                                        == ActionItemStatus.OVERDUE
                        )
                        .count()
        );
        response.setCompletedActionItems(
                actionItems.stream()
                        .filter(actionItem ->
                                actionItem.getStatus()
                                        == ActionItemStatus.COMPLETED
                        )
                        .count()
        );

        return response;
    }

    public List<DashboardProjectResponse> getProjects(
            ProjectStatus status,
            ProjectHealthStatus healthStatus
    ) {
        List<Project> projects = status == null
                ? projectRepository.findByActiveTrue()
                : projectRepository.findByStatusAndActiveTrue(status);

        List<DashboardProjectResponse> responses =
                buildProjectResponses(
                        projects,
                        getAllRiskIssues(),
                        getAllDecisions(),
                        getAllActionItems()
                );

        if (healthStatus == null) {
            return responses;
        }

        return responses.stream()
                .filter(project ->
                        project.getHealthStatus() == healthStatus
                )
                .toList();
    }

    public List<DashboardRiskyWorkItemResponse> getRiskyWorkItems() {
        return getRiskyWorkItems(null, null);
    }

    public List<DashboardRiskyWorkItemResponse> getRiskyWorkItems(
            ProjectStatus status,
            ProjectHealthStatus healthStatus
    ) {
        return getRiskyWorkItemsForProjects(
                getProjectIds(status, healthStatus)
        );
    }

    public List<DashboardRiskyWorkItemResponse>
    getRiskyWorkItemsForProjects(Set<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return List.of();
        }

        return workItemRepository
                .findVisibleActiveWorkItemsByStatusInOrderByCreatedAtDesc(
                        RISKY_STATUSES
                )
                .stream()
                .filter(workItem ->
                        projectIds.contains(
                                workItem.getWeeklyReport()
                                        .getProject()
                                        .getId()
                        )
                )
                .map(this::toRiskyWorkItemResponse)
                .toList();
    }

    public List<DashboardCriticalRiskResponse> getCriticalRisks() {
        return getCriticalRisks(null, null);
    }

    public List<DashboardCriticalRiskResponse> getCriticalRisks(
            ProjectStatus status,
            ProjectHealthStatus healthStatus
    ) {
        return getCriticalRisksForProjects(
                getProjectIds(status, healthStatus)
        );
    }

    public List<DashboardCriticalRiskResponse>
    getCriticalRisksForProjects(Set<Long> projectIds) {
        return getAllRiskIssues().stream()
                .filter(riskIssue ->
                        projectIds == null
                                || projectIds.contains(
                                riskIssue.getWeeklyReport()
                                        .getProject()
                                        .getId()
                        )
                )
                .filter(this::isOpenRisk)
                .filter(riskIssue ->
                        riskIssue.getSeverity()
                                == RiskIssueSeverity.HIGH
                                || riskIssue.getSeverity()
                                == RiskIssueSeverity.CRITICAL
                )
                .sorted(
                        Comparator
                                .comparing(
                                        RiskIssue::getSeverity,
                                        Comparator.reverseOrder()
                                )
                                .thenComparing(RiskIssue::getFollowUpDate)
                )
                .map(this::toCriticalRiskResponse)
                .toList();
    }

    public List<DashboardOverdueActionResponse> getOverdueActions() {
        return getOverdueActions(null, null);
    }

    public List<DashboardOverdueActionResponse> getOverdueActions(
            ProjectStatus status,
            ProjectHealthStatus healthStatus
    ) {
        return getOverdueActionsForProjects(
                getProjectIds(status, healthStatus)
        );
    }

    public List<DashboardOverdueActionResponse>
    getOverdueActionsForProjects(Set<Long> projectIds) {
        LocalDate today = LocalDate.now();

        return getAllActionItems().stream()
                .filter(actionItem ->
                        projectIds == null
                                || projectIds.contains(
                                actionItem.getProject().getId()
                        )
                )
                .filter(actionItem ->
                        getEffectiveActionStatus(actionItem)
                                == ActionItemStatus.OVERDUE
                )
                .sorted(Comparator.comparing(ActionItem::getTargetDate))
                .map(actionItem ->
                        toOverdueActionResponse(actionItem, today)
                )
                .toList();
    }

    private Set<Long> getProjectIds(
            ProjectStatus status,
            ProjectHealthStatus healthStatus
    ) {
        return getProjects(status, healthStatus)
                .stream()
                .map(DashboardProjectResponse::getProjectId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private List<DashboardProjectResponse> buildProjectResponses(
            List<Project> projects,
            List<RiskIssue> riskIssues,
            List<DecisionLog> decisions,
            List<ActionItem> actionItems
    ) {
        return projects.stream()
                .map(project ->
                        toProjectResponse(
                                project,
                                riskIssues,
                                decisions,
                                actionItems
                        )
                )
                .sorted(
                        Comparator
                                .comparingInt(
                                        DashboardProjectResponse::getHealthScore
                                )
                                .thenComparing(
                                        DashboardProjectResponse::getProjectName,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )
                .toList();
    }

    private DashboardProjectResponse toProjectResponse(
            Project project,
            List<RiskIssue> allRiskIssues,
            List<DecisionLog> allDecisions,
            List<ActionItem> allActionItems
    ) {
        DashboardProjectResponse response =
                new DashboardProjectResponse();

        response.setProjectId(project.getId());
        response.setProjectName(project.getName());
        response.setProjectManagerId(
                project.getProjectManager().getId()
        );
        response.setProjectManagerFullName(
                project.getProjectManager().getFullName()
        );
        response.setProjectStatus(project.getStatus());

        Optional<WeeklyReport> latestReportOptional =
                weeklyReportRepository
                        .findFirstByProject_IdAndActiveTrueOrderByWeekStartDateDesc(
                                project.getId()
                        );

        List<WorkItem> workItems = latestReportOptional
                .map(report ->
                        workItemRepository
                                .findByWeeklyReport_IdAndActiveTrueOrderByCreatedAtAsc(
                                        report.getId()
                                )
                )
                .orElseGet(List::of);

        latestReportOptional.ifPresent(report -> {
            response.setLatestWeeklyReportId(report.getId());
            response.setLatestReportWeekStartDate(
                    report.getWeekStartDate()
            );
            response.setLatestReportStatus(report.getStatus());
            response.setLatestReportSummary(report.getSummary());
            response.setLatestReportRisks(report.getRisks());
        });

        response.setTotalWorkItems(workItems.size());
        response.setCompletedWorkItems(
                countByStatus(workItems, WorkItemStatus.COMPLETED)
        );
        response.setInProgressWorkItems(
                countByStatus(workItems, WorkItemStatus.IN_PROGRESS)
        );
        response.setRiskyWorkItems(
                countByStatus(workItems, WorkItemStatus.AT_RISK)
        );
        response.setBlockedWorkItems(
                countByStatus(workItems, WorkItemStatus.BLOCKED)
        );
        response.setDelayedWorkItems(
                countByStatus(workItems, WorkItemStatus.DELAYED)
        );
        response.setCompletionRate(
                calculateCompletionRate(response)
        );

        List<RiskIssue> projectRisks = allRiskIssues.stream()
                .filter(riskIssue ->
                        riskIssue.getWeeklyReport()
                                .getProject()
                                .getId()
                                .equals(project.getId())
                )
                .toList();

        List<DecisionLog> projectDecisions = allDecisions.stream()
                .filter(decision ->
                        decision.getProject()
                                .getId()
                                .equals(project.getId())
                )
                .toList();

        List<ActionItem> projectActions = allActionItems.stream()
                .filter(actionItem ->
                        actionItem.getProject()
                                .getId()
                                .equals(project.getId())
                )
                .toList();

        response.setOpenRiskIssues(
                projectRisks.stream()
                        .filter(this::isOpenRisk)
                        .count()
        );
        response.setCriticalRiskIssues(
                projectRisks.stream()
                        .filter(this::isOpenRisk)
                        .filter(riskIssue ->
                                riskIssue.getSeverity()
                                        == RiskIssueSeverity.CRITICAL
                        )
                        .count()
        );
        response.setDecisionCount(projectDecisions.size());
        response.setApprovedDecisionCount(
                projectDecisions.stream()
                        .filter(decision ->
                                decision.getStatus()
                                        == DecisionStatus.APPROVED
                                        || decision.getStatus()
                                        == DecisionStatus.IMPLEMENTED
                        )
                        .count()
        );
        response.setOpenActionItems(
                projectActions.stream()
                        .filter(actionItem -> {
                            ActionItemStatus actionStatus =
                                    getEffectiveActionStatus(actionItem);
                            return actionStatus == ActionItemStatus.OPEN
                                    || actionStatus
                                    == ActionItemStatus.IN_PROGRESS;
                        })
                        .count()
        );
        response.setOverdueActionItems(
                projectActions.stream()
                        .filter(actionItem ->
                                getEffectiveActionStatus(actionItem)
                                        == ActionItemStatus.OVERDUE
                        )
                        .count()
        );
        response.setCompletedActionItems(
                projectActions.stream()
                        .filter(actionItem ->
                                actionItem.getStatus()
                                        == ActionItemStatus.COMPLETED
                        )
                        .count()
        );

        int healthScore = calculateHealthScore(
                project,
                latestReportOptional.orElse(null),
                response,
                projectRisks
        );

        response.setHealthScore(healthScore);
        response.setHealthStatus(
                determineHealthStatus(
                        latestReportOptional.orElse(null),
                        response,
                        healthScore
                )
        );

        return response;
    }

    private int calculateHealthScore(
            Project project,
            WeeklyReport latestReport,
            DashboardProjectResponse response,
            List<RiskIssue> projectRisks
    ) {
        int score = 100;

        if (latestReport == null) {
            return 55;
        }

        if (project.getStatus() == ProjectStatus.DELAYED) {
            score -= 30;
        } else if (project.getStatus() == ProjectStatus.AT_RISK) {
            score -= 20;
        } else if (project.getStatus() == ProjectStatus.ON_HOLD) {
            score -= 15;
        }

        if (latestReport.getStatus() == WeeklyReportStatus.DELAYED) {
            score -= 20;
        } else if (
                latestReport.getStatus()
                        == WeeklyReportStatus.AT_RISK
        ) {
            score -= 10;
        }

        long highRiskCount = projectRisks.stream()
                .filter(this::isOpenRisk)
                .filter(riskIssue ->
                        riskIssue.getSeverity()
                                == RiskIssueSeverity.HIGH
                )
                .count();

        score -= Math.min(
                30,
                Math.toIntExact(response.getCriticalRiskIssues() * 15)
        );
        score -= Math.min(
                16,
                Math.toIntExact(highRiskCount * 8)
        );
        score -= Math.min(
                30,
                Math.toIntExact(response.getOverdueActionItems() * 10)
        );
        score -= Math.min(
                20,
                Math.toIntExact(response.getBlockedWorkItems() * 10)
        );
        score -= Math.min(
                16,
                Math.toIntExact(response.getDelayedWorkItems() * 8)
        );
        score -= Math.min(
                10,
                Math.toIntExact(response.getRiskyWorkItems() * 5)
        );

        return Math.max(0, score);
    }

    private ProjectHealthStatus determineHealthStatus(
            WeeklyReport latestReport,
            DashboardProjectResponse response,
            int healthScore
    ) {
        if (latestReport == null) {
            return ProjectHealthStatus.NO_REPORT;
        }

        if (healthScore < 50
                || response.getCriticalRiskIssues() > 0
                || response.getOverdueActionItems() > 0
                || response.getBlockedWorkItems() > 0
                || response.getDelayedWorkItems() > 0) {
            return ProjectHealthStatus.CRITICAL;
        }

        if (healthScore < 80
                || response.getOpenRiskIssues() > 0
                || response.getRiskyWorkItems() > 0) {
            return ProjectHealthStatus.NEEDS_ATTENTION;
        }

        return ProjectHealthStatus.HEALTHY;
    }

    private DashboardRiskyWorkItemResponse toRiskyWorkItemResponse(
            WorkItem workItem
    ) {
        DashboardRiskyWorkItemResponse response =
                new DashboardRiskyWorkItemResponse();

        response.setWorkItemId(workItem.getId());
        response.setTitle(workItem.getTitle());
        response.setDescription(workItem.getDescription());
        response.setStatus(workItem.getStatus());
        response.setResponsiblePerson(
                workItem.getResponsiblePerson()
        );
        response.setWeeklyReportId(
                workItem.getWeeklyReport().getId()
        );
        response.setReportWeekStartDate(
                workItem.getWeeklyReport().getWeekStartDate()
        );
        response.setProjectId(
                workItem.getWeeklyReport().getProject().getId()
        );
        response.setProjectName(
                workItem.getWeeklyReport().getProject().getName()
        );
        response.setProjectStatus(
                workItem.getWeeklyReport().getProject().getStatus()
        );

        return response;
    }

    private DashboardCriticalRiskResponse toCriticalRiskResponse(
            RiskIssue riskIssue
    ) {
        DashboardCriticalRiskResponse response =
                new DashboardCriticalRiskResponse();

        response.setRiskIssueId(riskIssue.getId());
        response.setProjectId(
                riskIssue.getWeeklyReport().getProject().getId()
        );
        response.setProjectName(
                riskIssue.getWeeklyReport().getProject().getName()
        );
        response.setType(riskIssue.getType());
        response.setTitle(riskIssue.getTitle());
        response.setSeverity(riskIssue.getSeverity());
        response.setStatus(riskIssue.getStatus());
        response.setResponsibleUserId(
                riskIssue.getResponsibleUser().getId()
        );
        response.setResponsibleUserFullName(
                riskIssue.getResponsibleUser().getFullName()
        );
        response.setFollowUpDate(riskIssue.getFollowUpDate());

        return response;
    }

    private DashboardOverdueActionResponse toOverdueActionResponse(
            ActionItem actionItem,
            LocalDate today
    ) {
        DashboardOverdueActionResponse response =
                new DashboardOverdueActionResponse();

        response.setActionItemId(actionItem.getId());
        response.setProjectId(actionItem.getProject().getId());
        response.setProjectName(actionItem.getProject().getName());
        response.setTitle(actionItem.getTitle());
        response.setPriority(actionItem.getPriority());
        response.setStatus(ActionItemStatus.OVERDUE);
        response.setResponsibleUserId(
                actionItem.getResponsibleUser().getId()
        );
        response.setResponsibleUserFullName(
                actionItem.getResponsibleUser().getFullName()
        );
        response.setTargetDate(actionItem.getTargetDate());
        response.setOverdueDays(
                Math.max(
                        0,
                        ChronoUnit.DAYS.between(
                                actionItem.getTargetDate(),
                                today
                        )
                )
        );

        return response;
    }

    private List<RiskIssue> getAllRiskIssues() {
        return riskIssueRepository.searchVisibleRiskIssues(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private List<DecisionLog> getAllDecisions() {
        return decisionLogRepository.searchVisibleDecisionLogs(
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private List<ActionItem> getAllActionItems() {
        return actionItemRepository.searchVisibleActionItems(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private boolean isOpenRisk(RiskIssue riskIssue) {
        return OPEN_RISK_STATUSES.contains(riskIssue.getStatus());
    }

    private ActionItemStatus getEffectiveActionStatus(
            ActionItem actionItem
    ) {
        if ((actionItem.getStatus() == ActionItemStatus.OPEN
                || actionItem.getStatus()
                == ActionItemStatus.IN_PROGRESS)
                && actionItem.getTargetDate()
                .isBefore(LocalDate.now())) {
            return ActionItemStatus.OVERDUE;
        }

        return actionItem.getStatus();
    }

    private long countProjectsByHealth(
            List<DashboardProjectResponse> projects,
            ProjectHealthStatus healthStatus
    ) {
        return projects.stream()
                .filter(project ->
                        project.getHealthStatus() == healthStatus
                )
                .count();
    }

    private long countByStatus(
            List<WorkItem> workItems,
            WorkItemStatus status
    ) {
        return workItems.stream()
                .filter(workItem -> workItem.getStatus() == status)
                .count();
    }

    private int calculateCompletionRate(
            DashboardProjectResponse response
    ) {
        if (response.getTotalWorkItems() == 0) {
            return 0;
        }

        return (int) Math.round(
                response.getCompletedWorkItems()
                        * 100.0
                        / response.getTotalWorkItems()
        );
    }
}
