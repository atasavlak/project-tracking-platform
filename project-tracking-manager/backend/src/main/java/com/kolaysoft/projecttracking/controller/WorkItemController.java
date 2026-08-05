package com.kolaysoft.projecttracking.controller;

import com.kolaysoft.projecttracking.dto.CreateWorkItemRequest;
import com.kolaysoft.projecttracking.dto.PatchWorkItemRequest;
import com.kolaysoft.projecttracking.dto.UpdateWorkItemRequest;
import com.kolaysoft.projecttracking.dto.WorkItemResponse;
import com.kolaysoft.projecttracking.entity.WorkItemStatus;
import com.kolaysoft.projecttracking.service.WorkItemService;
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
@RequestMapping("/api/work-items")
@RequiredArgsConstructor
@Tag(
        name = "İş Kalemleri",
        description = "Haftalık raporlara bağlı iş kalemlerini yönetmek için kullanılan işlemler"
)
public class WorkItemController {

    private final WorkItemService workItemService;

    @Operation(
            summary = "Yeni iş kalemi oluşturur"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @PostMapping
    public ResponseEntity<WorkItemResponse> createWorkItem(
            @Valid
            @RequestBody
            CreateWorkItemRequest request
    ) {
        WorkItemResponse response =
                workItemService.createWorkItem(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Aktif iş kalemlerini filtreleyerek listeler"
    )
    @GetMapping
    public ResponseEntity<List<WorkItemResponse>> getWorkItems(
            @RequestParam(required = false)
            Long projectId,

            @RequestParam(required = false)
            Long weeklyReportId,

            @RequestParam(required = false)
            WorkItemStatus status,

            @RequestParam(required = false)
            String responsiblePerson
    ) {
        return ResponseEntity.ok(
                workItemService.getWorkItems(
                        projectId,
                        weeklyReportId,
                        status,
                        responsiblePerson
                )
        );
    }

    @Operation(
            summary = "ID bilgisine göre iş kalemini getirir"
    )
    @GetMapping("/{id}")
    public ResponseEntity<WorkItemResponse> getWorkItemById(
            @PathVariable
            Long id
    ) {
        return ResponseEntity.ok(
                workItemService.getWorkItemById(id)
        );
    }

    @Operation(
            summary = "İş kalemini tamamen günceller"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'TEAM_LEAD', 'ADMIN')"
    )
    @PutMapping("/{id}")
    public ResponseEntity<WorkItemResponse> updateWorkItem(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateWorkItemRequest request
    ) {
        return ResponseEntity.ok(
                workItemService.updateWorkItem(
                        id,
                        request
                )
        );
    }

    @Operation(
            summary = "İş kaleminin belirtilen alanlarını günceller"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'TEAM_LEAD', 'ADMIN')"
    )
    @PatchMapping("/{id}")
    public ResponseEntity<WorkItemResponse> patchWorkItem(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            PatchWorkItemRequest request
    ) {
        return ResponseEntity.ok(
                workItemService.patchWorkItem(
                        id,
                        request
                )
        );
    }

    @Operation(
            summary = "İş kalemini pasife alır"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateWorkItem(
            @PathVariable
            Long id
    ) {
        workItemService.deactivateWorkItem(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}