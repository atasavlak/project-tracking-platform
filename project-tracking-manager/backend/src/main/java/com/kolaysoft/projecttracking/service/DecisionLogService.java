package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.CreateDecisionLogRequest;
import com.kolaysoft.projecttracking.dto.DecisionLogResponse;
import com.kolaysoft.projecttracking.dto.PatchDecisionLogRequest;
import com.kolaysoft.projecttracking.dto.UpdateDecisionLogRequest;
import com.kolaysoft.projecttracking.entity.DecisionLog;
import com.kolaysoft.projecttracking.entity.DecisionStatus;
import com.kolaysoft.projecttracking.entity.Project;
import com.kolaysoft.projecttracking.entity.WeeklyReport;
import com.kolaysoft.projecttracking.exception.BusinessRuleException;
import com.kolaysoft.projecttracking.exception.DecisionLogNotFoundException;
import com.kolaysoft.projecttracking.exception.ProjectNotFoundException;
import com.kolaysoft.projecttracking.exception.WeeklyReportNotFoundException;
import com.kolaysoft.projecttracking.repository.AppUserRepository;
import com.kolaysoft.projecttracking.repository.DecisionLogRepository;
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
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DecisionLogService {

    private final DecisionLogRepository decisionLogRepository;
    private final ProjectRepository projectRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final AppUserRepository appUserRepository;
    private final CurrentUserService currentUserService;

    public DecisionLogService(
            DecisionLogRepository decisionLogRepository,
            ProjectRepository projectRepository,
            WeeklyReportRepository weeklyReportRepository,
            AppUserRepository appUserRepository,
            CurrentUserService currentUserService
    ) {
        this.decisionLogRepository = decisionLogRepository;
        this.projectRepository = projectRepository;
        this.weeklyReportRepository = weeklyReportRepository;
        this.appUserRepository = appUserRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public DecisionLogResponse createDecisionLog(
            CreateDecisionLogRequest request
    ) {
        Project project =
                getActiveProject(
                        request.getProjectId()
                );

        ensureCanCreateOrDeleteDecisionLog(
                project
        );

        WeeklyReport weeklyReport =
                resolveWeeklyReport(
                        request.getWeeklyReportId(),
                        project
                );

        String normalizedTitle =
                normalizeRequiredText(
                        request.getTitle(),
                        "Karar başlığı"
                );

        boolean sameTitleExists =
                decisionLogRepository
                        .existsByProject_IdAndTitleIgnoreCaseAndActiveTrue(
                                project.getId(),
                                normalizedTitle
                        );

        if (sameTitleExists) {
            throw new BusinessRuleException(
                    "Bu projede aynı başlığa sahip aktif bir karar kaydı bulunmaktadır."
            );
        }

        DecisionLog decisionLog =
                new DecisionLog();

        decisionLog.setProject(
                project
        );

        decisionLog.setWeeklyReport(
                weeklyReport
        );

        decisionLog.setDecisionOwner(
                resolveDecisionOwner(
                        request.getDecisionOwnerId(),
                        project
                )
        );

        decisionLog.setTitle(
                normalizedTitle
        );

        decisionLog.setDescription(
                normalizeRequiredText(
                        request.getDescription(),
                        "Karar açıklaması"
                )
        );

        decisionLog.setDecisionDate(
                request.getDecisionDate()
        );

        decisionLog.setStatus(
                request.getStatus()
        );

        decisionLog.setNote(
                normalizeOptionalText(
                        request.getNote()
                )
        );

        DecisionLog savedDecisionLog =
                decisionLogRepository.save(
                        decisionLog
                );

        return toResponse(
                savedDecisionLog
        );
    }

    public List<DecisionLogResponse> getDecisionLogs(
            Long projectId,
            Long weeklyReportId,
            DecisionStatus status,
            Long decisionOwnerId,
            LocalDate decisionDateFrom,
            LocalDate decisionDateTo
    ) {
        validateFilterDates(
                decisionDateFrom,
                decisionDateTo
        );

        validateUserIdFilter(
                decisionOwnerId
        );

        Project filteredProject = null;

        if (projectId != null) {
            filteredProject =
                    getActiveProject(
                            projectId
                    );
        }

        if (weeklyReportId != null) {
            WeeklyReport weeklyReport =
                    getActiveWeeklyReport(
                            weeklyReportId
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

        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        Long currentProjectManagerId = null;

        if (currentUser.getRole()
                == UserRole.PROJECT_MANAGER) {

            currentProjectManagerId =
                    currentUser.getId();
        }

        return decisionLogRepository
                .searchVisibleDecisionLogs(
                        projectId,
                        weeklyReportId,
                        status,
                        decisionOwnerId,
                        decisionDateFrom,
                        decisionDateTo,
                        currentProjectManagerId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DecisionLogResponse getDecisionLogById(
            Long id
    ) {
        DecisionLog decisionLog =
                getActiveDecisionLog(
                        id
                );

        ensureCanViewDecisionLog(
                decisionLog,
                currentUserService.getCurrentUser()
        );

        return toResponse(
                decisionLog
        );
    }

    @Transactional
    public DecisionLogResponse updateDecisionLog(
            Long id,
            UpdateDecisionLogRequest request
    ) {
        DecisionLog decisionLog =
                getActiveDecisionLog(
                        id
                );

        ensureCanUpdateDecisionLog(
                decisionLog
        );

        String normalizedTitle =
                normalizeRequiredText(
                        request.getTitle(),
                        "Karar başlığı"
                );

        boolean sameTitleExists =
                decisionLogRepository
                        .existsByProject_IdAndTitleIgnoreCaseAndActiveTrueAndIdNot(
                                decisionLog
                                        .getProject()
                                        .getId(),
                                normalizedTitle,
                                id
                        );

        if (sameTitleExists) {
            throw new BusinessRuleException(
                    "Bu projede aynı başlığa sahip aktif bir karar kaydı bulunmaktadır."
            );
        }

        decisionLog.setDecisionOwner(
                resolveDecisionOwner(
                        request.getDecisionOwnerId(),
                        decisionLog.getProject()
                )
        );

        decisionLog.setTitle(
                normalizedTitle
        );

        decisionLog.setDescription(
                normalizeRequiredText(
                        request.getDescription(),
                        "Karar açıklaması"
                )
        );

        decisionLog.setDecisionDate(
                request.getDecisionDate()
        );

        decisionLog.setStatus(
                request.getStatus()
        );

        decisionLog.setNote(
                normalizeOptionalText(
                        request.getNote()
                )
        );

        return toResponse(
                decisionLogRepository.save(
                        decisionLog
                )
        );
    }

    @Transactional
    public DecisionLogResponse patchDecisionLog(
            Long id,
            PatchDecisionLogRequest request
    ) {
        DecisionLog decisionLog =
                getActiveDecisionLog(
                        id
                );

        ensureCanUpdateDecisionLog(
                decisionLog
        );

        if (request.getDecisionOwnerId() != null) {
            decisionLog.setDecisionOwner(
                    resolveDecisionOwner(
                            request.getDecisionOwnerId(),
                            decisionLog.getProject()
                    )
            );
        }

        if (request.getTitle() != null) {
            String normalizedTitle =
                    normalizeRequiredText(
                            request.getTitle(),
                            "Karar başlığı"
                    );

            boolean sameTitleExists =
                    decisionLogRepository
                            .existsByProject_IdAndTitleIgnoreCaseAndActiveTrueAndIdNot(
                                    decisionLog
                                            .getProject()
                                            .getId(),
                                    normalizedTitle,
                                    id
                            );

            if (sameTitleExists) {
                throw new BusinessRuleException(
                        "Bu projede aynı başlığa sahip aktif bir karar kaydı bulunmaktadır."
                );
            }

            decisionLog.setTitle(
                    normalizedTitle
            );
        }

        if (request.getDescription() != null) {
            decisionLog.setDescription(
                    normalizeRequiredText(
                            request.getDescription(),
                            "Karar açıklaması"
                    )
            );
        }

        if (request.getDecisionDate() != null) {
            decisionLog.setDecisionDate(
                    request.getDecisionDate()
            );
        }

        if (request.getStatus() != null) {
            decisionLog.setStatus(
                    request.getStatus()
            );
        }

        if (request.getNote() != null) {
            decisionLog.setNote(
                    normalizeOptionalText(
                            request.getNote()
                    )
            );
        }

        return toResponse(
                decisionLogRepository.save(
                        decisionLog
                )
        );
    }

    @Transactional
    public void deactivateDecisionLog(
            Long id
    ) {
        DecisionLog decisionLog =
                getActiveDecisionLog(
                        id
                );

        ensureCanCreateOrDeleteDecisionLog(
                decisionLog.getProject()
        );

        decisionLog.setActive(
                false
        );

        decisionLogRepository.save(
                decisionLog
        );
    }

    private Project getActiveProject(
            Long projectId
    ) {
        return projectRepository
                .findById(
                        projectId
                )
                .filter(
                        Project::isActive
                )
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
                        .findById(
                                weeklyReportId
                        )
                        .filter(
                                WeeklyReport::isActive
                        )
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

    private WeeklyReport resolveWeeklyReport(
            Long weeklyReportId,
            Project project
    ) {
        if (weeklyReportId == null) {
            return null;
        }

        WeeklyReport weeklyReport =
                getActiveWeeklyReport(
                        weeklyReportId
                );

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

    private DecisionLog getActiveDecisionLog(
            Long id
    ) {
        DecisionLog decisionLog =
                decisionLogRepository
                        .findById(
                                id
                        )
                        .filter(
                                DecisionLog::isActive
                        )
                        .orElseThrow(() ->
                                new DecisionLogNotFoundException(
                                        id
                                )
                        );

        if (!decisionLog
                .getProject()
                .isActive()) {

            throw new BusinessRuleException(
                    "Pasif projeye bağlı karar kaydı üzerinde işlem yapılamaz."
            );
        }

        if (decisionLog.getWeeklyReport() != null
                && !decisionLog
                .getWeeklyReport()
                .isActive()) {

            throw new BusinessRuleException(
                    "Pasif haftalık rapora bağlı karar kaydı üzerinde işlem yapılamaz."
            );
        }

        return decisionLog;
    }

    private AppUser resolveDecisionOwner(
            Long requestedDecisionOwnerId,
            Project project
    ) {
        Long resolvedUserId =
                requestedDecisionOwnerId != null
                        ? requestedDecisionOwnerId
                        : project
                        .getProjectManager()
                        .getId();

        return appUserRepository
                .findByIdAndActiveTrueAndActivationCompletedTrue(
                        resolvedUserId
                )
                .orElseThrow(() ->
                        new BusinessRuleException(
                                "Karar sahibi olarak atanacak aktif ve aktivasyonu tamamlanmış kullanıcı bulunamadı."
                        )
                );
    }

    private void ensureCanViewDecisionLog(
            DecisionLog decisionLog,
            AppUserPrincipal currentUser
    ) {
        if (currentUser.getRole()
                != UserRole.PROJECT_MANAGER) {

            return;
        }

        if (!isProjectOwnerOrDecisionOwner(
                decisionLog,
                currentUser
        )) {

            throw new AccessDeniedException(
                    "Yalnızca yöneticisi olduğunuz projelerdeki veya sahibi olduğunuz karar kayıtlarını görüntüleyebilirsiniz."
            );
        }
    }

    private void ensureCanCreateOrDeleteDecisionLog(
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

    private void ensureCanUpdateDecisionLog(
            DecisionLog decisionLog
    ) {
        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole()
                == UserRole.ADMIN) {

            return;
        }

        boolean isDecisionOwner =
                decisionLog
                        .getDecisionOwner()
                        .getId()
                        .equals(currentUser.getId());

        if (currentUser.getRole()
                == UserRole.TEAM_LEAD
                && isDecisionOwner) {

            return;
        }

        if (currentUser.getRole()
                == UserRole.PROJECT_MANAGER
                && isProjectOwnerOrDecisionOwner(
                decisionLog,
                currentUser
        )) {

            return;
        }

        throw new AccessDeniedException(
                "Bu karar kaydını güncelleme yetkiniz bulunmamaktadır."
        );
    }

    private boolean isProjectOwnerOrDecisionOwner(
            DecisionLog decisionLog,
            AppUserPrincipal currentUser
    ) {
        boolean isProjectOwner =
                decisionLog
                        .getProject()
                        .getProjectManager()
                        .getId()
                        .equals(currentUser.getId());

        boolean isDecisionOwner =
                decisionLog
                        .getDecisionOwner()
                        .getId()
                        .equals(currentUser.getId());

        return isProjectOwner || isDecisionOwner;
    }

    private void ensureProjectOwnership(
            Project project,
            AppUserPrincipal currentUser
    ) {
        if (currentUser.getRole()
                != UserRole.PROJECT_MANAGER
                || !project
                .getProjectManager()
                .getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "Yalnızca sorumlu olduğunuz projedeki karar kayıtlarını yönetebilirsiniz."
            );
        }
    }

    private void validateFilterDates(
            LocalDate decisionDateFrom,
            LocalDate decisionDateTo
    ) {
        if (decisionDateFrom != null
                && decisionDateTo != null
                && decisionDateTo.isBefore(
                decisionDateFrom
        )) {

            throw new BusinessRuleException(
                    "Karar tarihi filtre bitiş değeri başlangıç değerinden önce olamaz."
            );
        }
    }

    private void validateUserIdFilter(
            Long decisionOwnerId
    ) {
        if (decisionOwnerId != null
                && decisionOwnerId <= 0) {

            throw new BusinessRuleException(
                    "Karar sahibi kullanıcı ID değeri pozitif olmalıdır."
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

    private DecisionLogResponse toResponse(
            DecisionLog decisionLog
    ) {
        DecisionLogResponse response =
                new DecisionLogResponse();

        response.setId(
                decisionLog.getId()
        );

        response.setProjectId(
                decisionLog
                        .getProject()
                        .getId()
        );

        response.setProjectName(
                decisionLog
                        .getProject()
                        .getName()
        );

        if (decisionLog.getWeeklyReport() != null) {
            response.setWeeklyReportId(
                    decisionLog
                            .getWeeklyReport()
                            .getId()
            );
        }

        response.setDecisionOwnerId(
                decisionLog
                        .getDecisionOwner()
                        .getId()
        );

        response.setDecisionOwnerUsername(
                decisionLog
                        .getDecisionOwner()
                        .getUsername()
        );

        response.setDecisionOwnerFullName(
                decisionLog
                        .getDecisionOwner()
                        .getFullName()
        );

        response.setTitle(
                decisionLog.getTitle()
        );

        response.setDescription(
                decisionLog.getDescription()
        );

        response.setDecisionDate(
                decisionLog.getDecisionDate()
        );

        response.setStatus(
                decisionLog.getStatus()
        );

        response.setNote(
                decisionLog.getNote()
        );

        response.setActive(
                decisionLog.isActive()
        );

        response.setCreatedAt(
                decisionLog.getCreatedAt()
        );

        response.setUpdatedAt(
                decisionLog.getUpdatedAt()
        );

        return response;
    }
}
