package com.kolaysoft.projecttracking.repository;

import com.kolaysoft.projecttracking.entity.DecisionLog;
import com.kolaysoft.projecttracking.entity.DecisionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DecisionLogRepository
        extends JpaRepository<DecisionLog, Long> {

    boolean existsByProject_IdAndTitleIgnoreCaseAndActiveTrue(
            Long projectId,
            String title
    );

    boolean existsByProject_IdAndTitleIgnoreCaseAndActiveTrueAndIdNot(
            Long projectId,
            String title,
            Long id
    );

    @Query("""
            SELECT dl
            FROM DecisionLog dl
            WHERE dl.active = true
              AND dl.project.active = true
              AND dl.decisionOwner.active = true
              AND dl.decisionOwner.activationCompleted = true
              AND (
                    dl.weeklyReport IS NULL
                    OR dl.weeklyReport.active = true
              )
              AND (
                    :projectId IS NULL
                    OR dl.project.id = :projectId
              )
              AND (
                    :weeklyReportId IS NULL
                    OR dl.weeklyReport.id = :weeklyReportId
              )
              AND (
                    :status IS NULL
                    OR dl.status = :status
              )
              AND (
                    :decisionOwnerId IS NULL
                    OR dl.decisionOwner.id = :decisionOwnerId
              )
              AND (
                    :decisionDateFrom IS NULL
                    OR dl.decisionDate >= :decisionDateFrom
              )
              AND (
                    :decisionDateTo IS NULL
                    OR dl.decisionDate <= :decisionDateTo
              )
              AND (
                    :currentProjectManagerId IS NULL
                    OR dl.project.projectManager.id = :currentProjectManagerId
                    OR dl.decisionOwner.id = :currentProjectManagerId
              )
            ORDER BY dl.decisionDate DESC, dl.createdAt DESC
            """)
    List<DecisionLog> searchVisibleDecisionLogs(
            @Param("projectId")
            Long projectId,

            @Param("weeklyReportId")
            Long weeklyReportId,

            @Param("status")
            DecisionStatus status,

            @Param("decisionOwnerId")
            Long decisionOwnerId,

            @Param("decisionDateFrom")
            LocalDate decisionDateFrom,

            @Param("decisionDateTo")
            LocalDate decisionDateTo,

            @Param("currentProjectManagerId")
            Long currentProjectManagerId
    );
}
