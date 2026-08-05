package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.CreateProjectRequest;
import com.kolaysoft.projecttracking.dto.PatchProjectRequest;
import com.kolaysoft.projecttracking.dto.ProjectResponse;
import com.kolaysoft.projecttracking.dto.UpdateProjectRequest;
import com.kolaysoft.projecttracking.entity.Project;
import com.kolaysoft.projecttracking.entity.ProjectStatus;
import com.kolaysoft.projecttracking.exception.BusinessRuleException;
import com.kolaysoft.projecttracking.exception.ProjectNotFoundException;
import com.kolaysoft.projecttracking.repository.AppUserRepository;
import com.kolaysoft.projecttracking.repository.ProjectRepository;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public ProjectResponse createProject(
            CreateProjectRequest request
    ) {
        String normalizedName =
                normalizeRequiredName(
                        request.getName()
                );

        validateProjectDates(
                request.getStartDate(),
                request.getEndDate()
        );

        boolean sameNameExists =
                projectRepository
                        .existsByNameIgnoreCase(
                                normalizedName
                        );

        if (sameNameExists) {
            throw new BusinessRuleException(
                    "Aynı isimde başka bir proje bulunmaktadır."
            );
        }

        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        AppUser projectManager =
                resolveProjectManager(
                        currentUser.getId()
                );

        Project project = new Project();

        project.setName(normalizedName);

        project.setDescription(
                normalizeOptionalText(
                        request.getDescription()
                )
        );

        project.setProjectManager(
                projectManager
        );

        project.setStartDate(
                request.getStartDate()
        );

        project.setEndDate(
                request.getEndDate()
        );

        project.setStatus(
                request.getStatus()
        );

        Project savedProject =
                projectRepository.save(project);

        return toResponse(savedProject);
    }

    public List<ProjectResponse> getProjects(
            ProjectStatus status
    ) {
        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        List<Project> projects;

        if (currentUser.getRole()
                == UserRole.PROJECT_MANAGER) {

            if (status == null) {
                projects =
                        projectRepository
                                .findByProjectManager_IdAndActiveTrue(
                                        currentUser.getId()
                                );
            } else {
                projects =
                        projectRepository
                                .findByProjectManager_IdAndStatusAndActiveTrue(
                                        currentUser.getId(),
                                        status
                                );
            }

        } else {

            if (status == null) {
                projects =
                        projectRepository
                                .findByActiveTrue();
            } else {
                projects =
                        projectRepository
                                .findByStatusAndActiveTrue(
                                        status
                                );
            }
        }

        return projects.stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getProjectById(
            Long id
    ) {
        Project project =
                getActiveProject(id);

        ensureCanViewProject(project);

        return toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(
            Long id,
            UpdateProjectRequest request
    ) {
        Project project =
                getActiveProject(id);

        ensureCanManageProject(project);

        String normalizedName =
                normalizeRequiredName(
                        request.getName()
                );

        validateProjectDates(
                request.getStartDate(),
                request.getEndDate()
        );

        boolean sameNameExists =
                projectRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                normalizedName,
                                id
                        );

        if (sameNameExists) {
            throw new BusinessRuleException(
                    "Aynı isimde başka bir proje bulunmaktadır."
            );
        }

        project.setName(normalizedName);

        project.setDescription(
                normalizeOptionalText(
                        request.getDescription()
                )
        );

        project.setStartDate(
                request.getStartDate()
        );

        project.setEndDate(
                request.getEndDate()
        );

        project.setStatus(
                request.getStatus()
        );

        Project updatedProject =
                projectRepository.save(project);

        return toResponse(updatedProject);
    }

    @Transactional
    public ProjectResponse patchProject(
            Long id,
            PatchProjectRequest request
    ) {
        Project project =
                getActiveProject(id);

        ensureCanManageProject(project);

        if (request.getName() != null) {
            String normalizedName =
                    normalizeRequiredName(
                            request.getName()
                    );

            boolean sameNameExists =
                    projectRepository
                            .existsByNameIgnoreCaseAndIdNot(
                                    normalizedName,
                                    id
                            );

            if (sameNameExists) {
                throw new BusinessRuleException(
                        "Aynı isimde başka bir proje bulunmaktadır."
                );
            }

            project.setName(normalizedName);
        }

        if (request.getDescription() != null) {
            project.setDescription(
                    normalizeOptionalText(
                            request.getDescription()
                    )
            );
        }

        if (request.getStartDate() != null) {
            project.setStartDate(
                    request.getStartDate()
            );
        }

        if (request.getEndDate() != null) {
            project.setEndDate(
                    request.getEndDate()
            );
        }

        if (request.getStatus() != null) {
            project.setStatus(
                    request.getStatus()
            );
        }

        validateProjectDates(
                project.getStartDate(),
                project.getEndDate()
        );

        Project updatedProject =
                projectRepository.save(project);

        return toResponse(updatedProject);
    }

    @Transactional
    public void deactivateProject(
            Long id
    ) {
        Project project =
                getActiveProject(id);

        ensureCanManageProject(project);

        project.setActive(false);

        projectRepository.save(project);
    }

    private Project getActiveProject(
            Long id
    ) {
        return projectRepository
                .findById(id)
                .filter(Project::isActive)
                .orElseThrow(() ->
                        new ProjectNotFoundException(id)
                );
    }

    private void ensureCanViewProject(
            Project project
    ) {
        AppUserPrincipal currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole()
                != UserRole.PROJECT_MANAGER) {
            return;
        }

        if (!project
                .getProjectManager()
                .getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "Bu projeyi görüntüleme yetkiniz bulunmamaktadır."
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

        if (!project
                .getProjectManager()
                .getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "Yalnızca sorumlu olduğunuz projeyi değiştirebilirsiniz."
            );
        }
    }

    private void validateProjectDates(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null) {
            throw new BusinessRuleException(
                    "Proje başlangıç tarihi zorunludur."
            );
        }

        if (endDate != null
                && endDate.isBefore(startDate)) {

            throw new BusinessRuleException(
                    "Proje bitiş tarihi başlangıç tarihinden önce olamaz."
            );
        }
    }

    private String normalizeRequiredName(
            String name
    ) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException(
                    "Proje adı boş bırakılamaz."
            );
        }

        return name.trim();
    }

    private String normalizeOptionalText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private AppUser resolveProjectManager(
            Long projectManagerId
    ) {
        return appUserRepository
                .findByIdAndActiveTrueAndActivationCompletedTrue(
                        projectManagerId
                )
                .orElseThrow(() ->
                        new BusinessRuleException(
                                "Proje yöneticisi olarak atanacak aktif kullanıcı bulunamadı."
                        )
                );
    }

    private ProjectResponse toResponse(
            Project project
    ) {
        ProjectResponse response =
                new ProjectResponse();

        response.setId(
                project.getId()
        );

        response.setName(
                project.getName()
        );

        response.setDescription(
                project.getDescription()
        );

        response.setProjectManagerId(
                project
                        .getProjectManager()
                        .getId()
        );

        response.setProjectManagerFullName(
                project
                        .getProjectManager()
                        .getFullName()
        );

        response.setStartDate(
                project.getStartDate()
        );

        response.setEndDate(
                project.getEndDate()
        );

        response.setStatus(
                project.getStatus()
        );

        response.setActive(
                project.isActive()
        );

        response.setCreatedAt(
                project.getCreatedAt()
        );

        response.setUpdatedAt(
                project.getUpdatedAt()
        );

        return response;
    }
}