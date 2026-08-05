package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.ActionItemResponse;
import com.kolaysoft.projecttracking.dto.CreateActionItemRequest;
import com.kolaysoft.projecttracking.dto.PatchActionItemRequest;
import com.kolaysoft.projecttracking.dto.UpdateActionItemRequest;
import com.kolaysoft.projecttracking.entity.ActionItem;
import com.kolaysoft.projecttracking.entity.ActionItemPriority;
import com.kolaysoft.projecttracking.entity.ActionItemStatus;
import com.kolaysoft.projecttracking.entity.Project;
import com.kolaysoft.projecttracking.entity.WeeklyReport;
import com.kolaysoft.projecttracking.exception.ActionItemNotFoundException;
import com.kolaysoft.projecttracking.exception.BusinessRuleException;
import com.kolaysoft.projecttracking.exception.ProjectNotFoundException;
import com.kolaysoft.projecttracking.exception.WeeklyReportNotFoundException;
import com.kolaysoft.projecttracking.repository.ActionItemRepository;
import com.kolaysoft.projecttracking.repository.AppUserRepository;
import com.kolaysoft.projecttracking.repository.ProjectRepository;
import com.kolaysoft.projecttracking.repository.WeeklyReportRepository;
import com.kolaysoft.projecttracking.security.AppUserPrincipal;
import com.kolaysoft.projecttracking.security.CurrentUserService;
import com.kolaysoft.projecttracking.user.AppUser;
import com.kolaysoft.projecttracking.user.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ActionItemService {

    private static final EnumSet<ActionItemStatus> OVERDUE_CANDIDATE_STATUSES =
            EnumSet.of(
                    ActionItemStatus.OPEN,
                    ActionItemStatus.IN_PROGRESS
            );

    private final ActionItemRepository actionItemRepository;
    private final ProjectRepository projectRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final AppUserRepository appUserRepository;
    private final CurrentUserService currentUserService;

    public ActionItemService(
            ActionItemRepository actionItemRepository,
            ProjectRepository projectRepository,
            WeeklyReportRepository weeklyReportRepository,
            AppUserRepository appUserRepository,
            CurrentUserService currentUserService
    ) {
        this.actionItemRepository = actionItemRepository;
        this.projectRepository = projectRepository;
        this.weeklyReportRepository = weeklyReportRepository;
        this.appUserRepository = appUserRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public ActionItemResponse createActionItem(
            CreateActionItemRequest request
    ) {
        Project project = getActiveProject(request.getProjectId());

        ensureCanCreateOrDeleteActionItem(project);

        WeeklyReport weeklyReport = resolveWeeklyReport(
                request.getWeeklyReportId(),
                project
        );

        String normalizedTitle = normalizeRequiredText(
                request.getTitle(),
                "Aksiyon başlığı"
        );

        validateDuplicateTitle(project.getId(), normalizedTitle, null);

        ActionItem actionItem = new ActionItem();

        actionItem.setProject(project);
        actionItem.setWeeklyReport(weeklyReport);
        actionItem.setResponsibleUser(
                resolveResponsibleUser(
                        request.getResponsibleUserId(),
                        project
                )
        );
        actionItem.setTitle(normalizedTitle);
        actionItem.setDescription(
                normalizeRequiredText(
                        request.getDescription(),
                        "Aksiyon açıklaması"
                )
        );
        actionItem.setPriority(
                request.getPriority() != null
                        ? request.getPriority()
                        : ActionItemPriority.MEDIUM
        );
        actionItem.setStatus(
                request.getStatus() != null
                        ? request.getStatus()
                        : ActionItemStatus.OPEN
        );
        actionItem.setTargetDate(request.getTargetDate());
        actionItem.setCompletionDate(request.getCompletionDate());
        actionItem.setNote(normalizeOptionalText(request.getNote()));

        applyLifecycleRules(actionItem, request.getCompletionDate() != null);

        return toResponse(actionItemRepository.save(actionItem));
    }

    @Transactional
    public List<ActionItemResponse> getActionItems(
            Long projectId,
            Long weeklyReportId,
            ActionItemStatus status,
            ActionItemPriority priority,
            Long responsibleUserId,
            LocalDate targetDateFrom,
            LocalDate targetDateTo
    ) {
        validateFilterDates(targetDateFrom, targetDateTo);
        validateUserIdFilter(responsibleUserId);
        refreshOverdueActionItems();

        Project filteredProject = null;

        if (projectId != null) {
            filteredProject = getActiveProject(projectId);
        }

        if (weeklyReportId != null) {
            WeeklyReport weeklyReport = getActiveWeeklyReport(weeklyReportId);

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

        AppUserPrincipal currentUser = currentUserService.getCurrentUser();
        Long currentProjectManagerId = null;

        if (currentUser.getRole() == UserRole.PROJECT_MANAGER) {
            currentProjectManagerId = currentUser.getId();
        }

        return actionItemRepository
                .searchVisibleActionItems(
                        projectId,
                        weeklyReportId,
                        status,
                        priority,
                        responsibleUserId,
                        targetDateFrom,
                        targetDateTo,
                        currentProjectManagerId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ActionItemResponse getActionItemById(Long id) {
        ActionItem actionItem = getActiveActionItem(id);

        refreshOverdueStatus(actionItem);
        ensureCanViewActionItem(
                actionItem,
                currentUserService.getCurrentUser()
        );

        return toResponse(actionItem);
    }

    @Transactional
    public ActionItemResponse updateActionItem(
            Long id,
            UpdateActionItemRequest request
    ) {
        ActionItem actionItem = getActiveActionItem(id);

        ensureCanUpdateActionItem(actionItem);

        String normalizedTitle = normalizeRequiredText(
                request.getTitle(),
                "Aksiyon başlığı"
        );

        validateDuplicateTitle(
                actionItem.getProject().getId(),
                normalizedTitle,
                id
        );

        actionItem.setResponsibleUser(
                resolveResponsibleUser(
                        request.getResponsibleUserId(),
                        actionItem.getProject()
                )
        );
        actionItem.setTitle(normalizedTitle);
        actionItem.setDescription(
                normalizeRequiredText(
                        request.getDescription(),
                        "Aksiyon açıklaması"
                )
        );
        actionItem.setPriority(request.getPriority());
        actionItem.setStatus(request.getStatus());
        actionItem.setTargetDate(request.getTargetDate());
        actionItem.setCompletionDate(request.getCompletionDate());
        actionItem.setNote(normalizeOptionalText(request.getNote()));

        applyLifecycleRules(actionItem, request.getCompletionDate() != null);

        return toResponse(actionItemRepository.save(actionItem));
    }

    @Transactional
    public ActionItemResponse patchActionItem(
            Long id,
            PatchActionItemRequest request
    ) {
        ActionItem actionItem = getActiveActionItem(id);

        ensureCanUpdateActionItem(actionItem);

        if (request.getResponsibleUserId() != null) {
            actionItem.setResponsibleUser(
                    resolveResponsibleUser(
                            request.getResponsibleUserId(),
                            actionItem.getProject()
                    )
            );
        }

        if (request.getTitle() != null) {
            String normalizedTitle = normalizeRequiredText(
                    request.getTitle(),
                    "Aksiyon başlığı"
            );

            validateDuplicateTitle(
                    actionItem.getProject().getId(),
                    normalizedTitle,
                    id
            );

            actionItem.setTitle(normalizedTitle);
        }

        if (request.getDescription() != null) {
            actionItem.setDescription(
                    normalizeRequiredText(
                            request.getDescription(),
                            "Aksiyon açıklaması"
                    )
            );
        }

        if (request.getPriority() != null) {
            actionItem.setPriority(request.getPriority());
        }

        if (request.getStatus() != null) {
            actionItem.setStatus(request.getStatus());

            if (request.getStatus() != ActionItemStatus.COMPLETED) {
                actionItem.setCompletionDate(null);
            }
        }

        if (request.getTargetDate() != null) {
            actionItem.setTargetDate(request.getTargetDate());
        }

        if (request.getCompletionDate() != null) {
            actionItem.setCompletionDate(request.getCompletionDate());
        }

        if (request.getNote() != null) {
            actionItem.setNote(normalizeOptionalText(request.getNote()));
        }

        applyLifecycleRules(actionItem, request.getCompletionDate() != null);

        return toResponse(actionItemRepository.save(actionItem));
    }

    @Transactional
    public void deactivateActionItem(Long id) {
        ActionItem actionItem = getActiveActionItem(id);

        ensureCanCreateOrDeleteActionItem(actionItem.getProject());

        actionItem.setActive(false);
        actionItemRepository.save(actionItem);
    }

    private void validateDuplicateTitle(
            Long projectId,
            String title,
            Long excludedId
    ) {
        boolean sameTitleExists = excludedId == null
                ? actionItemRepository
                .existsByProject_IdAndTitleIgnoreCaseAndActiveTrue(
                        projectId,
                        title
                )
                : actionItemRepository
                .existsByProject_IdAndTitleIgnoreCaseAndActiveTrueAndIdNot(
                        projectId,
                        title,
                        excludedId
                );

        if (sameTitleExists) {
            throw new BusinessRuleException(
                    "Bu projede aynı başlığa sahip aktif bir aksiyon kaydı bulunmaktadır."
            );
        }
    }

    private Project getActiveProject(Long projectId) {
        return projectRepository
                .findById(projectId)
                .filter(Project::isActive)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    private WeeklyReport getActiveWeeklyReport(Long weeklyReportId) {
        WeeklyReport weeklyReport = weeklyReportRepository
                .findById(weeklyReportId)
                .filter(WeeklyReport::isActive)
                .orElseThrow(() ->
                        new WeeklyReportNotFoundException(weeklyReportId)
                );

        if (!weeklyReport.getProject().isActive()) {
            throw new BusinessRuleException(
                    "Pasif projeye bağlı haftalık rapor üzerinde işlem yapılamaz."
            );
        }

        return weeklyReport;
    }

    private WeeklyReport resolveWeeklyReport(
            Long weeklyReportId,
            Project project
    ) {
        if (weeklyReportId == null) {
            return null;
        }

        WeeklyReport weeklyReport = getActiveWeeklyReport(weeklyReportId);

        if (!weeklyReport
                .getProject()
                .getId()
                .equals(project.getId())) {

            throw new BusinessRuleException(
                    "Haftalık rapor belirtilen projeye ait değildir."
            );
        }

        return weeklyReport;
    }

    private ActionItem getActiveActionItem(Long id) {
        ActionItem actionItem = actionItemRepository
                .findById(id)
                .filter(ActionItem::isActive)
                .orElseThrow(() -> new ActionItemNotFoundException(id));

        if (!actionItem.getProject().isActive()) {
            throw new BusinessRuleException(
                    "Pasif projeye bağlı aksiyon kaydı üzerinde işlem yapılamaz."
            );
        }

        if (actionItem.getWeeklyReport() != null
                && !actionItem.getWeeklyReport().isActive()) {

            throw new BusinessRuleException(
                    "Pasif haftalık rapora bağlı aksiyon kaydı üzerinde işlem yapılamaz."
            );
        }

        return actionItem;
    }

    private AppUser resolveResponsibleUser(
            Long requestedResponsibleUserId,
            Project project
    ) {
        Long resolvedUserId = requestedResponsibleUserId != null
                ? requestedResponsibleUserId
                : project.getProjectManager().getId();

        return appUserRepository
                .findByIdAndActiveTrueAndActivationCompletedTrue(
                        resolvedUserId
                )
                .orElseThrow(() ->
                        new BusinessRuleException(
                                "Sorumlu olarak atanacak aktif ve aktivasyonu tamamlanmış kullanıcı bulunamadı."
                        )
                );
    }

    private void applyLifecycleRules(
            ActionItem actionItem,
            boolean completionDateExplicitlyProvided
    ) {
        if (actionItem.getTargetDate() == null) {
            throw new BusinessRuleException(
                    "Hedef tarih boş bırakılamaz."
            );
        }

        ActionItemStatus status = actionItem.getStatus() != null
                ? actionItem.getStatus()
                : ActionItemStatus.OPEN;

        actionItem.setStatus(status);

        if (status == ActionItemStatus.COMPLETED) {
            LocalDate completionDate = actionItem.getCompletionDate();

            if (completionDate == null) {
                actionItem.setCompletionDate(LocalDate.now());
            } else if (completionDate.isAfter(LocalDate.now())) {
                throw new BusinessRuleException(
                        "Tamamlanma tarihi bugünden ileri olamaz."
                );
            }

            return;
        }

        if (completionDateExplicitlyProvided
                && actionItem.getCompletionDate() != null) {

            throw new BusinessRuleException(
                    "Tamamlanma tarihi yalnızca tamamlanmış aksiyonlarda girilebilir."
            );
        }

        actionItem.setCompletionDate(null);

        if (status == ActionItemStatus.CANCELLED) {
            return;
        }

        if (actionItem.getTargetDate().isBefore(LocalDate.now())) {
            actionItem.setStatus(ActionItemStatus.OVERDUE);
            return;
        }

        if (status == ActionItemStatus.OVERDUE) {
            actionItem.setStatus(ActionItemStatus.IN_PROGRESS);
        }
    }

    private void refreshOverdueActionItems() {
        List<ActionItem> overdueCandidates = actionItemRepository
                .findByActiveTrueAndStatusInAndTargetDateBefore(
                        OVERDUE_CANDIDATE_STATUSES,
                        LocalDate.now()
                );

        overdueCandidates.forEach(actionItem ->
                actionItem.setStatus(ActionItemStatus.OVERDUE)
        );

        if (!overdueCandidates.isEmpty()) {
            actionItemRepository.saveAll(overdueCandidates);
        }
    }

    private void refreshOverdueStatus(ActionItem actionItem) {
        if (OVERDUE_CANDIDATE_STATUSES.contains(actionItem.getStatus())
                && actionItem.getTargetDate().isBefore(LocalDate.now())) {

            actionItem.setStatus(ActionItemStatus.OVERDUE);
            actionItemRepository.save(actionItem);
        }
    }

    private void ensureCanViewActionItem(
            ActionItem actionItem,
            AppUserPrincipal currentUser
    ) {
        if (currentUser.getRole() != UserRole.PROJECT_MANAGER) {
            return;
        }

        if (!isProjectOwnerOrResponsible(actionItem, currentUser)) {
            throw new AccessDeniedException(
                    "Yalnızca yöneticisi olduğunuz projelerdeki veya sorumlusu olduğunuz aksiyonları görüntüleyebilirsiniz."
            );
        }
    }

    private void ensureCanCreateOrDeleteActionItem(Project project) {
        AppUserPrincipal currentUser = currentUserService.getCurrentUser();

        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        ensureProjectOwnership(project, currentUser);
    }

    private void ensureCanUpdateActionItem(ActionItem actionItem) {
        AppUserPrincipal currentUser = currentUserService.getCurrentUser();

        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        boolean isResponsible = actionItem
                .getResponsibleUser()
                .getId()
                .equals(currentUser.getId());

        if (currentUser.getRole() == UserRole.TEAM_LEAD
                && isResponsible) {
            return;
        }

        if (currentUser.getRole() == UserRole.PROJECT_MANAGER
                && isProjectOwnerOrResponsible(actionItem, currentUser)) {
            return;
        }

        throw new AccessDeniedException(
                "Bu aksiyon kaydını güncelleme yetkiniz bulunmamaktadır."
        );
    }

    private boolean isProjectOwnerOrResponsible(
            ActionItem actionItem,
            AppUserPrincipal currentUser
    ) {
        boolean isProjectOwner = actionItem
                .getProject()
                .getProjectManager()
                .getId()
                .equals(currentUser.getId());

        boolean isResponsible = actionItem
                .getResponsibleUser()
                .getId()
                .equals(currentUser.getId());

        return isProjectOwner || isResponsible;
    }

    private void ensureProjectOwnership(
            Project project,
            AppUserPrincipal currentUser
    ) {
        if (currentUser.getRole() != UserRole.PROJECT_MANAGER
                || !project
                .getProjectManager()
                .getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "Yalnızca sorumlu olduğunuz projedeki aksiyon kayıtlarını yönetebilirsiniz."
            );
        }
    }

    private void validateFilterDates(
            LocalDate targetDateFrom,
            LocalDate targetDateTo
    ) {
        if (targetDateFrom != null
                && targetDateTo != null
                && targetDateTo.isBefore(targetDateFrom)) {

            throw new BusinessRuleException(
                    "Hedef tarih filtre bitiş değeri başlangıç değerinden önce olamaz."
            );
        }
    }

    private void validateUserIdFilter(Long responsibleUserId) {
        if (responsibleUserId != null && responsibleUserId <= 0) {
            throw new BusinessRuleException(
                    "Sorumlu kullanıcı ID değeri pozitif olmalıdır."
            );
        }
    }

    private String normalizeRequiredText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(
                    fieldName + " boş bırakılamaz."
            );
        }

        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private ActionItemResponse toResponse(ActionItem actionItem) {
        ActionItemResponse response = new ActionItemResponse();

        response.setId(actionItem.getId());
        response.setProjectId(actionItem.getProject().getId());
        response.setProjectName(actionItem.getProject().getName());

        if (actionItem.getWeeklyReport() != null) {
            response.setWeeklyReportId(
                    actionItem.getWeeklyReport().getId()
            );
        }

        response.setResponsibleUserId(
                actionItem.getResponsibleUser().getId()
        );
        response.setResponsibleUsername(
                actionItem.getResponsibleUser().getUsername()
        );
        response.setResponsibleFullName(
                actionItem.getResponsibleUser().getFullName()
        );
        response.setTitle(actionItem.getTitle());
        response.setDescription(actionItem.getDescription());
        response.setPriority(actionItem.getPriority());
        response.setStatus(actionItem.getStatus());
        response.setTargetDate(actionItem.getTargetDate());
        response.setCompletionDate(actionItem.getCompletionDate());
        response.setNote(actionItem.getNote());
        response.setActive(actionItem.isActive());
        response.setCreatedAt(actionItem.getCreatedAt());
        response.setUpdatedAt(actionItem.getUpdatedAt());

        return response;
    }
}
