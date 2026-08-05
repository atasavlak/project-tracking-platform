package com.kolaysoft.projecttracking.repository;

import com.kolaysoft.projecttracking.entity.ActionItem;
import com.kolaysoft.projecttracking.entity.ActionItemPriority;
import com.kolaysoft.projecttracking.entity.ActionItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ActionItemRepository
        extends JpaRepository<ActionItem, Long> {

    boolean existsByProject_IdAndTitleIgnoreCaseAndActiveTrue(
            Long projectId,
            String title
    );

    boolean existsByProject_IdAndTitleIgnoreCaseAndActiveTrueAndIdNot(
            Long projectId,
            String title,
            Long id
    );

    List<ActionItem> findByActiveTrueAndStatusInAndTargetDateBefore(
            Collection<ActionItemStatus> statuses,
            LocalDate targetDate
    );

    @Query("""
            SELECT ai
            FROM ActionItem ai
            WHERE ai.active = true
              AND ai.project.active = true
              AND ai.responsibleUser.active = true
              AND ai.responsibleUser.activationCompleted = true
              AND (
                    ai.weeklyReport IS NULL
                    OR ai.weeklyReport.active = true
              )
              AND (
                    :projectId IS NULL
                    OR ai.project.id = :projectId
              )
              AND (
                    :weeklyReportId IS NULL
                    OR ai.weeklyReport.id = :weeklyReportId
              )
              AND (
                    :status IS NULL
                    OR ai.status = :status
              )
              AND (
                    :priority IS NULL
                    OR ai.priority = :priority
              )
              AND (
                    :responsibleUserId IS NULL
                    OR ai.responsibleUser.id = :responsibleUserId
              )
              AND (
                    :targetDateFrom IS NULL
                    OR ai.targetDate >= :targetDateFrom
              )
              AND (
                    :targetDateTo IS NULL
                    OR ai.targetDate <= :targetDateTo
              )
              AND (
                    :currentProjectManagerId IS NULL
                    OR ai.project.projectManager.id = :currentProjectManagerId
                    OR ai.responsibleUser.id = :currentProjectManagerId
              )
            ORDER BY ai.targetDate ASC, ai.priority DESC, ai.createdAt DESC
            """)
    List<ActionItem> searchVisibleActionItems(
            @Param("projectId") Long projectId,
            @Param("weeklyReportId") Long weeklyReportId,
            @Param("status") ActionItemStatus status,
            @Param("priority") ActionItemPriority priority,
            @Param("responsibleUserId") Long responsibleUserId,
            @Param("targetDateFrom") LocalDate targetDateFrom,
            @Param("targetDateTo") LocalDate targetDateTo,
            @Param("currentProjectManagerId") Long currentProjectManagerId
    );
}
