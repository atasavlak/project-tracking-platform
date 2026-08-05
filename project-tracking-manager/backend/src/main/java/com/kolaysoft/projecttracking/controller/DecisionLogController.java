package com.kolaysoft.projecttracking.controller;

import com.kolaysoft.projecttracking.dto.CreateDecisionLogRequest;
import com.kolaysoft.projecttracking.dto.DecisionLogResponse;
import com.kolaysoft.projecttracking.dto.PatchDecisionLogRequest;
import com.kolaysoft.projecttracking.dto.UpdateDecisionLogRequest;
import com.kolaysoft.projecttracking.entity.DecisionStatus;
import com.kolaysoft.projecttracking.service.DecisionLogService;
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
@RequestMapping("/api/decision-logs")
@Tag(
        name = "Karar Kayıtları",
        description = "Projelerde alınan kararları yönetmek için kullanılan işlemler"
)
public class DecisionLogController {

    private final DecisionLogService decisionLogService;

    public DecisionLogController(
            DecisionLogService decisionLogService
    ) {
        this.decisionLogService = decisionLogService;
    }

    @Operation(
            summary = "Yeni karar kaydı oluşturur"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @PostMapping
    public ResponseEntity<DecisionLogResponse> createDecisionLog(
            @Valid
            @RequestBody
            CreateDecisionLogRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        decisionLogService.createDecisionLog(
                                request
                        )
                );
    }

    @Operation(
            summary = "Aktif karar kayıtlarını filtreleyerek listeler"
    )
    @GetMapping
    public ResponseEntity<List<DecisionLogResponse>> getDecisionLogs(
            @RequestParam(required = false)
            Long projectId,

            @RequestParam(required = false)
            Long weeklyReportId,

            @RequestParam(required = false)
            DecisionStatus status,

            @RequestParam(required = false)
            Long decisionOwnerId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate decisionDateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate decisionDateTo
    ) {
        return ResponseEntity.ok(
                decisionLogService.getDecisionLogs(
                        projectId,
                        weeklyReportId,
                        status,
                        decisionOwnerId,
                        decisionDateFrom,
                        decisionDateTo
                )
        );
    }

    @Operation(
            summary = "ID bilgisine göre karar kaydını getirir"
    )
    @GetMapping("/{id}")
    public ResponseEntity<DecisionLogResponse> getDecisionLogById(
            @PathVariable
            Long id
    ) {
        return ResponseEntity.ok(
                decisionLogService.getDecisionLogById(
                        id
                )
        );
    }

    @Operation(
            summary = "Karar kaydını tamamen günceller"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'TEAM_LEAD', 'ADMIN')"
    )
    @PutMapping("/{id}")
    public ResponseEntity<DecisionLogResponse> updateDecisionLog(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateDecisionLogRequest request
    ) {
        return ResponseEntity.ok(
                decisionLogService.updateDecisionLog(
                        id,
                        request
                )
        );
    }

    @Operation(
            summary = "Karar kaydının belirtilen alanlarını günceller"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'TEAM_LEAD', 'ADMIN')"
    )
    @PatchMapping("/{id}")
    public ResponseEntity<DecisionLogResponse> patchDecisionLog(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            PatchDecisionLogRequest request
    ) {
        return ResponseEntity.ok(
                decisionLogService.patchDecisionLog(
                        id,
                        request
                )
        );
    }

    @Operation(
            summary = "Karar kaydını pasife alır"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateDecisionLog(
            @PathVariable
            Long id
    ) {
        decisionLogService.deactivateDecisionLog(
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}
