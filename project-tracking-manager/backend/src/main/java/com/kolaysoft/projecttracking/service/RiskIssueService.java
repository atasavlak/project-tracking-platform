package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.CreateRiskIssueRequest;
import com.kolaysoft.projecttracking.dto.PatchRiskIssueRequest;
import com.kolaysoft.projecttracking.dto.RiskIssueResponse;
import com.kolaysoft.projecttracking.dto.UpdateRiskIssueRequest;
import com.kolaysoft.projecttracking.entity.Project;
import com.kolaysoft.projecttracking.entity.RiskIssue;
import com.kolaysoft.projecttracking.entity.RiskIssueSeverity;
import com.kolaysoft.projecttracking.entity.RiskIssueStatus;
import com.kolaysoft.projecttracking.entity.RiskIssueType;
import com.kolaysoft.projecttracking.entity.WeeklyReport;
import com.kolaysoft.projecttracking.exception.BusinessRuleException;
import com.kolaysoft.projecttracking.exception.ProjectNotFoundException;
import com.kolaysoft.projecttracking.exception.RiskIssueNotFoundException;
import com.kolaysoft.projecttracking.exception.WeeklyReportNotFoundException;
import com.kolaysoft.projecttracking.repository.AppUserRepository;
import com.kolaysoft.projecttracking.repository.ProjectRepository;
import com.kolaysoft.projecttracking.repository.RiskIssueRepository;
import com.kolaysoft.projecttracking.repository.WeeklyReportRepository;
import com.kolaysoft.projecttracking.security.AppUserPrincipal;
import com.kolaysoft.projecttracking.security.CurrentUserService;
import com.kolaysoft.projecttracking.user.AppUser;
import com.kolaysoft.projecttracking.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiskIssueService {

    private final RiskIssueRepository riskIssueRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public RiskIssueResponse createRiskIssue(
            CreateRiskIssueRequest request
    ) {
        WeeklyReport weeklyReport =
                getActiveWeeklyReport(
                        request.getWeeklyReportId()
                );

        ensureCanCreateOrDeleteRiskIssue(
                weeklyReport.getProject()
        );

        String normalizedTitle =
                normalizeRequiredText(
                        request.getTitle(),
                        "Risk veya engel başlığı"
                );

        boolean sameTitleExists =
                riskIssueRepository
                        .existsByWeeklyReport_IdAndTitleIgnoreCaseAndActiveTrue(
                                weeklyReport.getId(),
                                normalizedTitle
                        );

        if (sameTitleExists) {
            throw new BusinessRuleException(
                    "Bu haftalık raporda aynı başlığa sahip aktif bir risk veya engel kaydı bulunmaktadır."
            );
        }

        AppUser responsibleUser =
                resolveResponsibleUser(
                        request.getResponsibleUserId(),
                        weeklyReport.getProject()
                );

        RiskIssue riskIssue =
                new RiskIssue();

        riskIssue.setWeeklyReport(
                weeklyReport
        );

        riskIssue.setResponsibleUser(
                responsibleUser
        );

        riskIssue.setType(
                request.getType()
        );

        riskIssue.setTitle(
                normalizedTitle
        );

        riskIssue.setDescription(
                normalizeRequiredText(
                        request.getDescription(),
                        "Risk veya engel açıklaması"
                )
        );

        riskIssue.setSeverity(
                request.getSeverity()
        );

        riskIssue.setStatus(
                request.getStatus()
        );

        riskIssue.setFollowUpDate(
                request.getFollowUpDate()
        );

        riskIssue.setResolutionNote(
                normalizeOptionalText(
                        request.getResolutionNote()
                )
        );

        RiskIssue savedRiskIssue =
                riskIssueRepository.save(
                        riskIssue
                );

        return toResponse(
                savedRiskIssue
        );
    }

    public List<RiskIssueResponse> getRiskIssues(
            Long projectId,
            Long weeklyReportId,
            RiskIssueType type,
            RiskIssueSeverity severity,
            RiskIssueStatus status,
            Long responsibleUserId,
            LocalDate followUpDateFrom,
            LocalDate followUpDateTo
    ) {
        validateFilterDates(
                followUpDateFrom,
                followUpDateTo
        );

        validateResponsibleUserFilter(
                responsibleUserId
        );

        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

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

        Long currentProjectManagerId = null;

        if (currentUser.getRole()
                == UserRole.PROJECT_MANAGER) {

            currentProjectManagerId =
                    currentUser.getId();
        }

        List<RiskIssue> riskIssues =
                riskIssueRepository
                        .searchVisibleRiskIssues(
                                projectId,
                                weeklyReportId,
                                type,
                                severity,
                                status,
                                responsibleUserId,
                                followUpDateFrom,
                                followUpDateTo,
                                currentProjectManagerId
                        );

        return riskIssues.stream()
                .map(this::toResponse)
                .toList();
    }

    public RiskIssueResponse getRiskIssueById(
            Long id
    ) {
        RiskIssue riskIssue =
                getActiveRiskIssue(
                        id
                );

        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        ensureCanViewRiskIssue(
                riskIssue,
                currentUser
        );

        return toResponse(
                riskIssue
        );
    }

    @Transactional
    public RiskIssueResponse updateRiskIssue(
            Long id,
            UpdateRiskIssueRequest request
    ) {
        RiskIssue riskIssue =
                getActiveRiskIssue(
                        id
                );

        Project project =
                riskIssue
                        .getWeeklyReport()
                        .getProject();

        ensureCanUpdateRiskIssue(
                riskIssue
        );

        String normalizedTitle =
                normalizeRequiredText(
                        request.getTitle(),
                        "Risk veya engel başlığı"
                );

        boolean sameTitleExists =
                riskIssueRepository
                        .existsByWeeklyReport_IdAndTitleIgnoreCaseAndActiveTrueAndIdNot(
                                riskIssue
                                        .getWeeklyReport()
                                        .getId(),
                                normalizedTitle,
                                id
                        );

        if (sameTitleExists) {
            throw new BusinessRuleException(
                    "Bu haftalık raporda aynı başlığa sahip aktif bir risk veya engel kaydı bulunmaktadır."
            );
        }

        riskIssue.setResponsibleUser(
                resolveResponsibleUser(
                        request.getResponsibleUserId(),
                        project
                )
        );

        riskIssue.setType(
                request.getType()
        );

        riskIssue.setTitle(
                normalizedTitle
        );

        riskIssue.setDescription(
                normalizeRequiredText(
                        request.getDescription(),
                        "Risk veya engel açıklaması"
                )
        );

        riskIssue.setSeverity(
                request.getSeverity()
        );

        riskIssue.setStatus(
                request.getStatus()
        );

        riskIssue.setFollowUpDate(
                request.getFollowUpDate()
        );

        riskIssue.setResolutionNote(
                normalizeOptionalText(
                        request.getResolutionNote()
                )
        );

        RiskIssue updatedRiskIssue =
                riskIssueRepository.save(
                        riskIssue
                );

        return toResponse(
                updatedRiskIssue
        );
    }

    @Transactional
    public RiskIssueResponse patchRiskIssue(
            Long id,
            PatchRiskIssueRequest request
    ) {
        RiskIssue riskIssue =
                getActiveRiskIssue(
                        id
                );

        Project project =
                riskIssue
                        .getWeeklyReport()
                        .getProject();

        ensureCanUpdateRiskIssue(
                riskIssue
        );

        if (request.getResponsibleUserId() != null) {
            riskIssue.setResponsibleUser(
                    resolveResponsibleUser(
                            request.getResponsibleUserId(),
                            project
                    )
            );
        }

        if (request.getType() != null) {
            riskIssue.setType(
                    request.getType()
            );
        }

        if (request.getTitle() != null) {
            String normalizedTitle =
                    normalizeRequiredText(
                            request.getTitle(),
                            "Risk veya engel başlığı"
                    );

            boolean sameTitleExists =
                    riskIssueRepository
                            .existsByWeeklyReport_IdAndTitleIgnoreCaseAndActiveTrueAndIdNot(
                                    riskIssue
                                            .getWeeklyReport()
                                            .getId(),
                                    normalizedTitle,
                                    id
                            );

            if (sameTitleExists) {
                throw new BusinessRuleException(
                        "Bu haftalık raporda aynı başlığa sahip aktif bir risk veya engel kaydı bulunmaktadır."
                );
            }

            riskIssue.setTitle(
                    normalizedTitle
            );
        }

        if (request.getDescription() != null) {
            riskIssue.setDescription(
                    normalizeRequiredText(
                            request.getDescription(),
                            "Risk veya engel açıklaması"
                    )
            );
        }

        if (request.getSeverity() != null) {
            riskIssue.setSeverity(
                    request.getSeverity()
            );
        }

        if (request.getStatus() != null) {
            riskIssue.setStatus(
                    request.getStatus()
            );
        }

        if (request.getFollowUpDate() != null) {
            riskIssue.setFollowUpDate(
                    request.getFollowUpDate()
            );
        }

        if (request.getResolutionNote() != null) {
            riskIssue.setResolutionNote(
                    normalizeOptionalText(
                            request.getResolutionNote()
                    )
            );
        }

        RiskIssue updatedRiskIssue =
                riskIssueRepository.save(
                        riskIssue
                );

        return toResponse(
                updatedRiskIssue
        );
    }

    @Transactional
    public void deactivateRiskIssue(
            Long id
    ) {
        RiskIssue riskIssue =
                getActiveRiskIssue(
                        id
                );

        ensureCanCreateOrDeleteRiskIssue(
                riskIssue
                        .getWeeklyReport()
                        .getProject()
        );

        riskIssue.setActive(
                false
        );

        riskIssueRepository.save(
                riskIssue
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

    private RiskIssue getActiveRiskIssue(
            Long id
    ) {
        RiskIssue riskIssue =
                riskIssueRepository
                        .findById(
                                id
                        )
                        .filter(
                                RiskIssue::isActive
                        )
                        .orElseThrow(() ->
                                new RiskIssueNotFoundException(
                                        id
                                )
                        );

        WeeklyReport weeklyReport =
                riskIssue.getWeeklyReport();

        if (!weeklyReport.isActive()) {
            throw new BusinessRuleException(
                    "Pasif haftalık rapora bağlı risk veya engel kaydı üzerinde işlem yapılamaz."
            );
        }

        if (!weeklyReport
                .getProject()
                .isActive()) {

            throw new BusinessRuleException(
                    "Pasif projeye bağlı risk veya engel kaydı üzerinde işlem yapılamaz."
            );
        }

        return riskIssue;
    }

    private AppUser resolveResponsibleUser(
            Long requestedResponsibleUserId,
            Project project
    ) {
        Long resolvedUserId =
                requestedResponsibleUserId != null
                        ? requestedResponsibleUserId
                        : project.getProjectManager().getId();

        if (resolvedUserId == null) {
            throw new BusinessRuleException(
                    "Projeye bağlı bir yönetici bulunamadığı için sorumlu kullanıcı belirlenemedi."
            );
        }

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

    private void validateResponsibleUserFilter(
            Long responsibleUserId
    ) {
        if (responsibleUserId != null
                && responsibleUserId <= 0) {

            throw new BusinessRuleException(
                    "Sorumlu kullanıcı ID değeri pozitif olmalıdır."
            );
        }
    }

    private void ensureCanViewRiskIssue(
            RiskIssue riskIssue,
            AppUserPrincipal currentUser
    ) {
        if (currentUser.getRole()
                != UserRole.PROJECT_MANAGER) {

            return;
        }

        if (!isProjectOwnerOrResponsibleUser(
                riskIssue,
                currentUser
        )) {

            throw new AccessDeniedException(
                    "Yalnızca yöneticisi olduğunuz projelerdeki veya sorumlusu olduğunuz risk ve engel kayıtlarını görüntüleyebilirsiniz."
            );
        }
    }

    private void ensureCanCreateOrDeleteRiskIssue(
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

    private void ensureCanUpdateRiskIssue(
            RiskIssue riskIssue
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

        if (currentUser.getRole()
                == UserRole.PROJECT_MANAGER
                && isProjectOwnerOrResponsibleUser(
                riskIssue,
                currentUser
        )) {

            return;
        }

        throw new AccessDeniedException(
                "Yalnızca yöneticisi olduğunuz projelerdeki veya sorumlusu olduğunuz risk ve engel kayıtlarını güncelleyebilirsiniz."
        );
    }

    private boolean isProjectOwnerOrResponsibleUser(
            RiskIssue riskIssue,
            AppUserPrincipal currentUser
    ) {
        Project project =
                riskIssue
                        .getWeeklyReport()
                        .getProject();

        boolean isProjectOwner =
                project.getProjectManager() != null
                        && project
                        .getProjectManager()
                        .getId()
                        .equals(currentUser.getId());

        boolean isResponsibleUser =
                riskIssue.getResponsibleUser() != null
                        && riskIssue
                        .getResponsibleUser()
                        .getId()
                        .equals(currentUser.getId());

        return isProjectOwner || isResponsibleUser;
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
                    "Yalnızca sorumlu olduğunuz projedeki risk ve engel kayıtlarını yönetebilirsiniz."
            );
        }
    }

    private void validateFilterDates(
            LocalDate followUpDateFrom,
            LocalDate followUpDateTo
    ) {
        if (followUpDateFrom != null
                && followUpDateTo != null
                && followUpDateTo.isBefore(
                followUpDateFrom
        )) {

            throw new BusinessRuleException(
                    "Takip tarihi filtre bitiş değeri başlangıç değerinden önce olamaz."
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

    private RiskIssueResponse toResponse(
            RiskIssue riskIssue
    ) {
        RiskIssueResponse response =
                new RiskIssueResponse();

        response.setId(
                riskIssue.getId()
        );

        response.setWeeklyReportId(
                riskIssue
                        .getWeeklyReport()
                        .getId()
        );

        response.setProjectId(
                riskIssue
                        .getWeeklyReport()
                        .getProject()
                        .getId()
        );

        response.setProjectName(
                riskIssue
                        .getWeeklyReport()
                        .getProject()
                        .getName()
        );

        response.setResponsibleUserId(
                riskIssue
                        .getResponsibleUser()
                        .getId()
        );

        response.setResponsibleUsername(
                riskIssue
                        .getResponsibleUser()
                        .getUsername()
        );

        response.setResponsibleFullName(
                riskIssue
                        .getResponsibleUser()
                        .getFullName()
        );

        response.setType(
                riskIssue.getType()
        );

        response.setTitle(
                riskIssue.getTitle()
        );

        response.setDescription(
                riskIssue.getDescription()
        );

        response.setSeverity(
                riskIssue.getSeverity()
        );

        response.setStatus(
                riskIssue.getStatus()
        );

        response.setFollowUpDate(
                riskIssue.getFollowUpDate()
        );

        response.setResolutionNote(
                riskIssue.getResolutionNote()
        );

        response.setActive(
                riskIssue.isActive()
        );

        response.setCreatedAt(
                riskIssue.getCreatedAt()
        );

        response.setUpdatedAt(
                riskIssue.getUpdatedAt()
        );

        return response;
    }
}