package com.kolaysoft.projecttracking.repository;

import com.kolaysoft.projecttracking.entity.RiskIssue;
import com.kolaysoft.projecttracking.entity.RiskIssueSeverity;
import com.kolaysoft.projecttracking.entity.RiskIssueStatus;
import com.kolaysoft.projecttracking.entity.RiskIssueType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RiskIssueRepository
        extends JpaRepository<RiskIssue, Long> {

    boolean existsByWeeklyReport_IdAndTitleIgnoreCaseAndActiveTrue(
            Long weeklyReportId,
            String title
    );

    boolean existsByWeeklyReport_IdAndTitleIgnoreCaseAndActiveTrueAndIdNot(
            Long weeklyReportId,
            String title,
            Long id
    );

    @Query("""
            SELECT ri
            FROM RiskIssue ri
            WHERE ri.active = true
              AND ri.weeklyReport.active = true
              AND ri.weeklyReport.project.active = true
              AND ri.responsibleUser.active = true
              AND ri.responsibleUser.activationCompleted = true
              AND (
                    :projectId IS NULL
                    OR ri.weeklyReport.project.id = :projectId
              )
              AND (
                    :weeklyReportId IS NULL
                    OR ri.weeklyReport.id = :weeklyReportId
              )
              AND (
                    :type IS NULL
                    OR ri.type = :type
              )
              AND (
                    :severity IS NULL
                    OR ri.severity = :severity
              )
              AND (
                    :status IS NULL
                    OR ri.status = :status
              )
              AND (
                    :responsibleUserId IS NULL
                    OR ri.responsibleUser.id = :responsibleUserId
              )
              AND (
                    :followUpDateFrom IS NULL
                    OR ri.followUpDate >= :followUpDateFrom
              )
              AND (
                    :followUpDateTo IS NULL
                    OR ri.followUpDate <= :followUpDateTo
              )
              AND (
                    :currentProjectManagerId IS NULL
                    OR ri.weeklyReport.project.projectManager.id =
                       :currentProjectManagerId
                    OR ri.responsibleUser.id =
                       :currentProjectManagerId
              )
            ORDER BY ri.followUpDate ASC, ri.createdAt DESC
            """)
    List<RiskIssue> searchVisibleRiskIssues(
            @Param("projectId")
            Long projectId,

            @Param("weeklyReportId")
            Long weeklyReportId,

            @Param("type")
            RiskIssueType type,

            @Param("severity")
            RiskIssueSeverity severity,

            @Param("status")
            RiskIssueStatus status,

            @Param("responsibleUserId")
            Long responsibleUserId,

            @Param("followUpDateFrom")
            LocalDate followUpDateFrom,

            @Param("followUpDateTo")
            LocalDate followUpDateTo,

            @Param("currentProjectManagerId")
            Long currentProjectManagerId
    );
}