package com.kolaysoft.projecttracking.controller;

import com.kolaysoft.projecttracking.dto.CreateWeeklyReportRequest;
import com.kolaysoft.projecttracking.dto.PatchWeeklyReportRequest;
import com.kolaysoft.projecttracking.dto.UpdateWeeklyReportRequest;
import com.kolaysoft.projecttracking.dto.WeeklyReportResponse;
import com.kolaysoft.projecttracking.entity.WeeklyReportStatus;
import com.kolaysoft.projecttracking.service.WeeklyReportService;
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
@RequestMapping("/api/weekly-reports")
@RequiredArgsConstructor
@Tag(
        name = "Haftalık Raporlar",
        description = "Projelerin haftalık raporlarını yönetmek için kullanılan işlemler"
)
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    @Operation(
            summary = "Yeni haftalık rapor oluşturur"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @PostMapping
    public ResponseEntity<WeeklyReportResponse> createWeeklyReport(
            @Valid
            @RequestBody
            CreateWeeklyReportRequest request
    ) {
        WeeklyReportResponse response =
                weeklyReportService.createWeeklyReport(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Aktif haftalık raporları filtreleyerek listeler"
    )
    @GetMapping
    public ResponseEntity<List<WeeklyReportResponse>> getWeeklyReports(
            @RequestParam(required = false)
            Long projectId,

            @RequestParam(required = false)
            WeeklyReportStatus status,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate weekStartDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate weekEndDate
    ) {
        return ResponseEntity.ok(
                weeklyReportService.getWeeklyReports(
                        projectId,
                        status,
                        weekStartDate,
                        weekEndDate
                )
        );
    }

    @Operation(
            summary = "ID bilgisine göre haftalık raporu getirir"
    )
    @GetMapping("/{id}")
    public ResponseEntity<WeeklyReportResponse> getWeeklyReportById(
            @PathVariable
            Long id
    ) {
        return ResponseEntity.ok(
                weeklyReportService.getWeeklyReportById(
                        id
                )
        );
    }

    @Operation(
            summary = "Haftalık raporu tamamen günceller"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @PutMapping("/{id}")
    public ResponseEntity<WeeklyReportResponse> updateWeeklyReport(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateWeeklyReportRequest request
    ) {
        return ResponseEntity.ok(
                weeklyReportService.updateWeeklyReport(
                        id,
                        request
                )
        );
    }

    @Operation(
            summary = "Haftalık raporun belirtilen alanlarını günceller"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @PatchMapping("/{id}")
    public ResponseEntity<WeeklyReportResponse> patchWeeklyReport(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            PatchWeeklyReportRequest request
    ) {
        return ResponseEntity.ok(
                weeklyReportService.patchWeeklyReport(
                        id,
                        request
                )
        );
    }

    @Operation(
            summary = "Haftalık raporu pasife alır"
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateWeeklyReport(
            @PathVariable
            Long id
    ) {
        weeklyReportService.deactivateWeeklyReport(
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}