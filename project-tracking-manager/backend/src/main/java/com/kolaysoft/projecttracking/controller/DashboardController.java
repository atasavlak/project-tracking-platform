package com.kolaysoft.projecttracking.controller;

import com.kolaysoft.projecttracking.dto.DashboardCriticalRiskResponse;
import com.kolaysoft.projecttracking.dto.DashboardOverdueActionResponse;
import com.kolaysoft.projecttracking.dto.DashboardProjectResponse;
import com.kolaysoft.projecttracking.dto.DashboardRiskyWorkItemResponse;
import com.kolaysoft.projecttracking.dto.DashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.ProjectHealthStatus;
import com.kolaysoft.projecttracking.entity.ProjectStatus;
import com.kolaysoft.projecttracking.service.DashboardExportService;
import com.kolaysoft.projecttracking.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CTO', 'ADMIN')")
@Tag(
        name = "Dashboard",
        description = "CTO proje sağlığı, kritik kayıtlar ve rapor çıktıları"
)
public class DashboardController {

    private static final DateTimeFormatter FILE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private final DashboardService dashboardService;
    private final DashboardExportService dashboardExportService;

    @Operation(summary = "Dashboard özet bilgilerini getirir")
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestParam(required = false)
            ProjectStatus status,

            @RequestParam(required = false)
            ProjectHealthStatus healthStatus
    ) {
        return ResponseEntity.ok(
                dashboardService.getSummary(status, healthStatus)
        );
    }

    @Operation(summary = "Proje sağlık görünümünü listeler")
    @GetMapping("/projects")
    public ResponseEntity<List<DashboardProjectResponse>> getProjects(
            @RequestParam(required = false)
            ProjectStatus status,

            @RequestParam(required = false)
            ProjectHealthStatus healthStatus
    ) {
        return ResponseEntity.ok(
                dashboardService.getProjects(status, healthStatus)
        );
    }

    @Operation(summary = "Riskli iş kalemlerini listeler")
    @GetMapping("/risky-work-items")
    public ResponseEntity<List<DashboardRiskyWorkItemResponse>>
    getRiskyWorkItems(
            @RequestParam(required = false)
            ProjectStatus status,

            @RequestParam(required = false)
            ProjectHealthStatus healthStatus
    ) {
        return ResponseEntity.ok(
                dashboardService.getRiskyWorkItems(
                        status,
                        healthStatus
                )
        );
    }

    @Operation(summary = "Kritik risk ve engelleri listeler")
    @GetMapping("/critical-risks")
    public ResponseEntity<List<DashboardCriticalRiskResponse>>
    getCriticalRisks(
            @RequestParam(required = false)
            ProjectStatus status,

            @RequestParam(required = false)
            ProjectHealthStatus healthStatus
    ) {
        return ResponseEntity.ok(
                dashboardService.getCriticalRisks(
                        status,
                        healthStatus
                )
        );
    }

    @Operation(summary = "Gecikmiş aksiyonları listeler")
    @GetMapping("/overdue-actions")
    public ResponseEntity<List<DashboardOverdueActionResponse>>
    getOverdueActions(
            @RequestParam(required = false)
            ProjectStatus status,

            @RequestParam(required = false)
            ProjectHealthStatus healthStatus
    ) {
        return ResponseEntity.ok(
                dashboardService.getOverdueActions(
                        status,
                        healthStatus
                )
        );
    }

    @Operation(summary = "Dashboard raporunu PDF olarak indirir")
    @GetMapping(
            value = "/export/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false)
            ProjectStatus status,

            @RequestParam(required = false)
            ProjectHealthStatus healthStatus
    ) {
        byte[] content = dashboardExportService.exportPdf(
                status,
                healthStatus
        );

        return createFileResponse(
                content,
                "cto-proje-raporu-"
                        + FILE_DATE_FORMATTER.format(
                        LocalDateTime.now()
                )
                        + ".pdf",
                MediaType.APPLICATION_PDF
        );
    }

    @Operation(summary = "Dashboard raporunu Excel olarak indirir")
    @GetMapping(
            value = "/export/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false)
            ProjectStatus status,

            @RequestParam(required = false)
            ProjectHealthStatus healthStatus
    ) {
        byte[] content = dashboardExportService.exportExcel(
                status,
                healthStatus
        );

        return createFileResponse(
                content,
                "cto-proje-raporu-"
                        + FILE_DATE_FORMATTER.format(
                        LocalDateTime.now()
                )
                        + ".xlsx",
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
        );
    }

    private ResponseEntity<byte[]> createFileResponse(
            byte[] content,
            String fileName,
            MediaType mediaType
    ) {
        ContentDisposition contentDisposition =
                ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(content.length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .body(content);
    }
}
