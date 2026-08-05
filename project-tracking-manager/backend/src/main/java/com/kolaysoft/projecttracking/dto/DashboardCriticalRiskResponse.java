package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.RiskIssueSeverity;
import com.kolaysoft.projecttracking.entity.RiskIssueStatus;
import com.kolaysoft.projecttracking.entity.RiskIssueType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DashboardCriticalRiskResponse {

    private Long riskIssueId;
    private Long projectId;
    private String projectName;
    private RiskIssueType type;
    private String title;
    private RiskIssueSeverity severity;
    private RiskIssueStatus status;
    private Long responsibleUserId;
    private String responsibleUserFullName;
    private LocalDate followUpDate;
}
