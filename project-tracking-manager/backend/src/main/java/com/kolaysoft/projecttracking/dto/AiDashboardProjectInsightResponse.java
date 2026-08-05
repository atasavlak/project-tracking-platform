package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.ProjectStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AiDashboardProjectInsightResponse {

    private Long projectId;
    private String projectName;
    private ProjectStatus projectStatus;
    private ProjectHealthStatus healthStatus;
    private int healthScore;
    private LocalDate latestReportWeekStartDate;
    private String reason;
}
