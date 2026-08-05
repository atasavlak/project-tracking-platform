package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.CreateWorkItemRequest;
import com.kolaysoft.projecttracking.dto.PatchWorkItemRequest;
import com.kolaysoft.projecttracking.dto.UpdateWorkItemRequest;
import com.kolaysoft.projecttracking.dto.WorkItemResponse;
import com.kolaysoft.projecttracking.entity.Project;
import com.kolaysoft.projecttracking.entity.WeeklyReport;
import com.kolaysoft.projecttracking.entity.WorkItem;
import com.kolaysoft.projecttracking.entity.WorkItemStatus;
import com.kolaysoft.projecttracking.exception.BusinessRuleException;
import com.kolaysoft.projecttracking.exception.ProjectNotFoundException;
import com.kolaysoft.projecttracking.exception.WeeklyReportNotFoundException;
import com.kolaysoft.projecttracking.exception.WorkItemNotFoundException;
import com.kolaysoft.projecttracking.repository.ProjectRepository;
import com.kolaysoft.projecttracking.repository.WeeklyReportRepository;
import com.kolaysoft.projecttracking.repository.WorkItemRepository;
import com.kolaysoft.projecttracking.security.AppUserPrincipal;
import com.kolaysoft.projecttracking.security.CurrentUserService;
import com.kolaysoft.projecttracking.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkItemService {

    private final WorkItemRepository workItemRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public WorkItemResponse createWorkItem(
            CreateWorkItemRequest request
    ) {
        WeeklyReport weeklyReport =
                getActiveWeeklyReport(
                        request.getWeeklyReportId()
                );

        ensureCanCreateOrDeleteWorkItem(
                weeklyReport.getProject()
        );

        String normalizedTitle =
                normalizeRequiredText(
                        request.getTitle(),
                        "İş kalemi başlığı"
                );

        boolean sameTitleExists =
                workItemRepository
                        .existsByWeeklyReport_IdAndTitleIgnoreCaseAndActiveTrue(
                                weeklyReport.getId(),
                                normalizedTitle
                        );

        if (sameTitleExists) {
            throw new BusinessRuleException(
                    "Bu haftalık raporda aynı başlığa sahip aktif bir iş kalemi bulunmaktadır."
            );
        }

        WorkItem workItem =
                new WorkItem();

        workItem.setWeeklyReport(
                weeklyReport
        );

        workItem.setTitle(
                normalizedTitle
        );

        workItem.setStatus(
                request.getStatus()
        );

        workItem.setResponsiblePerson(
                normalizeRequiredText(
                        request.getResponsiblePerson(),
                        "Sorumlu kişi"
                )
        );

        workItem.setPlannedWork(
                normalizeRequiredText(
                        request.getPlannedWork(),
                        "Planlanan çalışma"
                )
        );

        workItem.setCompletedWork(
                normalizeOptionalText(
                        request.getCompletedWork()
                )
        );

        workItem.setDescription(
                normalizeOptionalText(
                        request.getDescription()
                )
        );

        WorkItem savedWorkItem =
                workItemRepository.save(
                        workItem
                );

        return toResponse(savedWorkItem);
    }

    public List<WorkItemResponse> getWorkItems(
            Long projectId,
            Long weeklyReportId,
            WorkItemStatus status,
            String responsiblePerson
    ) {
        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        Project filteredProject = null;

        if (projectId != null) {
            filteredProject =
                    getActiveProject(projectId);

            ensureCanViewProject(
                    filteredProject,
                    currentUser
            );
        }

        if (weeklyReportId != null) {
            WeeklyReport weeklyReport =
                    getActiveWeeklyReport(
                            weeklyReportId
                    );

            ensureCanViewProject(
                    weeklyReport.getProject(),
                    currentUser
            );

            if (filteredProject != null
                    && !weeklyReport
                    .getProject()
                    .getId()
                    .equals(filteredProject.getId())) {

                throw new BusinessRuleException(
                        "Haftalık rapor belirtilen projeye ait değildir."
                );
            }
        }

        String normalizedResponsiblePerson =
                normalizeOptionalText(
                        responsiblePerson
                );

        Long projectManagerId = null;

        if (currentUser.getRole()
                == UserRole.PROJECT_MANAGER) {

            projectManagerId =
                    currentUser.getId();
        }

        List<WorkItem> workItems =
                workItemRepository
                        .searchVisibleWorkItems(
                                projectId,
                                weeklyReportId,
                                status,
                                normalizedResponsiblePerson,
                                projectManagerId
                        );

        return workItems.stream()
                .map(this::toResponse)
                .toList();
    }

    public WorkItemResponse getWorkItemById(
            Long id
    ) {
        WorkItem workItem =
                getActiveWorkItem(id);

        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        ensureCanViewProject(
                workItem
                        .getWeeklyReport()
                        .getProject(),
                currentUser
        );

        return toResponse(workItem);
    }

    @Transactional
    public WorkItemResponse updateWorkItem(
            Long id,
            UpdateWorkItemRequest request
    ) {
        WorkItem workItem =
                getActiveWorkItem(id);

        ensureCanUpdateWorkItem(
                workItem
                        .getWeeklyReport()
                        .getProject()
        );

        String normalizedTitle =
                normalizeRequiredText(
                        request.getTitle(),
                        "İş kalemi başlığı"
                );

        boolean sameTitleExists =
                workItemRepository
                        .existsByWeeklyReport_IdAndTitleIgnoreCaseAndActiveTrueAndIdNot(
                                workItem
                                        .getWeeklyReport()
                                        .getId(),
                                normalizedTitle,
                                id
                        );

        if (sameTitleExists) {
            throw new BusinessRuleException(
                    "Bu haftalık raporda aynı başlığa sahip aktif bir iş kalemi bulunmaktadır."
            );
        }

        workItem.setTitle(
                normalizedTitle
        );

        workItem.setStatus(
                request.getStatus()
        );

        workItem.setResponsiblePerson(
                normalizeRequiredText(
                        request.getResponsiblePerson(),
                        "Sorumlu kişi"
                )
        );

        workItem.setPlannedWork(
                normalizeRequiredText(
                        request.getPlannedWork(),
                        "Planlanan çalışma"
                )
        );

        workItem.setCompletedWork(
                normalizeOptionalText(
                        request.getCompletedWork()
                )
        );

        workItem.setDescription(
                normalizeOptionalText(
                        request.getDescription()
                )
        );

        WorkItem updatedWorkItem =
                workItemRepository.save(
                        workItem
                );

        return toResponse(updatedWorkItem);
    }

    @Transactional
    public WorkItemResponse patchWorkItem(
            Long id,
            PatchWorkItemRequest request
    ) {
        WorkItem workItem =
                getActiveWorkItem(id);

        ensureCanUpdateWorkItem(
                workItem
                        .getWeeklyReport()
                        .getProject()
        );

        if (request.getTitle() != null) {
            String normalizedTitle =
                    normalizeRequiredText(
                            request.getTitle(),
                            "İş kalemi başlığı"
                    );

            boolean sameTitleExists =
                    workItemRepository
                            .existsByWeeklyReport_IdAndTitleIgnoreCaseAndActiveTrueAndIdNot(
                                    workItem
                                            .getWeeklyReport()
                                            .getId(),
                                    normalizedTitle,
                                    id
                            );

            if (sameTitleExists) {
                throw new BusinessRuleException(
                        "Bu haftalık raporda aynı başlığa sahip aktif bir iş kalemi bulunmaktadır."
                );
            }

            workItem.setTitle(
                    normalizedTitle
            );
        }

        if (request.getStatus() != null) {
            workItem.setStatus(
                    request.getStatus()
            );
        }

        if (request.getResponsiblePerson() != null) {
            workItem.setResponsiblePerson(
                    normalizeRequiredText(
                            request.getResponsiblePerson(),
                            "Sorumlu kişi"
                    )
            );
        }

        if (request.getPlannedWork() != null) {
            workItem.setPlannedWork(
                    normalizeRequiredText(
                            request.getPlannedWork(),
                            "Planlanan çalışma"
                    )
            );
        }

        if (request.getCompletedWork() != null) {
            workItem.setCompletedWork(
                    normalizeOptionalText(
                            request.getCompletedWork()
                    )
            );
        }

        if (request.getDescription() != null) {
            workItem.setDescription(
                    normalizeOptionalText(
                            request.getDescription()
                    )
            );
        }

        WorkItem updatedWorkItem =
                workItemRepository.save(
                        workItem
                );

        return toResponse(updatedWorkItem);
    }

    @Transactional
    public void deactivateWorkItem(
            Long id
    ) {
        WorkItem workItem =
                getActiveWorkItem(id);

        ensureCanCreateOrDeleteWorkItem(
                workItem
                        .getWeeklyReport()
                        .getProject()
        );

        workItem.setActive(false);

        workItemRepository.save(
                workItem
        );
    }

    private Project getActiveProject(
            Long projectId
    ) {
        return projectRepository
                .findById(projectId)
                .filter(Project::isActive)
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                projectId
                        )
                );
    }

    private WeeklyReport getActiveWeeklyReport(
            Long weeklyReportId
    ) {
        WeeklyReport weeklyReport =
                weeklyReportRepository
                        .findById(weeklyReportId)
                        .filter(WeeklyReport::isActive)
                        .orElseThrow(() ->
                                new WeeklyReportNotFoundException(
                                        weeklyReportId
                                )
                        );

        if (!weeklyReport
                .getProject()
                .isActive()) {

            throw new BusinessRuleException(
                    "Pasif projeye bağlı haftalık rapor üzerinde işlem yapılamaz."
            );
        }

        return weeklyReport;
    }

    private WorkItem getActiveWorkItem(
            Long id
    ) {
        WorkItem workItem =
                workItemRepository
                        .findById(id)
                        .filter(WorkItem::isActive)
                        .orElseThrow(() ->
                                new WorkItemNotFoundException(id)
                        );

        WeeklyReport weeklyReport =
                workItem.getWeeklyReport();

        if (!weeklyReport.isActive()) {
            throw new BusinessRuleException(
                    "Pasif haftalık rapora bağlı iş kalemi üzerinde işlem yapılamaz."
            );
        }

        if (!weeklyReport
                .getProject()
                .isActive()) {

            throw new BusinessRuleException(
                    "Pasif projeye bağlı iş kalemi üzerinde işlem yapılamaz."
            );
        }

        return workItem;
    }

    private void ensureCanViewProject(
            Project project,
            AppUserPrincipal currentUser
    ) {
        if (currentUser.getRole()
                != UserRole.PROJECT_MANAGER) {

            return;
        }

        if (!project
                .getProjectManager()
                .getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "Yalnızca sorumlu olduğunuz projedeki iş kalemlerini görüntüleyebilirsiniz."
            );
        }
    }

    private void ensureCanCreateOrDeleteWorkItem(
            Project project
    ) {
        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole()
                == UserRole.ADMIN) {

            return;
        }

        ensureProjectOwnership(
                project,
                currentUser
        );
    }

    private void ensureCanUpdateWorkItem(
            Project project
    ) {
        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole()
                == UserRole.ADMIN) {

            return;
        }

        if (currentUser.getRole()
                == UserRole.TEAM_LEAD) {

            return;
        }

        ensureProjectOwnership(
                project,
                currentUser
        );
    }

    private void ensureProjectOwnership(
            Project project,
            AppUserPrincipal currentUser
    ) {
        if (!project
                .getProjectManager()
                .getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "Yalnızca sorumlu olduğunuz projedeki iş kalemlerini yönetebilirsiniz."
            );
        }
    }

    private String normalizeRequiredText(
            String value,
            String fieldName
    ) {
        if (value == null
                || value.isBlank()) {

            throw new BusinessRuleException(
                    fieldName
                            + " boş bırakılamaz."
            );
        }

        return value.trim();
    }

    private String normalizeOptionalText(
            String value
    ) {
        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }

    private WorkItemResponse toResponse(
            WorkItem workItem
    ) {
        WorkItemResponse response =
                new WorkItemResponse();

        response.setId(
                workItem.getId()
        );

        response.setWeeklyReportId(
                workItem
                        .getWeeklyReport()
                        .getId()
        );

        response.setProjectId(
                workItem
                        .getWeeklyReport()
                        .getProject()
                        .getId()
        );

        response.setProjectName(
                workItem
                        .getWeeklyReport()
                        .getProject()
                        .getName()
        );

        response.setTitle(
                workItem.getTitle()
        );

        response.setStatus(
                workItem.getStatus()
        );

        response.setResponsiblePerson(
                workItem.getResponsiblePerson()
        );

        response.setPlannedWork(
                workItem.getPlannedWork()
        );

        response.setCompletedWork(
                workItem.getCompletedWork()
        );

        response.setDescription(
                workItem.getDescription()
        );

        response.setActive(
                workItem.isActive()
        );

        response.setCreatedAt(
                workItem.getCreatedAt()
        );

        response.setUpdatedAt(
                workItem.getUpdatedAt()
        );

        return response;
    }
}