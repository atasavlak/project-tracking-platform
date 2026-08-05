package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.CreateWeeklyReportRequest;
import com.kolaysoft.projecttracking.dto.PatchWeeklyReportRequest;
import com.kolaysoft.projecttracking.dto.UpdateWeeklyReportRequest;
import com.kolaysoft.projecttracking.dto.WeeklyReportResponse;
import com.kolaysoft.projecttracking.entity.Project;
import com.kolaysoft.projecttracking.entity.WeeklyReport;
import com.kolaysoft.projecttracking.entity.WeeklyReportStatus;
import com.kolaysoft.projecttracking.exception.BusinessRuleException;
import com.kolaysoft.projecttracking.exception.ProjectNotFoundException;
import com.kolaysoft.projecttracking.exception.WeeklyReportNotFoundException;
import com.kolaysoft.projecttracking.repository.ProjectRepository;
import com.kolaysoft.projecttracking.repository.WeeklyReportRepository;
import com.kolaysoft.projecttracking.security.AppUserPrincipal;
import com.kolaysoft.projecttracking.security.CurrentUserService;
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
public class WeeklyReportService {

    private final WeeklyReportRepository weeklyReportRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public WeeklyReportResponse createWeeklyReport(
            CreateWeeklyReportRequest request
    ) {
        Project project =
                getActiveProject(
                        request.getProjectId()
                );

        ensureCanManageProject(
                project
        );

        validateReportDates(
                request.getWeekStartDate(),
                request.getWeekEndDate()
        );

        boolean reportExists =
                weeklyReportRepository
                        .existsByProject_IdAndWeekStartDateAndActiveTrue(
                                project.getId(),
                                request.getWeekStartDate()
                        );

        if (reportExists) {
            throw new BusinessRuleException(
                    "Bu proje ve hafta için aktif bir rapor zaten bulunmaktadır."
            );
        }

        WeeklyReport weeklyReport =
                new WeeklyReport();

        weeklyReport.setProject(
                project
        );

        weeklyReport.setWeekStartDate(
                request.getWeekStartDate()
        );

        weeklyReport.setWeekEndDate(
                request.getWeekEndDate()
        );

        weeklyReport.setStatus(
                request.getStatus()
        );

        weeklyReport.setSummary(
                normalizeRequiredText(
                        request.getSummary(),
                        "Rapor özeti"
                )
        );

        weeklyReport.setCompletedWork(
                normalizeOptionalText(
                        request.getCompletedWork()
                )
        );

        weeklyReport.setNextWeekPlan(
                normalizeOptionalText(
                        request.getNextWeekPlan()
                )
        );

        weeklyReport.setRisks(
                normalizeOptionalText(
                        request.getRisks()
                )
        );

        WeeklyReport savedReport =
                weeklyReportRepository.save(
                        weeklyReport
                );

        return toResponse(
                savedReport
        );
    }

    public List<WeeklyReportResponse> getWeeklyReports(
            Long projectId,
            WeeklyReportStatus status,
            LocalDate weekStartDate,
            LocalDate weekEndDate
    ) {
        validateFilterDates(
                weekStartDate,
                weekEndDate
        );

        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        if (projectId != null) {
            Project project =
                    getActiveProject(
                            projectId
                    );

            ensureCanViewProject(
                    project,
                    currentUser
            );
        }

        Long projectManagerId = null;

        if (currentUser.getRole()
                == UserRole.PROJECT_MANAGER) {

            projectManagerId =
                    currentUser.getId();
        }

        List<WeeklyReport> reports =
                weeklyReportRepository
                        .searchVisibleReports(
                                projectId,
                                status,
                                weekStartDate,
                                weekEndDate,
                                projectManagerId
                        );

        return reports.stream()
                .map(this::toResponse)
                .toList();
    }

    public WeeklyReportResponse getWeeklyReportById(
            Long id
    ) {
        WeeklyReport weeklyReport =
                getActiveWeeklyReport(
                        id
                );

        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        ensureCanViewProject(
                weeklyReport.getProject(),
                currentUser
        );

        return toResponse(
                weeklyReport
        );
    }

    @Transactional
    public WeeklyReportResponse updateWeeklyReport(
            Long id,
            UpdateWeeklyReportRequest request
    ) {
        WeeklyReport weeklyReport =
                getActiveWeeklyReport(
                        id
                );

        ensureCanManageProject(
                weeklyReport.getProject()
        );

        validateReportDates(
                request.getWeekStartDate(),
                request.getWeekEndDate()
        );

        boolean reportExists =
                weeklyReportRepository
                        .existsByProject_IdAndWeekStartDateAndActiveTrueAndIdNot(
                                weeklyReport
                                        .getProject()
                                        .getId(),
                                request.getWeekStartDate(),
                                id
                        );

        if (reportExists) {
            throw new BusinessRuleException(
                    "Bu proje ve hafta için aktif bir rapor zaten bulunmaktadır."
            );
        }

        weeklyReport.setWeekStartDate(
                request.getWeekStartDate()
        );

        weeklyReport.setWeekEndDate(
                request.getWeekEndDate()
        );

        weeklyReport.setStatus(
                request.getStatus()
        );

        weeklyReport.setSummary(
                normalizeRequiredText(
                        request.getSummary(),
                        "Rapor özeti"
                )
        );

        weeklyReport.setCompletedWork(
                normalizeOptionalText(
                        request.getCompletedWork()
                )
        );

        weeklyReport.setNextWeekPlan(
                normalizeOptionalText(
                        request.getNextWeekPlan()
                )
        );

        weeklyReport.setRisks(
                normalizeOptionalText(
                        request.getRisks()
                )
        );

        WeeklyReport updatedReport =
                weeklyReportRepository.save(
                        weeklyReport
                );

        return toResponse(
                updatedReport
        );
    }

