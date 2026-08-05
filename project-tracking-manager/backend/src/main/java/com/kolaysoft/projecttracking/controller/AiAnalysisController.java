package com.kolaysoft.projecttracking.controller;

import com.kolaysoft.projecttracking.dto.AiDashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.AiWeeklyReportAnalysisRequest;
import com.kolaysoft.projecttracking.dto.AiWeeklyReportAnalysisResponse;
import com.kolaysoft.projecttracking.dto.ProjectHealthStatus;
import com.kolaysoft.projecttracking.entity.ProjectStatus;
import com.kolaysoft.projecttracking.service.AiAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(
        name = "AI Rapor Asistanı",
        description = "Haftalık rapor ve CTO dashboard verilerini analiz eden prototip işlemler"
)
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @Operation(
            summary = "Haftalık rapor taslağını analiz eder",
            description = "Kural tabanlı mock sağlayıcı ile durum, yönetici özeti, risk ve aksiyon önerileri üretir."
    )
    @PreAuthorize(
            "hasAnyRole('PROJECT_MANAGER', 'ADMIN')"
    )
    @PostMapping("/weekly-report-analysis")
    public ResponseEntity<AiWeeklyReportAnalysisResponse>
    analyzeWeeklyReport(
            @Valid
            @RequestBody
            AiWeeklyReportAnalysisRequest request
    ) {
        return ResponseEntity.ok(
                aiAnalysisService.analyzeWeeklyReport(
                        request
                )
        );
    }

    @Operation(
            summary = "CTO dashboard için AI yönetici özeti üretir",
            description = "Seçili proje ve sağlık filtrelerine göre son haftalık raporları, riskleri, aksiyonları ve kritik iş kalemlerini özetler."
    )
    @PreAuthorize(
            "hasAnyRole('CTO', 'ADMIN')"
    )
    @GetMapping("/dashboard-summary")
    public ResponseEntity<AiDashboardSummaryResponse>
    summarizeDashboard(
            @RequestParam(required = false)
            ProjectStatus status,

            @RequestParam(required = false)
            ProjectHealthStatus healthStatus
    ) {
        return ResponseEntity.ok(
                aiAnalysisService.summarizeDashboard(
                        status,
                        healthStatus
                )
        );
    }
}
