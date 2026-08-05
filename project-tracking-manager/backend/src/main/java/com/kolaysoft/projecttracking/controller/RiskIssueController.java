package com.kolaysoft.projecttracking.controller;

import com.kolaysoft.projecttracking.dto.CreateRiskIssueRequest;
import com.kolaysoft.projecttracking.dto.PatchRiskIssueRequest;
import com.kolaysoft.projecttracking.dto.RiskIssueResponse;
import com.kolaysoft.projecttracking.dto.UpdateRiskIssueRequest;
import com.kolaysoft.projecttracking.entity.RiskIssueSeverity;
import com.kolaysoft.projecttracking.entity.RiskIssueStatus;
import com.kolaysoft.projecttracking.entity.RiskIssueType;
import com.kolaysoft.projecttracking.service.RiskIssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/risk-issues")
@RequiredArgsConstructor
@Tag(
        name = "Risk ve Engel Kayıtları",
        description = "Haftalık raporlara bağlı risk ve engel kayıtlarını yönetmek için kullanılan işlemler"
)
public class RiskIssueController {

    private final RiskIssueService riskIssueService;

    @Operation(
            summary = "Yeni risk veya engel kaydı oluşturur"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @PostMapping
    public ResponseEntity<RiskIssueResponse> createRiskIssue(
            @Valid
            @RequestBody
            CreateRiskIssueRequest request
    ) {
        RiskIssueResponse response =
                riskIssueService.createRiskIssue(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Aktif risk ve engel kayıtlarını filtreleyerek listeler"
    )
    @GetMapping
    public ResponseEntity<List<RiskIssueResponse>> getRiskIssues(
            @RequestParam(required = false)
            Long projectId,

            @RequestParam(required = false)
            Long weeklyReportId,

            @RequestParam(required = false)
            RiskIssueType type,

            @RequestParam(required = false)
            RiskIssueSeverity severity,

            @RequestParam(required = false)
            RiskIssueStatus status,

            @RequestParam(required = false)
            Long responsibleUserId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate followUpDateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate followUpDateTo
    ) {
        return ResponseEntity.ok(
                riskIssueService.getRiskIssues(
                        projectId,
                        weeklyReportId,
                        type,
                        severity,
                        status,
                        responsibleUserId,
                        followUpDateFrom,
                        followUpDateTo
                )
        );
    }

    @Operation(
            summary = "ID bilgisine göre risk veya engel kaydını getirir"
    )
    @GetMapping("/{id}")
    public ResponseEntity<RiskIssueResponse> getRiskIssueById(
            @PathVariable
            Long id
    ) {
        return ResponseEntity.ok(
                riskIssueService.getRiskIssueById(
                        id
                )
        );
    }

    @Operation(
            summary = "Risk veya engel kaydını tamamen günceller"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'TEAM_LEAD', 'ADMIN')"
    )
    @PutMapping("/{id}")
    public ResponseEntity<RiskIssueResponse> updateRiskIssue(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateRiskIssueRequest request
    ) {
        return ResponseEntity.ok(
                riskIssueService.updateRiskIssue(
                        id,
                        request
                )
        );
    }

    @Operation(
            summary = "Risk veya engel kaydının belirtilen alanlarını günceller"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'TEAM_LEAD', 'ADMIN')"
    )
    @PatchMapping("/{id}")
    public ResponseEntity<RiskIssueResponse> patchRiskIssue(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            PatchRiskIssueRequest request
    ) {
        return ResponseEntity.ok(
                riskIssueService.patchRiskIssue(
                        id,
                        request
                )
        );
    }

    @Operation(
            summary = "Risk veya engel kaydını pasife alır"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateRiskIssue(
            @PathVariable
            Long id
    ) {
        riskIssueService.deactivateRiskIssue(
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}