    @Transactional
    public WeeklyReportResponse patchWeeklyReport(
            Long id,
            PatchWeeklyReportRequest request
    ) {
        WeeklyReport weeklyReport =
                getActiveWeeklyReport(
                        id
                );

        ensureCanManageProject(
                weeklyReport.getProject()
        );

        if (request.getWeekStartDate() != null) {
            boolean reportExists =
                    weeklyReportRepository
                            .existsByProject_IdAndWeekStartDateAndActiveTrueAndIdNot(
                                    weeklyReport
                                            .getProject()
                                            .getId(),
                                    request.getWeekStartDate(),
                                    id
                            );

            if (reportExists) {
                throw new BusinessRuleException(
                        "Bu proje ve hafta için aktif bir rapor zaten bulunmaktadır."
                );
            }

            weeklyReport.setWeekStartDate(
                    request.getWeekStartDate()
            );
        }

        if (request.getWeekEndDate() != null) {
            weeklyReport.setWeekEndDate(
                    request.getWeekEndDate()
            );
        }

        if (request.getStatus() != null) {
            weeklyReport.setStatus(
                    request.getStatus()
            );
        }

        if (request.getSummary() != null) {
            weeklyReport.setSummary(
                    normalizeRequiredText(
                            request.getSummary(),
                            "Rapor özeti"
                    )
            );
        }

        if (request.getCompletedWork() != null) {
            weeklyReport.setCompletedWork(
                    normalizeOptionalText(
                            request.getCompletedWork()
                    )
            );
        }

        if (request.getNextWeekPlan() != null) {
            weeklyReport.setNextWeekPlan(
                    normalizeOptionalText(
                            request.getNextWeekPlan()
                    )
            );
        }

        if (request.getRisks() != null) {
            weeklyReport.setRisks(
                    normalizeOptionalText(
                            request.getRisks()
                    )
            );
        }

        validateReportDates(
                weeklyReport.getWeekStartDate(),
                weeklyReport.getWeekEndDate()
        );

        WeeklyReport updatedReport =
                weeklyReportRepository.save(
                        weeklyReport
                );

        return toResponse(
                updatedReport
        );
    }

    @Transactional
    public void deactivateWeeklyReport(
            Long id
    ) {
        WeeklyReport weeklyReport =
                getActiveWeeklyReport(
                        id
                );

        ensureCanManageProject(
                weeklyReport.getProject()
        );

        weeklyReport.setActive(
                false
        );

        weeklyReportRepository.save(
                weeklyReport
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
            Long id
    ) {
        WeeklyReport weeklyReport =
                weeklyReportRepository
                        .findById(
                                id
                        )
                        .filter(
                                WeeklyReport::isActive
                        )
                        .orElseThrow(() ->
                                new WeeklyReportNotFoundException(
                                        id
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
                    "Yalnızca sorumlu olduğunuz projenin raporlarını görüntüleyebilirsiniz."
            );
        }
    }

    private void ensureCanManageProject(
            Project project
    ) {
        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole()
                == UserRole.ADMIN) {

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
                "Yalnızca sorumlu olduğunuz projenin raporlarını yönetebilirsiniz."
        );
    }

    private void validateReportDates(
            LocalDate weekStartDate,
            LocalDate weekEndDate
    ) {
        if (weekStartDate == null) {
            throw new BusinessRuleException(
                    "Hafta başlangıç tarihi zorunludur."
            );
        }

        if (weekEndDate == null) {
            throw new BusinessRuleException(
                    "Hafta bitiş tarihi zorunludur."
            );
        }

        if (weekEndDate.isBefore(
                weekStartDate
        )) {
            throw new BusinessRuleException(
                    "Hafta bitiş tarihi başlangıç tarihinden önce olamaz."
            );
        }
    }

    private void validateFilterDates(
            LocalDate weekStartDate,
            LocalDate weekEndDate
    ) {
        if (weekStartDate != null
                && weekEndDate != null
                && weekEndDate.isBefore(
                weekStartDate
        )) {

            throw new BusinessRuleException(
                    "Filtre bitiş tarihi başlangıç tarihinden önce olamaz."
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

    private WeeklyReportResponse toResponse(
            WeeklyReport weeklyReport
    ) {
        WeeklyReportResponse response =
                new WeeklyReportResponse();

        response.setId(
                weeklyReport.getId()
        );

        response.setProjectId(
                weeklyReport
                        .getProject()
                        .getId()
        );

        response.setProjectName(
                weeklyReport
                        .getProject()
                        .getName()
        );

        response.setWeekStartDate(
                weeklyReport.getWeekStartDate()
        );

        response.setWeekEndDate(
                weeklyReport.getWeekEndDate()
        );

        response.setStatus(
                weeklyReport.getStatus()
        );

        response.setSummary(
                weeklyReport.getSummary()
        );

        response.setCompletedWork(
                weeklyReport.getCompletedWork()
        );

        response.setNextWeekPlan(
                weeklyReport.getNextWeekPlan()
        );

        response.setRisks(
                weeklyReport.getRisks()
        );

        response.setActive(
                weeklyReport.isActive()
        );

        response.setCreatedAt(
                weeklyReport.getCreatedAt()
        );

        response.setUpdatedAt(
                weeklyReport.getUpdatedAt()
        );

        return response;
    }
}