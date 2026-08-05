package com.kolaysoft.projecttracking.controller;

import com.kolaysoft.projecttracking.dto.ActionItemResponse;
import com.kolaysoft.projecttracking.dto.CreateActionItemRequest;
import com.kolaysoft.projecttracking.dto.PatchActionItemRequest;
import com.kolaysoft.projecttracking.dto.UpdateActionItemRequest;
import com.kolaysoft.projecttracking.entity.ActionItemPriority;
import com.kolaysoft.projecttracking.entity.ActionItemStatus;
import com.kolaysoft.projecttracking.service.ActionItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/action-items")
@Tag(
        name = "Aksiyon Kayıtları",
        description = "Projelerde takip edilen aksiyonları yönetmek için kullanılan işlemler"
)
public class ActionItemController {

    private final ActionItemService actionItemService;

    public ActionItemController(ActionItemService actionItemService) {
        this.actionItemService = actionItemService;
    }

    @Operation(summary = "Yeni aksiyon kaydı oluşturur")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ActionItemResponse> createActionItem(
            @Valid @RequestBody CreateActionItemRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(actionItemService.createActionItem(request));
    }

    @Operation(summary = "Aktif aksiyon kayıtlarını filtreleyerek listeler")
    @GetMapping
    public ResponseEntity<List<ActionItemResponse>> getActionItems(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long weeklyReportId,
            @RequestParam(required = false) ActionItemStatus status,
            @RequestParam(required = false) ActionItemPriority priority,
            @RequestParam(required = false) Long responsibleUserId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate targetDateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate targetDateTo
    ) {
        return ResponseEntity.ok(
                actionItemService.getActionItems(
                        projectId,
                        weeklyReportId,
                        status,
                        priority,
                        responsibleUserId,
                        targetDateFrom,
                        targetDateTo
                )
        );
    }

    @Operation(summary = "ID bilgisine göre aksiyon kaydını getirir")
    @GetMapping("/{id}")
    public ResponseEntity<ActionItemResponse> getActionItemById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                actionItemService.getActionItemById(id)
        );
    }

    @Operation(summary = "Aksiyon kaydını tamamen günceller")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'TEAM_LEAD', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ActionItemResponse> updateActionItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateActionItemRequest request
    ) {
        return ResponseEntity.ok(
                actionItemService.updateActionItem(id, request)
        );
    }

    @Operation(summary = "Aksiyon kaydının belirtilen alanlarını günceller")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'TEAM_LEAD', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<ActionItemResponse> patchActionItem(
            @PathVariable Long id,
            @Valid @RequestBody PatchActionItemRequest request
    ) {
        return ResponseEntity.ok(
                actionItemService.patchActionItem(id, request)
        );
    }

    @Operation(summary = "Aksiyon kaydını pasife alır")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateActionItem(
            @PathVariable Long id
    ) {
        actionItemService.deactivateActionItem(id);

        return ResponseEntity.noContent().build();
    }
}
