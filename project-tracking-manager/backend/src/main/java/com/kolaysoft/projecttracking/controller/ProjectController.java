package com.kolaysoft.projecttracking.controller;

import com.kolaysoft.projecttracking.dto.CreateProjectRequest;
import com.kolaysoft.projecttracking.dto.PatchProjectRequest;
import com.kolaysoft.projecttracking.dto.ProjectResponse;
import com.kolaysoft.projecttracking.dto.UpdateProjectRequest;
import com.kolaysoft.projecttracking.entity.ProjectStatus;
import com.kolaysoft.projecttracking.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(
        name = "Projeler",
        description = "Proje oluşturma, görüntüleme, güncelleme ve pasife alma işlemleri"
)
public class ProjectController {

    private final ProjectService projectService;

    @Operation(
            summary = "Yeni proje oluşturur"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid
            @RequestBody
            CreateProjectRequest request
    ) {
        ProjectResponse response =
                projectService.createProject(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Aktif projeleri listeler"
    )
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects(
            @RequestParam(required = false)
            ProjectStatus status
    ) {
        return ResponseEntity.ok(
                projectService.getProjects(status)
        );
    }

    @Operation(
            summary = "ID bilgisine göre proje detayını getirir"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable
            Long id
    ) {
        return ResponseEntity.ok(
                projectService.getProjectById(id)
        );
    }

    @Operation(
            summary = "Projeyi tamamen günceller"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateProjectRequest request
    ) {
        return ResponseEntity.ok(
                projectService.updateProject(
                        id,
                        request
                )
        );
    }

    @Operation(
            summary = "Projenin belirtilen alanlarını günceller"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> patchProject(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            PatchProjectRequest request
    ) {
        return ResponseEntity.ok(
                projectService.patchProject(
                        id,
                        request
                )
        );
    }

    @Operation(
            summary = "Projeyi pasife alır"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateProject(
            @PathVariable
            Long id
    ) {
        projectService.deactivateProject(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}